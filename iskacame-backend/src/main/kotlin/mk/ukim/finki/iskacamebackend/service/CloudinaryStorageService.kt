package mk.ukim.finki.iskacamebackend.service

import mk.ukim.finki.iskacamebackend.dto.response.CloudinaryUploadResponse
import org.springframework.web.multipart.MultipartFile

/**
 * Interface for Cloudinary storage operations
 */
interface CloudinaryStorageService {

  /**
   * Uploads a file to Cloudinary
   *
   * @param file The file to upload
   * @param publicId Public identifier of the file
   * @return The public URL of the uploaded file
   */
  fun uploadFile(file: MultipartFile, publicId: String? = null): CloudinaryUploadResponse

  /**
   * Deletes a file from Cloudinary
   *
   * @param publicId The public identifier of the file to delete
   * @return true if deletion was successful, false otherwise
   */
  fun deleteFile(publicId: String): Boolean
}