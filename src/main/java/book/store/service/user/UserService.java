package book.store.service.user;

import book.store.dto.user.request.UserRegisterRequestDto;
import book.store.dto.user.response.UserResponseDto;
import book.store.exception.RegistrationException;
import book.store.model.User;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto request) throws RegistrationException;

    User getAuthenticatedUser();
}
