package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.user.*
import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.events.EmailChangeEvent
import mk.ukim.finki.iskacamebackend.events.PasswordChangeEvent
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.AvatarImage
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.VerificationTokenPurpose
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import mk.ukim.finki.iskacamebackend.utils.ImageValidator
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Implementation of UserService
 */
@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val authService: AuthService,
  private val cloudinaryStorageService: CloudinaryStorageService,
  private val userMapper: UserMapper,
  private val verificationTokenService: VerificationTokenService,
  private val passwordEncoder: PasswordEncoder,
  private val eventPublisher: ApplicationEventPublisher
) : UserService {

  override fun getUserById(id: Long): User {

    return userRepository.findById(id)
      .orElseThrow { ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND) }
  }

  override fun uploadAvatar(file: MultipartFile): String {

    ImageValidator.validate(file)

    val user: User = authService.getCurrentUser()
    val avatarPublicId = generateAvatarPublicId(user.id!!)

    val response: CloudinaryUploadResponse = cloudinaryStorageService.uploadFile(file, avatarPublicId)

    user.avatar = AvatarImage(
      url = response.url,
      publicId = response.publicId
    )

    userRepository.save(user)
    return response.url
  }

  override fun deleteAvatar() {

    val user: User = authService.getCurrentUser()

    user.avatar?.publicId?.let { avatarPublicId ->
      cloudinaryStorageService.deleteFile(avatarPublicId)
    }

    user.avatar = null
    userRepository.save(user)
  }

  @Transactional
  override fun updateUser(request: UpdateUserRequest): UserDto {

    val user: User = authService.getCurrentUser()
    val isUsernameTaken = userRepository.existsByUsernameAndIdNot(request.username, user.id!!)

    if (isUsernameTaken) {
      throw ConflictException(AuthExceptionMessages.USERNAME_TAKEN)
    }

    user.name = request.name
    user.username = request.username
    user.phone = request.phone

    val updatedUser = userRepository.save(user)
    return userMapper.toUserDto(updatedUser)
  }

  @Transactional
  override fun disableUser() {

    val user: User = authService.getCurrentUser()

    user.enabled = false
    user.disabledAt = Instant.now()
    userRepository.save(user)
  }

  @Transactional
  override fun cleanUpDisabledUsers(): Int {
    val cutoff = Instant.now().minus(30, ChronoUnit.DAYS)
    return userRepository.deleteAllByDisabledAtBeforeAndEnabledFalse(cutoff)
  }

  override fun requestPasswordChange(request: ChangePasswordRequest) {
    val user = authService.getCurrentUser()

    val isPasswordCorrect = passwordEncoder.matches(request.currentPassword, user.password)
    if (!isPasswordCorrect) {
      throw BadCredentialsException(AuthExceptionMessages.INVALID_CREDENTIALS)
    }

    val verificationToken = verificationTokenService.createVerificationToken(
      user,
      VerificationTokenPurpose.PASSWORD_CHANGE
    )

    val event = PasswordChangeEvent(user, verificationToken)
    eventPublisher.publishEvent(event)
  }

  override fun confirmPasswordChange(request: ConfirmPasswordRequest) {
    val user = authService.getCurrentUser()

    verificationTokenService.consumeToken(
      user = user,
      token = request.token,
      purpose = VerificationTokenPurpose.PASSWORD_CHANGE
    )

    val encodedPassword = passwordEncoder.encode(request.newPassword)
      ?: throw CustomAuthenticationException(AuthExceptionMessages.AUTHENTICATION_ERROR)

    user.password = encodedPassword
    userRepository.save(user)
  }

  override fun requestEmailChange(request: ChangeEmailRequest) {
    val user = authService.getCurrentUser()

    if (userRepository.existsByEmail(request.newEmail)) {
      throw ConflictException(AuthExceptionMessages.EMAIL_TAKEN)
    }

    val verificationToken = verificationTokenService.createVerificationToken(
      user,
      VerificationTokenPurpose.EMAIL_CHANGE
    )

    val event = EmailChangeEvent(user, request.newEmail, verificationToken)
    eventPublisher.publishEvent(event)
  }

  override fun confirmEmailChange(request: ConfirmEmailRequest) {
    val user = authService.getCurrentUser()

    verificationTokenService.consumeToken(
      user = user,
      token = request.token,
      purpose = VerificationTokenPurpose.EMAIL_CHANGE
    )

    if (userRepository.existsByEmail(request.newEmail)) {
      throw ConflictException(AuthExceptionMessages.EMAIL_TAKEN)
    }

    user.email = request.newEmail
    userRepository.save(user)
  }

  private fun generateAvatarPublicId(userId: Long): String =
    "users/$userId/avatar"
}

