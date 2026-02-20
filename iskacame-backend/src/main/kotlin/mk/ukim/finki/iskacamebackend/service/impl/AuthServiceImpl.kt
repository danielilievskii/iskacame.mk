package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.UserDto
import mk.ukim.finki.iskacamebackend.dto.request.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.response.AuthResponse
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.User
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.security.JwtService
import mk.ukim.finki.iskacamebackend.security.UserPrincipal
import mk.ukim.finki.iskacamebackend.service.AuthService
import org.springframework.security.authentication.AuthenticationManager
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
      emailVerified = true,
    )

    return userRepository.save(user)
      .let(userMapper::toUserDto)
  }

  override fun signIn(request: SignInRequest): AuthResponse {

    val authToken = UsernamePasswordAuthenticationToken(
      request.email,
      request.password
    )

    val authentication = authenticationManager.authenticate(authToken)
    SecurityContextHolder.getContext().authentication = authentication

    val userPrincipal = authentication.principal as UserPrincipal
    val token = jwtService.generateToken(userPrincipal)

    val user = userRepository.findByEmail(userPrincipal.email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    val userDto = userMapper.toUserDto(user)

    return AuthResponse(
      token = token,
      user = userDto
    )
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
}