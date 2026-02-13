package mk.ukim.finki.iskacamebackend.service

import org.springframework.web.multipart.MultipartFile

interface UserService {

  /**
   * Uploads or updates a user's avatar.
   * This method handles both initial upload and updates (replaces existing picture).
   *
   * @param file The avatar file
   * @return The URL of the uploaded avatar
   * @throws IllegalArgumentException if the image doesn't meet requirements
   */
  fun uploadAvatar(file: MultipartFile): String

  /**
   * Deletes a user's avatar
   */
  fun deleteAvatar()
}