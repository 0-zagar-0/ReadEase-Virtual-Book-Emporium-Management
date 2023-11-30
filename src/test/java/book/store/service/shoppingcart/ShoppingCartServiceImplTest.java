package book.store.service.shoppingcart;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.dto.cartitem.CartItemResponseDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.ShoppingCartMapper;
import book.store.model.Book;
import book.store.model.CartItem;
import book.store.model.Role;
import book.store.model.ShoppingCart;
import book.store.model.User;
import book.store.repository.cartitem.CartItemRepository;
import book.store.repository.shoppingcart.ShoppingCartRepository;
import book.store.service.cartitem.CartItemService;
import book.store.service.user.UserService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private CartItemService cartItemService;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private UserService userService;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private ShoppingCartServiceImpl cartService;

    @Test
    @DisplayName("""
            Add cart item to shopping cart with valid date should return shopping cart
            """)
    public void addCartItem_WithValidData_ShouldReturnShoppingCartWithCartItems() {
        // Given
        Book book = createBook(1L, "Title","112233");
        CartItem cartItem = createCartItem(2L, book, 1);
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(book.getId());
        requestDto.setQuantity(1);

        when(cartItemService.createCartItem(requestDto)).thenReturn(cartItem);

        User user = createUser(1L, "user", "123456");
        ShoppingCart shoppingCart = getShoppingCart(user);

        // When
        shoppingCart.getCartItems().add(cartItem);
        when(shoppingCartRepository.save(shoppingCart)).thenReturn(shoppingCart);

        ShoppingCartResponseDto expected = getResponseCart(shoppingCart);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // Then
        ShoppingCartResponseDto actual = cartService.addCartItemToShoppingCart(requestDto);
        EqualsBuilder.reflectionEquals(expected, actual);
        verify(cartItemService, times(1)).createCartItem(requestDto);
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verify(userService, times(1)).getAuthenticatedUser();
        verify(shoppingCartRepository, times(1)).findByUserEmail(user.getEmail());
    }

    @Test
    @DisplayName("Get user shopping cart")
    public void getShoppingCart_WithUser() {
        // Given
        User user = createUser(1L, "User", "987654");
        ShoppingCart shoppingCart = getShoppingCart(user);
        ShoppingCartResponseDto expected = getResponseCart(shoppingCart);

        // When
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);
        ShoppingCartResponseDto actual = cartService.getUserShoppingCart();

        // Then
        EqualsBuilder.reflectionEquals(expected, actual);
        verify(shoppingCartMapper, times(1)).toDto(shoppingCart);
        verify(userService, times(1)).getAuthenticatedUser();
        verify(shoppingCartRepository, times(1)).findByUserEmail(user.getEmail());
    }

    @Test
    @DisplayName("Update quantity cart item by valid id should return updated shopping cart")
    public void updateQuantity_ByValidId_ShouldReturnUpdatedShoppingCart() {
        // Given
        Book book = createBook(2L, "Book2", "4445566");
        User user = createUser(1L, "User", "987654");
        ShoppingCart shoppingCart = getShoppingCart(user);
        shoppingCart.getCartItems().add(createCartItem(2L, book, 1));

        ShoppingCart expectedShoppingCart = getShoppingCart(user);
        expectedShoppingCart.getCartItems().add(createCartItem(2L, book, 3));
        ShoppingCartResponseDto expected = getResponseCart(expectedShoppingCart);

        // When
        ShoppingCartResponseDto actual = cartService.updateQuantityFromCartItemById(2L, 3);

        // Then
        EqualsBuilder.reflectionEquals(expected, actual);
        verify(userService, times(1)).getAuthenticatedUser();
        verify(shoppingCartRepository, times(1)).findByUserEmail(user.getEmail());
    }

    @Test
    @DisplayName("Delete cart item by valid id should return nothing")
    public void deleteCartItem_ByValidId_ShouldReturnNothing() {
        // Given
        Long id = 1L;
        when(cartItemRepository.existsById(id)).thenReturn(true);

        // When
        cartService.deleteBookFromShoppingCartById(id);

        // Then
        assertDoesNotThrow(() -> cartService.deleteBookFromShoppingCartById(id));
        verify(cartItemRepository, times(2)).existsById(id);
        verify(cartItemRepository, times(2)).deleteById(id);
    }

    @Test
    @DisplayName("Delete cart item by invalid id should return exception")
    public void deleteCartItem_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 999L;
        when(cartItemRepository.existsById(id)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> cartService.deleteBookFromShoppingCartById(id)
        );

        // Then
        String expected = "Can't find cart item by id: " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Get shopping cart without creating shopping cart should return exception")
    public void getShoppingCart_WithoutCreatingShoppingCart_ShouldReturnException() {
        // Given
        User user = createUser(1L, "admin", "123456");
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(shoppingCartRepository.findByUserEmail(user.getEmail())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> cartService.getShoppingCart()
        );

        // Then
        String expected = "Can't find shopping cart by user: " + user;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(userService, times(1)).getAuthenticatedUser();
        verify(shoppingCartRepository, times(1)).findByUserEmail(user.getEmail());
    }

    private ShoppingCartResponseDto getResponseCart(ShoppingCart shoppingCart) {
        return new ShoppingCartResponseDto(
          shoppingCart.getId(),
          shoppingCart.getUser().getId(),
          getCartItems(shoppingCart)
        );
    }

    private Set<CartItemResponseDto> getCartItems(ShoppingCart shoppingCart) {
        return shoppingCart.getCartItems().stream()
                .map(cartItem -> {
                    return new CartItemResponseDto(
                            cartItem.getId(),
                            cartItem.getBook().getId(),
                            cartItem.getBook().getTitle(),
                            cartItem.getQuantity());
                })
                .collect(Collectors.toSet());
    }

    private ShoppingCart getShoppingCart(User user) {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        Book book = createBook(1L, "Title", "123456");

        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(createCartItem(1L, book, 1));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCart.setCartItems(cartItems);

        when(shoppingCartRepository.findByUserEmail(user.getEmail()))
                .thenReturn(Optional.of(shoppingCart));
        return shoppingCart;
    }

    private User createUser(Long id, String emailName, String password) {
        Role userRole = new Role();
        userRole.setName(Role.RoleName.ROLE_USER);

        User user = new User();
        user.setId(id);
        user.setEmail(emailName + "@example.com");
        user.setPassword(password);
        user.setFirstName("First name");
        user.setLastName("Last name");
        user.setShippingAddress("Shipping address");
        user.setRoles(Set.of(userRole));
        return user;
    }

    private CartItem createCartItem(Long id, Book book, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private Book createBook(Long id, String title, String isbn) {
        Book book1 = new Book();
        book1.setId(id);
        book1.setAuthor("Author");
        book1.setTitle(title);
        book1.setDescription("Avada kedavra");
        book1.setIsbn(isbn);
        book1.setPrice(BigDecimal.valueOf(15));
        book1.setCoverImage("asdada");
        book1.setCategories(Collections.emptySet());
        return book1;
    }

}
