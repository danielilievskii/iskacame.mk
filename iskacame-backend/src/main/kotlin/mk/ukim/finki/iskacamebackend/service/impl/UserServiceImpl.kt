package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import mk.ukim.finki.iskacamebackend.model.domain.AvatarImage
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import mk.ukim.finki.iskacamebackend.utils.ImageValidator
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val authService: AuthService,
  private val cloudinaryStorageService: CloudinaryStorageService,
) : UserService {

  override fun getUserById(id: Long): User {

    return userRepository.findById(id)
      .orElseThrow{ ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND) }
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

  private fun generateAvatarPublicId(userId: Long): String =
    "users/$userId/avatar"
}

