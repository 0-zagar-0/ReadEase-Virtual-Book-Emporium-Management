package book.store.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.user.request.UserRegisterRequestDto;
import book.store.dto.user.response.UserResponseDto;
import book.store.exception.AuthenticationException;
import book.store.exception.EntityNotFoundException;
import book.store.exception.RegistrationException;
import book.store.mapper.UserMapper;
import book.store.model.Role;
import book.store.model.ShoppingCart;
import book.store.model.User;
import book.store.repository.role.RoleRepository;
import book.store.repository.shoppingcart.ShoppingCartRepository;
import book.store.repository.user.UserRepository;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @SneakyThrows
    @DisplayName("Register user by non exists user should return user")
    public void registerUser_ByNonExistsEmail_ShouldReturnUser() {
        // Given
        checkValidRole();
        UserRegisterRequestDto requestDto = createUserRequestDto();
        User user = createUserFromRequest(requestDto);
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        UserResponseDto expected = new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getShippingAddress()
        );

        // When
        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(shoppingCart)).thenReturn(shoppingCart);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        // Then
        UserResponseDto actual = userService.register(requestDto);
        assertEquals(expected, actual);
        verify(roleRepository, times(2)).findByName(Role.RoleName.ROLE_USER);
        verify(userRepository, times(1)).findByEmail(requestDto.getEmail());
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    @DisplayName("Register user exists by email should return exception")
    public void registerUser_ByExistsEmail_ShouldReturnException() {
        // Given
        checkValidRole();
        UserRegisterRequestDto requestDto = createUserRequestDto();
        User user = createUserFromRequest(requestDto);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // When
        Exception exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto)
        );

        // Then
        String expected = "Unable complete registration!";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(roleRepository, times(1)).findByName(Role.RoleName.ROLE_USER);
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Register user by non exists role should return exception")
    public void registerUser_ByNonExistsRole_ShouldReturnException() {
        // Given
        UserRegisterRequestDto requestDto = createUserRequestDto();
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.register(requestDto)
        );

        // Then
        String expected = "Can't find role by role name: " + Role.RoleName.ROLE_USER.name();
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(roleRepository, times(1)).findByName(Role.RoleName.ROLE_USER);
    }

    @Test
    @DisplayName("Get user with authenticated user should return user")
    public void getUser_WithAuthenticatedUser_ShouldReturnUser() {
        // Given
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        checkValidRole();
        User expected = createUserFromRequest(createUserRequestDto());
        when(authentication.getName()).thenReturn(expected.getEmail());

        // When
        when(userRepository.findByEmail(
                authentication.getName())).thenReturn(Optional.of(expected)
        );

        // Then
        User actual = userService.getAuthenticatedUser();
        EqualsBuilder.reflectionEquals(actual, expected);
        verify(roleRepository, times(1)).findByName(Role.RoleName.ROLE_USER);
        verify(authentication, times(2)).getName();
        verify(userRepository, times(1)).findByEmail(authentication.getName());
    }

    @Test
    @DisplayName("Get user with unauthenticated user should return exception")
    public void getUser_WithUnauthenticatedUser_ShouldReturnException() {
        // Given
        Authentication authentication = null;
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        Exception exception = assertThrows(
                AuthenticationException.class,
                () -> userService.getAuthenticatedUser()
        );

        // Then
        String expected = "Unable to find authenticated user";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get user by non exists user email should return exception")
    public void getUser_ByNonExistsUserEmail_ShouldReturnException() {
        // Given
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        checkValidRole();
        User user = createUserFromRequest(createUserRequestDto());
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(authentication.getName())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getAuthenticatedUser()
        );

        // Then
        String expected = "Can't find user by user name: " + authentication.getName();
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(authentication, times(4)).getName();
        verify(userRepository, times(1)).findByEmail(authentication.getName());
    }

    private void checkValidRole() {
        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);
        when(roleRepository.findByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(role));
    }

    private UserRegisterRequestDto createUserRequestDto() {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("User=123456789");
        requestDto.setRepeatPassword("User=123456789");
        requestDto.setFirstName("User");
        requestDto.setLastName("Alison");
        requestDto.setShippingAddress("Shipping address");
        return requestDto;
    }

    private User createUserFromRequest(UserRegisterRequestDto request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setShippingAddress(request.getShippingAddress());
        Role role = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find role by role name: " + Role.RoleName.ROLE_USER
                )
        );
        user.setRoles(Set.of(role));
        return user;
    }
}
