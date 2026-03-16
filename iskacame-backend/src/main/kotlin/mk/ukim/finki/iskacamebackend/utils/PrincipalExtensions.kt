package mk.ukim.finki.iskacamebackend.utils

import mk.ukim.finki.iskacamebackend.security.principal.UserPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.security.Principal

/**
 * Extracts the authenticated user's ID from a [Principal].
 * Assumes the principal is a [UsernamePasswordAuthenticationToken]
 * with a [UserPrincipal] as its inner principal.
 *
 * @throws ClassCastException if the principal chain is not the expected type
 */
fun Principal.extractUserId(): Long {

    val auth = this as UsernamePasswordAuthenticationToken
    val userPrincipal = auth.principal as UserPrincipal

    return userPrincipal.id
}