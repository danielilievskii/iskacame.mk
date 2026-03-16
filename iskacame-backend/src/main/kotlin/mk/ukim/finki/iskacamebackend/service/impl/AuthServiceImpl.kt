package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.auth.ResendTokenRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.VerifyTokenRequest
import mk.ukim.finki.iskacamebackend.dto.response.auth.AuthResponse
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.events.UserEnabledEvent
import mk.ukim.finki.iskacamebackend.events.UserRegisteredEvent
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.security.JwtService
import mk.ukim.finki.iskacamebackend.security.UserPrincipal
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import org.springframework.context.ApplicationEventPublisher
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
  private val authenticationManager: AuthenticationManager,
  private val jwtService: JwtService,
  private val verificationTokenService: VerificationTokenService,
  private val eventPublisher: ApplicationEventPublisher,
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

    val verificationToken = verificationTokenService.createVerificationToken(savedUser)

    eventPublisher.publishEvent(UserRegisteredEvent(savedUser, verificationToken))

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

    SecurityContextHolder.getContext().authentication = authentication

    val token = jwtService.generateToken(userPrincipal)

    val user = userRepository.findByEmail(userPrincipal.email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    val userDto = userMapper.toUserDto(user)

    return AuthResponse(
      token = token,
      user = userDto
    )
  }

  override fun resendVerificationToken(request: ResendTokenRequest) {

    val user = getUserByIdentifier(request.identifier)

    if (user.emailVerified) {
      throw ConflictException(AuthExceptionMessages.EMAIL_ALREADY_VERIFIED)
    }

    val verificationToken = verificationTokenService.createVerificationToken(user)

    eventPublisher.publishEvent(UserRegisteredEvent(user, verificationToken))
  }

  override fun verifyEmail(request: VerifyTokenRequest) {

    val user = getUserByIdentifier(request.identifier)

    if (user.emailVerified) {
      throw ConflictException(AuthExceptionMessages.EMAIL_ALREADY_VERIFIED)
    }

    verificationTokenService.verifyToken(user, request.token)
  }

  override fun reactivateAccount(request: SignInRequest) {

    val user = getUserByIdentifier(request.identifier)

    if (!passwordEncoder.matches(request.password, user.password)) {
      throw BadCredentialsException(AuthExceptionMessages.INVALID_CREDENTIALS)
    }

    if (user.enabled) {
      throw ConflictException(AuthExceptionMessages.ACCOUNT_ALREADY_ENABLED)
    }

    user.enabled = true
    user.disabledAt = null

    userRepository.save(user)

    eventPublisher.publishEvent(UserEnabledEvent(user))
  }

  override fun getCurrentUser(): User {

    val authentication = SecurityContextHolder.getContext().authentication
      ?: throw CustomAuthenticationException(AuthExceptionMessages.AUTHENTICATION_ERROR)

    val userPrincipal = authentication.principal as? UserPrincipal
      ?: throw CustomAuthenticationException(AuthExceptionMessages.INVALID_PRINCIPAL)

    val email = userPrincipal.username

    return userRepository.findByEmail(email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)
  }

  override fun getCurrentUserDto(): UserDto {

    val user = getCurrentUser()
    return userMapper.toUserDto(user)
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
}