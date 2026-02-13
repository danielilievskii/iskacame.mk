package mk.ukim.finki.iskacamebackend.service.impl

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import mk.ukim.finki.iskacamebackend.common.StorageExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import mk.ukim.finki.iskacamebackend.exception.StorageException
import mk.ukim.finki.iskacamebackend.service.CloudinaryStorageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Implementation of CloudinaryStorageService
 */
@Service
class CloudinaryStorageServiceImpl(
  private val cloudinary: Cloudinary
) : CloudinaryStorageService {

  override fun uploadFile(file: MultipartFile, publicId: String?): CloudinaryUploadResponse {

    val params = mutableMapOf<String, Any>(
      "resource_type" to "auto",
      "overwrite" to true,
      "invalidate" to true
    )

    publicId?.let {
      params["public_id"] = it
    }

    try {
      val response = cloudinary.uploader()
        .upload(file.bytes, params)

      return CloudinaryUploadResponse(
        url = response["secure_url"] as String,
        publicId = response["public_id"] as String
      )
    } catch (e: Exception) {
      throw StorageException(StorageExceptionMessages.UPLOAD_FAILED)
    }
  }

  override fun deleteFile(publicId: String): Boolean {

    return try {
      val options = ObjectUtils.emptyMap()

      val response = cloudinary.uploader()
        .destroy(publicId, options)

      response["result"] == "ok"
    } catch (e: Exception) {
      throw StorageException(StorageExceptionMessages.DELETE_FAILED)
    }
  }
}

