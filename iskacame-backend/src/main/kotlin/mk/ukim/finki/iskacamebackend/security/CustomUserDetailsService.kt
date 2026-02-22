package mk.ukim.finki.iskacamebackend.security

import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
  private val userRepository: UserRepository,
  private val userMapper: UserMapper
) : UserDetailsService {

  override fun loadUserByUsername(email: String): UserDetails {

    val user: User = userRepository.findByEmail(email)
      ?: throw UsernameNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    return userMapper.toUserPrincipal(user)
  }
}