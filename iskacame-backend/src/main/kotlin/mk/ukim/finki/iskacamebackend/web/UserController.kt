package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import mk.ukim.finki.iskacamebackend.dto.request.user.ChangeEmailRequest
import mk.ukim.finki.iskacamebackend.dto.request.user.ChangePasswordRequest
import mk.ukim.finki.iskacamebackend.dto.request.user.ConfirmEmailRequest
import mk.ukim.finki.iskacamebackend.dto.request.user.ConfirmPasswordRequest
import mk.ukim.finki.iskacamebackend.dto.request.user.UpdateUserRequest
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class UserController(
  private val userService: UserService,
  private val authService: AuthService,
) {

  @GetMapping("/me")
  @Operation(summary = "Get current user info")
  fun getMe(): ResponseEntity<UserDto> {

    val userDto: UserDto = authService.getCurrentUserDto()
    return ResponseEntity.ok(userDto)
  }

  @PostMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @Operation(summary = "Upload avatar for current user")
  fun uploadAvatar(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {

    val avatarUrl = userService.uploadAvatar(file)
    return ResponseEntity.ok(avatarUrl)
  }

  @DeleteMapping("/me/avatar")
  @Operation(summary = "Delete avatar for current user")
  fun deleteAvatar(): ResponseEntity<Void> {

    userService.deleteAvatar()
    return ResponseEntity.noContent().build()
  }

  @PatchMapping("/me/update")
  @Operation(summary = "Update current user info")
  fun updateProfile(@Valid @RequestBody updateUserRequest: UpdateUserRequest): ResponseEntity<UserDto> {

    val userDto: UserDto = userService.updateUser(updateUserRequest)
    return ResponseEntity.ok(userDto)
  }

  @PatchMapping("/me/disable")
  @Operation(summary = "Disable current user account")
  fun disableAccount(): ResponseEntity<Void> {

    userService.disableUser()
    return ResponseEntity.noContent().build()
  }

  @PostMapping("/change-password")
  @Operation(summary = "Requests a password change code")
  fun requestPasswordChange(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<Void> {
    userService.requestPasswordChange(request)
    return ResponseEntity.accepted().build()
  }

  @PatchMapping("/confirm-password")
  @Operation(summary = "Confirms user's new password with provided token")
  fun confirmPasswordChange(@Valid @RequestBody request: ConfirmPasswordRequest): ResponseEntity<Void> {
    userService.confirmPasswordChange(request)
    return ResponseEntity.noContent().build()
  }

  @PostMapping("/change-email")
  @Operation(summary = "Requests an email change code")
  fun requestEmailChange(@Valid @RequestBody request: ChangeEmailRequest): ResponseEntity<Void> {
    userService.requestEmailChange(request)
    return ResponseEntity.accepted().build()
  }

  @PatchMapping("/confirm-email")
  @Operation(summary = "Confirms user's new email with provided token")
  fun confirmEmailChange(@Valid @RequestBody request: ConfirmEmailRequest): ResponseEntity<Void> {
    userService.confirmEmailChange(request)
    return ResponseEntity.noContent().build()
  }
}