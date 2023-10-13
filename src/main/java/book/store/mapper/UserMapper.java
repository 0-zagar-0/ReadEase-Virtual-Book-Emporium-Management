package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.user.response.UserResponseDto;
import book.store.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);
}
