package mk.ukim.finki.iskacamebackend.security

import io.jsonwebtoken.JwtException
import jakarta.security.auth.message.AuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mk.ukim.finki.iskacamebackend.common.JWTConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import kotlin.text.startsWith
import kotlin.text.substring

@Component
class JwtAuthenticationFilter(
  private val jwtService: JwtService,
  private val customUserDetailsService: CustomUserDetailsService,

  @Qualifier("handlerExceptionResolver")
  private val resolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

  private fun getJwtFromRequest(request: HttpServletRequest): String? {
    val bearerToken = request.getHeader(JWTConstants.BEARER_TOKEN_HEADER)

    return if (bearerToken != null && bearerToken.startsWith(JWTConstants.TOKEN_PREFIX)) {
      bearerToken.substring(7)
    } else null
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    try {
      val jwt = getJwtFromRequest(request)

      if (jwt != null && SecurityContextHolder.getContext().authentication == null) {
        jwtService.validateToken(jwt)

        val email = jwtService.getEmailFromToken(jwt)
        val userDetails = customUserDetailsService.loadUserByUsername(email)

        AccountStatusUserDetailsChecker().check(userDetails)

        val authentication = UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.authorities
        )

        authentication.details = WebAuthenticationDetailsSource()
          .buildDetails(request)

        SecurityContextHolder.getContext().authentication = authentication
      }

      filterChain.doFilter(request, response)

    } catch (ex: JwtException) {
      resolver.resolveException(request, response, null, ex)
      return
    } catch (ex: IllegalArgumentException) {
      resolver.resolveException(request, response, null, ex)
      return
    } catch (ex: AuthException) {
      resolver.resolveException(request, response, null, ex)
      return
    } catch (ex: Exception) {
      resolver.resolveException(request, response, null, ex)
      return
    }
  }
}