package mk.ukim.finki.iskacamebackend.utils

import mk.ukim.finki.iskacamebackend.common.StorageExceptionMessages
import org.springframework.web.multipart.MultipartFile
import javax.imageio.ImageIO

/**
 * Utility class for validating image files
 */
object ImageValidator {

  private val ALLOWED_CONTENT_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp"
  )

  private const val MAX_FILE_SIZE: Long = 5242880

  /**
   * Validates an uploaded image file
   *
   * @param file The file to validate
   * @throws IllegalArgumentException if validation fails
   */
  fun validate(file: MultipartFile) {

    if (file.isEmpty) {
      throw IllegalArgumentException(StorageExceptionMessages.IMAGE_EMPTY)
    }

    if (file.size > MAX_FILE_SIZE) {
      throw IllegalArgumentException(StorageExceptionMessages.IMAGE_TOO_LARGE)
    }

    val contentType = file.contentType

    if (contentType == null || contentType !in ALLOWED_CONTENT_TYPES) {
      throw IllegalArgumentException(StorageExceptionMessages.IMAGE_UNSUPPORTED_TYPE)
    }

    file.inputStream.use { inputStream ->
      val image = ImageIO.read(inputStream)
        ?: throw IllegalArgumentException(StorageExceptionMessages.IMAGE_INVALID)
    }
  }
}

