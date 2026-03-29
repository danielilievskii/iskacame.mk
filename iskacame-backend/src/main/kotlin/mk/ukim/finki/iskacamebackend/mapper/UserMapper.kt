package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.dto.response.user.UserSearchDto
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import mk.ukim.finki.iskacamebackend.security.principal.UserPrincipal
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Class for mapping between to [User] entities and DTOs.
 */
@Mapper(componentModel = "spring")
interface UserMapper {

  /**
   * Maps a [User] object to a [UserDto] object.
   *
   * @param user the User entity object
   * @return the mapped UserDto object
   */
  @Mapping(target = "avatarUrl", source = "avatar.url")
  fun toUserDto(user: User): UserDto

  /**
   * Maps a [User] object to a [UserSearchDto] object.
   *
   * @param user the User entity object
   * @return the mapped UserSearchDto object
   */
  @Mapping(target = "avatarUrl", source = "avatar.url")
  fun toUserSearchDto(user: User): UserSearchDto

  /**
   * Maps a [User] object to a [UserPrincipal] object.
   *
   * @param user the User entity object
   * @return the mapped UserPrincipal object
   */
  @Mapping(target = "authorities", source = "roles", qualifiedByName = ["mapAuthorities"])
  fun toUserPrincipal(user: User): UserPrincipal

  /**
   * This method is exposed as a static Java method via [JvmStatic] so that
   * MapStruct can invoke it during mapping.
   */
  companion object {

    /**
     * Maps a collection of [UserRole] enums to a collection of Spring Security
     * [GrantedAuthority] objects.
     *
     * @param roles the collection of roles associated with a user
     * @return a collection of granted authorities derived from the roles
     */
    @JvmStatic
    @Named("mapAuthorities")
    fun mapAuthorities(roles: Collection<UserRole>): Collection<GrantedAuthority> {
      return roles.map { SimpleGrantedAuthority(it.name) }
    }
  }
}