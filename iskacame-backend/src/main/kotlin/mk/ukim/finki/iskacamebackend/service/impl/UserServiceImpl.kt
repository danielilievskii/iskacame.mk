package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.user.UpdateUserRequest
import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.AvatarImage
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.utils.ImageValidator
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
) : UserService {

  override fun getUserById(id: Long): User {

    return userRepository.findById(id)
      .orElseThrow { ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND) }
  }

  override fun getUsersByIds(ids: List<Long>): List<User> {

    return userRepository.findAllById(ids)
      .takeIf { it.size == ids.size }
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)
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

  private fun generateAvatarPublicId(userId: Long): String =
    "users/$userId/avatar"
}

