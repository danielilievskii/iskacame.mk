package mk.ukim.finki.iskacamebackend.service.impl

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.auth.ForgotPasswordRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.GoogleAuthRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.RefreshTokenRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.ResendTokenRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.ResetPasswordRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.VerifyTokenRequest
import mk.ukim.finki.iskacamebackend.dto.response.auth.AuthResponse
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.events.PasswordResetEvent
import mk.ukim.finki.iskacamebackend.dto.response.user.UserSearchDto
import mk.ukim.finki.iskacamebackend.events.UserEnabledEvent
import mk.ukim.finki.iskacamebackend.events.UserRegisteredEvent
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.CurrentUserMapper
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.AvatarImage
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.AuthProvider
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import mk.ukim.finki.iskacamebackend.model.enums.VerificationTokenPurpose
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.security.jwt.JwtService
import mk.ukim.finki.iskacamebackend.security.principal.UserPrincipal
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.intf.RefreshTokenService
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Implementation of the AuthService.
 */
@Service
class AuthServiceImpl(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val userMapper: UserMapper,
  private val currentUserMapper: CurrentUserMapper,
  private val authenticationManager: AuthenticationManager,
  private val jwtService: JwtService,
  private val refreshTokenService: RefreshTokenService,
  private val verificationTokenService: VerificationTokenService,
  private val cloudinaryStorageService: CloudinaryStorageService,
  private val eventPublisher: ApplicationEventPublisher,
  @Value("\${google.client-id}") private val googleClientId: String,
  @Value("\${google.client-secret}") private val googleClientSecret: String,
  @Value("\${google.ios-client-id:}") private val googleIosClientId: String
) : AuthService {

  override fun signUp(request: SignUpRequest): UserDto {

    if (userRepository.existsByEmail(request.email)) {
      throw ConflictException(AuthExceptionMessages.EMAIL_TAKEN)
    }

    if (userRepository.existsByUsername(request.username)) {
      throw ConflictException(AuthExceptionMessages.USERNAME_TAKEN)
    }

    val encodedPassword = passwordEncoder.encode(request.password)
      ?: throw CustomAuthenticationException(AuthExceptionMessages.AUTHENTICATION_ERROR)

    val roles = mutableSetOf(UserRole.USER)

    val user = User(
      name = request.name,
      username = request.username,
      email = request.email,
      password = encodedPassword,
      roles = roles,
      emailVerified = false,
      phone = request.phone
    )

    val savedUser = userRepository.save(user)

    val verificationToken = verificationTokenService.createVerificationToken(savedUser, VerificationTokenPurpose.EMAIL_VERIFICATION)

    val event = UserRegisteredEvent(savedUser, verificationToken)
    eventPublisher.publishEvent(event)

    return userMapper.toUserDto(savedUser)
  }

  override fun signIn(request: SignInRequest): AuthResponse {

    val authToken = UsernamePasswordAuthenticationToken(
      request.identifier,
      request.password
    )

    val authentication = authenticationManager.authenticate(authToken)
    val userPrincipal = authentication.principal as UserPrincipal

    if (!userPrincipal.emailVerified) {
      throw DisabledException(AuthExceptionMessages.EMAIL_NOT_VERIFIED)
    }

    val context = SecurityContextHolder.getContext()
    context.authentication = authentication

    val token = jwtService.generateToken(userPrincipal)
    val refreshToken = jwtService.generateRefreshToken(userPrincipal)

    val user = userRepository.findByEmail(userPrincipal.email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    refreshTokenService.create(user, refreshToken)

    val userDto = currentUserMapper.toCurrentUserDto(user)

    return AuthResponse(
      token = token,
      refreshToken = refreshToken,
      user = userDto
    )
  }

  override fun refreshToken(request: RefreshTokenRequest): AuthResponse {

    val (newAccessToken, newRefreshToken) = refreshTokenService.rotate(request.refreshToken)

    val email = jwtService.getEmailFromToken(newAccessToken)
    val user = userRepository.findByEmail(email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    return AuthResponse(
      token = newAccessToken,
      refreshToken = newRefreshToken,
      user = currentUserMapper.toCurrentUserDto(user)
    )
  }

  override fun logout(request: RefreshTokenRequest) {
    refreshTokenService.revoke(request.refreshToken)
  }

  override fun resendVerificationToken(request: ResendTokenRequest) {

    val user = getUserByIdentifier(request.identifier)

    if (user.emailVerified) {
      throw ConflictException(AuthExceptionMessages.EMAIL_ALREADY_VERIFIED)
    }

    val verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenPurpose.EMAIL_VERIFICATION)

    val event = UserRegisteredEvent(user, verificationToken)
    eventPublisher.publishEvent(event)
  }

  override fun verifyEmail(request: VerifyTokenRequest) {

    val user = getUserByIdentifier(request.identifier)

    if (user.emailVerified) {
      throw ConflictException(AuthExceptionMessages.EMAIL_ALREADY_VERIFIED)
    }

    verificationTokenService.consumeToken(user, request.token, VerificationTokenPurpose.EMAIL_VERIFICATION)

    user.emailVerified = true
    userRepository.save(user)
  }

  override fun reactivateAccount(request: SignInRequest) {

    val user = getUserByIdentifier(request.identifier)

    val isPasswordCorrect = passwordEncoder.matches(request.password, user.password)

    if (!isPasswordCorrect) {
      throw BadCredentialsException(AuthExceptionMessages.INVALID_CREDENTIALS)
    }

    if (user.enabled) {
      throw ConflictException(AuthExceptionMessages.ACCOUNT_ALREADY_ENABLED)
    }

    user.enabled = true
    user.disabledAt = null

    userRepository.save(user)

    val event = UserEnabledEvent(user)
    eventPublisher.publishEvent(event)
  }

  override fun forgotPassword(request: ForgotPasswordRequest) {
    val user = getUserByIdentifier(request.identifier)

    val verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenPurpose.PASSWORD_RESET)

    val event = PasswordResetEvent(user, verificationToken)
    eventPublisher.publishEvent(event)
  }

  override fun resetPassword(request: ResetPasswordRequest) {
    val user = getUserByIdentifier(request.identifier)

    verificationTokenService.consumeToken(user, request.token, VerificationTokenPurpose.PASSWORD_RESET)

    val encodedPassword = passwordEncoder.encode(request.newPassword)
      ?: throw CustomAuthenticationException(AuthExceptionMessages.AUTHENTICATION_ERROR)

    user.password = encodedPassword
    userRepository.save(user)
  }

  override fun googleSignIn(request: GoogleAuthRequest): AuthResponse {

    val httpTransport = NetHttpTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()

    val tokenResponse = try {
      AuthorizationCodeTokenRequest(httpTransport, jsonFactory, GenericUrl("https://oauth2.googleapis.com/token"), request.code)
        .setRedirectUri(request.redirectUri)
        .setClientAuthentication(BasicAuthentication(googleClientId, googleClientSecret))
        .setGrantType("authorization_code")
        .execute()
    } catch (e: Exception) {
      throw BadRequestException(AuthExceptionMessages.INVALID_GOOGLE_TOKEN)
    }

    val rawIdToken = tokenResponse["id_token"] as? String
      ?: throw BadRequestException(AuthExceptionMessages.INVALID_GOOGLE_TOKEN)

    val audiences = listOfNotNull(googleClientId, googleIosClientId.ifBlank { null })
    val verifier = GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
      .setAudience(audiences)
      .build()

    val idToken: GoogleIdToken = verifier.verify(rawIdToken)
      ?: throw BadRequestException(AuthExceptionMessages.INVALID_GOOGLE_TOKEN)

    val payload = idToken.payload
    val googleId = payload.subject
    val email = payload.email
      ?: throw BadRequestException(AuthExceptionMessages.GOOGLE_EMAIL_MISSING)
    val name = payload["name"] as? String ?: email.substringBefore("@")
    val pictureUrl = payload["picture"] as? String

    var user = userRepository.findByGoogleId(googleId)

    if (user == null) {
      val existingUser = userRepository.findByEmail(email)
      if (existingUser != null) {
        if (existingUser.authProvider == AuthProvider.LOCAL) {
          throw ConflictException(AuthExceptionMessages.ACCOUNT_EXISTS_WITH_LOCAL)
        }
        user = existingUser
      } else {
        val username = generateUniqueUsername(email.substringBefore("@"))

        var avatar: AvatarImage? = null
        if (pictureUrl != null) {
          try {
            val uploadResponse = cloudinaryStorageService.uploadFromUrl(pictureUrl, "avatars/$username")
            avatar = AvatarImage(url = uploadResponse.url, publicId = uploadResponse.publicId)
          } catch (_: Exception) {
            // continue without avatar
          }
        }

        user = User(
          name = name,
          username = username,
          email = email,
          password = null,
          roles = mutableSetOf(UserRole.USER),
          emailVerified = true,
          enabled = true,
          authProvider = AuthProvider.GOOGLE,
          googleId = googleId,
          avatar = avatar
        )
        user = userRepository.save(user)
      }
    }

    val userPrincipal = userMapper.toUserPrincipal(user)
    val token = jwtService.generateToken(userPrincipal)
    val refreshToken = jwtService.generateRefreshToken(userPrincipal)

    refreshTokenService.create(user, refreshToken)

    return AuthResponse(
      token = token,
      refreshToken = refreshToken,
      user = currentUserMapper.toCurrentUserDto(user)
    )
  }

  override fun getCurrentUser(): User {

    val context = SecurityContextHolder.getContext()

    val authentication = context.authentication
      ?: throw CustomAuthenticationException(AuthExceptionMessages.AUTHENTICATION_ERROR)

    val userPrincipal = authentication.principal as? UserPrincipal
      ?: throw CustomAuthenticationException(AuthExceptionMessages.INVALID_PRINCIPAL)

    val email = userPrincipal.username

    return userRepository.findByEmail(email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)
  }

  override fun getCurrentUserDto(): UserDto {

    val user = getCurrentUser()
    return currentUserMapper.toCurrentUserDto(user)
  }

  override fun getUserDtoByIdentifier(identifier: String): List<UserSearchDto> {
    val users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(identifier, identifier, identifier)

    return users.map { userMapper.toUserSearchDto(it) }
  }

  override fun getCurrentUserId(): Long {

    val user = getCurrentUser()
    return user.id ?: throw IllegalStateException("User ID not assigned")
  }

  private fun getUserByIdentifier(identifier: String): User {

    val user = if (identifier.contains("@")) {
      userRepository.findByEmail(identifier)
    } else {
      userRepository.findByUsername(identifier)
    } ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    return user
  }

  private fun generateUniqueUsername(base: String): String {
    val sanitized = base.replace(Regex("[^a-zA-Z0-9_-]"), "").take(20)
    var candidate = sanitized
    var counter = 1
    while (userRepository.existsByUsername(candidate)) {
      candidate = "${sanitized}_$counter"
      counter++
    }
    return candidate
  }
}