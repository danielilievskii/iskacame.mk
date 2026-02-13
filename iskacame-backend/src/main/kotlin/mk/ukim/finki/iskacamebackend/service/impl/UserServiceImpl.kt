package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import mk.ukim.finki.iskacamebackend.model.AvatarImage
import mk.ukim.finki.iskacamebackend.model.User
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.service.AuthService
import mk.ukim.finki.iskacamebackend.service.CloudinaryStorageService
import mk.ukim.finki.iskacamebackend.service.UserService
import mk.ukim.finki.iskacamebackend.utils.ImageValidator
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val authService: AuthService,
  private val cloudinaryStorageService: CloudinaryStorageService,
) : UserService {

  override fun uploadAvatar(file: MultipartFile): String {

    ImageValidator.validate(file)

    val user: User = authService.getCurrentUser()
    val avatarPublicId = generateAvatarPublicId(user.id!!)

    val response: CloudinaryUploadResponse =
      cloudinaryStorageService.uploadFile(file, avatarPublicId)

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

