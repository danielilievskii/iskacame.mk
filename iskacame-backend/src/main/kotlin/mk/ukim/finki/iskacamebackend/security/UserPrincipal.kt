package mk.ukim.finki.iskacamebackend.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UserPrincipal(
  val id: Long,
  private val name: String,
  val email: String,
  private val password: String,
  val emailVerified: Boolean,
  private val authorities: Collection<GrantedAuthority>
) : UserDetails {

  override fun getAuthorities(): Collection<GrantedAuthority> = authorities

  override fun getPassword(): String = password

  override fun getUsername(): String = email

  override fun isAccountNonExpired(): Boolean = true

  override fun isAccountNonLocked(): Boolean = true

  override fun isCredentialsNonExpired(): Boolean = true

  override fun isEnabled(): Boolean = true
}