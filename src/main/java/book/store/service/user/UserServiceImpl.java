package book.store.service.user;

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
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    @Transactional
    public UserResponseDto register(UserRegisterRequestDto request) throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Unable complete registration!");
        }

        User user = createUserFromRequest(request);
        User savedUser = userRepository.save(user);
        createShoppingCartForUser(savedUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationException("Unable to find authenticated user");
        }

        return userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find user by user name: " + authentication.getName()
                )
        );
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

    private void createShoppingCartForUser(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setCartItems(new HashSet<>());
        shoppingCartRepository.save(shoppingCart);
    }
}
