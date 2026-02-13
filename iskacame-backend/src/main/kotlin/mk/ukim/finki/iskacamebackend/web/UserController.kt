package mk.ukim.finki.iskacamebackend.web

import mk.ukim.finki.iskacamebackend.service.UserService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
class UserController(
  private val userService: UserService,
) {

  @PostMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun uploadAvatar(
    @RequestParam("file") file: MultipartFile
  ): ResponseEntity<String> {

    val avatarUrl = userService.uploadAvatar(file)
    return ResponseEntity.ok(avatarUrl)
  }

  @DeleteMapping("/me/avatar")
  fun deleteAvatar(): ResponseEntity<Void> {

    userService.deleteAvatar()
    return ResponseEntity.noContent().build()
  }
}