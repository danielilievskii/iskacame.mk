package mk.ukim.finki.iskacamebackend.service

import mk.ukim.finki.iskacamebackend.dto.UserDTO
import mk.ukim.finki.iskacamebackend.dto.request.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.response.AuthResponse
import mk.ukim.finki.iskacamebackend.model.User

interface AuthService {

  /**
   * Registers a new user account by validating username and email uniqueness,
   * encoding the password, persisting the user entity, and returning the corresponding [UserDTO].
   *
   * @param request the sign-up request containing username, email, and password
   * @return the created user as a [UserDTO]
   *
   * @throws BadRequestException if the username or email is already in use
   * @throws CustomAuthenticationException if password encoding fails
   */
  fun signUp(request: SignUpRequest): UserDTO

  /**
   * Authenticates a user by validating credentials, storing the authentication in the
   * security context, generating a JWT token, and returning an authentication response
   * containing the token and user details.
   *
   * @param request the sign-in request containing username and password
   * @return an [AuthResponse] containing the JWT token and user details
   *
   * @throws AuthenticationException if authentication fails
   * @throws UsernameNotFoundException if the authenticated user cannot be found
   */
  fun signIn(request: SignInRequest): AuthResponse

  /**
   * Returns the currently authenticated [User] from the current JWT
   * by extracting the "email" claim and looking it up.
   *
   * @throws CustomAuthenticationException if no auth or invalid principal
   * @throws ResourceNotFoundException if user not found
   */
  fun getCurrentUser(): User

  /**
   * Returns the ID of the currently authenticated [User].   *
   *
   * @throws IllegalStateException if the user ID has not been assigned
   */
  fun getCurrentUserId(): Long
}