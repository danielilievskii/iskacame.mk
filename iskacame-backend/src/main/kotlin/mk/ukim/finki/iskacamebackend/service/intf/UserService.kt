package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.user.UpdateUserRequest
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.domain.User
import org.springframework.web.multipart.MultipartFile
import mk.ukim.finki.iskacamebackend.exception.ConflictException

interface UserService {

  /**
   * Retrieves a user by ID
   *
   * @param id the ID of the user to retrieve
   * @return the User entity
   */
  fun getUserById(id: Long): User

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

  /**
   * Updates the current user's profile information.
   * Validates that the new username is not already taken by another user.
   *
   * @param request The update request containing a new name, username, and phone
   * @return The updated user details as a UserDto
   * @throws ConflictException if the username is already in use by another account
   */
  fun updateUser(request: UpdateUserRequest): UserDto

  /**
   * Disables the current user's account.
   */
  fun disableUser()
}