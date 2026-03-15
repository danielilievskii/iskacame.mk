package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.auth.ResendTokenRequest
import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.VerifyTokenRequest
import mk.ukim.finki.iskacamebackend.dto.response.auth.AuthResponse
import mk.ukim.finki.iskacamebackend.dto.response.user.UserSearchDto
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.exception.ConflictException

interface AuthService {

  /**
   * Registers a new user account by validating username and email uniqueness,
   * encoding the password, persisting the user entity, and returning the corresponding [UserDto].
   *
   * @param request the sign-up request containing username, email, and password
   * @return the created user as a [UserDto]
   *
   * @throws BadRequestException if the username or email is already in use
   * @throws CustomAuthenticationException if password encoding fails
   */
  fun signUp(request: SignUpRequest): UserDto

  /**
   * Authenticates a user by validating credentials, storing the authentication in the
   * security context, generating a JWT token, and returning an authentication response
   * containing the token and user details.
   *
   * @param request the sign-in request containing username and password
   * @return an [AuthResponse] containing the JWT token and user details
   *
   * @throws org.springframework.security.core.AuthenticationException if authentication fails
   * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the authenticated user cannot be found
   */
  fun signIn(request: SignInRequest): AuthResponse

  /**
   * Resends a verification token to the user associated with the provided email address.
   *
   * @param request the resend request containing the user's email
   *
   * @throws ResourceNotFoundException if no user exists with the provided email
   * @throws ConflictException if the user's email is already verified
   */
  fun resendVerificationToken(request: ResendTokenRequest)

  /**
   * Verifies a user's email address using the provided verification token.
   *
   * @param request the verification request containing the user's email and token
   *
   * @throws ResourceNotFoundException if no user exists with the provided email
   * @throws ConflictException if the user's email is already verified
   */
  fun verifyEmail(request: VerifyTokenRequest)

  /**
   * Returns the currently authenticated [User] from the current JWT
   * by extracting the "email" claim and looking it up.
   *
   * @throws CustomAuthenticationException if no auth or invalid principal
   * @throws ResourceNotFoundException if user not found
   */
  fun getCurrentUser(): User

  /**
   * Retrieves the currently authenticated user and maps it
   * to a [UserDto] representation.
   *
   * This method internally calls [getCurrentUser] and converts
   * the returned entity into a DTO suitable for API responses.
   *
   * @return the authenticated user as [UserDto]
   */
  fun getCurrentUserDto(): UserDto

  /**
   * Retrieves the user found by an identifier and maps it
   * to a list of [UserSearchDto] representation.
   *
   * This method internally calls [getUserDtoByIdentifier] and converts
   * the returned entity into a DTO suitable for API responses.
   *
   * @return the found users as list of [UserSearchDto]
   */
  fun getUserDtoByIdentifier(identifier: String): List<UserSearchDto>

  /**
   * Returns the ID of the currently authenticated [User].   *
   *
   * @throws IllegalStateException if the user ID has not been assigned
   */
  fun getCurrentUserId(): Long
}