package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.domain.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Dedicated mapper for the currently authenticated user. Unlike [UserMapper.toUserDto],
 * the result includes the user's email. Kept in its own [Mapper] interface so MapStruct
 * never considers it as an implicit mapping candidate for [User] → [UserDto].
 */
@Mapper(componentModel = "spring")
interface CurrentUserMapper {

  @Mapping(target = "avatarUrl", source = "avatar.url")
  fun toCurrentUserDto(user: User): UserDto
}
