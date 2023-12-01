package book.store.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.order.OrderRequestDto;
import book.store.dto.order.OrderResponseDto;
import book.store.dto.order.OrderUpdateStatusDto;
import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.OrderMapper;
import book.store.model.Book;
import book.store.model.CartItem;
import book.store.model.Order;
import book.store.model.OrderItem;
import book.store.model.Role;
import book.store.model.ShoppingCart;
import book.store.model.User;
import book.store.repository.order.OrderRepository;
import book.store.service.orderitem.OrderItemService;
import book.store.service.shoppingcart.ShoppingCartService;
import book.store.service.user.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Create order with valid data should return order")
    public void createOrder_WithValidData_ShouldReturnOrder() {
        // Given
        Set<CartItem> cartItems = createSetCartItems();
        ShoppingCart shoppingCart = createShoppingCart(cartItems);

        Order order = createOrder(1L, BigDecimal.valueOf(30));
        Set<OrderItem> orderItems = createSetOrderItems(order);
        order.setOrderItems(orderItems);

        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setShippingAddress(order.getShippingAddress());

        OrderResponseDto expected = createOrderResponse(
                createSetOrderItemResponseDtos(),
                order.getOrderDate(),
                order.getTotal(),
                order.getStatus()
        );

        // When
        when(shoppingCartService.getShoppingCart()).thenReturn(shoppingCart);
        when(orderMapper.toEntity(requestDto, shoppingCart)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(expected);

        // Then
        OrderResponseDto actual = orderService.createOrder(requestDto);
        assertEquals(expected, actual);
        verify(shoppingCartService, times(1)).getShoppingCart();
        verify(orderMapper, times(1)).toDto(order);
        verify(orderMapper, times(1)).toEntity(requestDto, shoppingCart);
        verify(orderRepository, times(2)).save(order);
    }

    @Test
    @DisplayName("Get all order from user should return all orders")
    public void getAllOrder_FromUser_ShouldReturnAllOrders() {
        // Given
        Order order1 = createOrder(1L, BigDecimal.valueOf(30));
        order1.setOrderItems(createSetOrderItems(order1));

        Order order2 = createOrder(2L, BigDecimal.valueOf(45));
        Set<OrderItem> orderItems2 = createSetOrderItems(order1);
        Book book = createBook(3L, "Title 3", "333333");
        orderItems2.add(createOrderItem(3L, book, BigDecimal.valueOf(15), order2, 1));
        order2.setOrderItems(orderItems2);

        List<Order> orders = List.of(order1, order2);
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "user@example.com", "123456789");

        Set<OrderItemResponseDto> orderItemResponseDtos1 = createSetOrderItemResponseDtos();
        Set<OrderItemResponseDto> orderItemResponseDtos2 = createSetOrderItemResponseDtos();
        orderItemResponseDtos2.add(new OrderItemResponseDto(3L, 3L, 1));

        OrderResponseDto orderResponseDto1 = createOrderResponse(
                orderItemResponseDtos1,
                order1.getOrderDate(),
                order1.getTotal(),
                order1.getStatus()
        );

        OrderResponseDto orderResponseDto2 = createOrderResponse(
                orderItemResponseDtos2,
                order2.getOrderDate(),
                order2.getTotal(),
                order2.getStatus());

        // When
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(orderRepository.findAllByUser(pageable, user)).thenReturn(orders);
        when(orderMapper.toDto(order1)).thenReturn(orderResponseDto1);
        when(orderMapper.toDto(order2)).thenReturn(orderResponseDto2);

        // Then
        List<OrderResponseDto> expected = List.of(orderResponseDto1, orderResponseDto2);
        List<OrderResponseDto> actual = orderService.getAll(pageable);
        assertEquals(expected, actual);
        verify(userService, times(1)).getAuthenticatedUser();
        verify(orderRepository, times(1)).findAllByUser(pageable, user);
        verify(orderMapper, times(2)).toDto(any(Order.class));
    }

    @Test
    @DisplayName("Update order status by valid order id should return nothing")
    public void updateOrderStatus_ByValidOrderId_ShouldReturnNothing() {
        // Given
        Order expected = createOrder(1L, BigDecimal.valueOf(30));
        expected.setOrderItems(createSetOrderItems(expected));
        when(orderRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        OrderUpdateStatusDto requestUpdate = new OrderUpdateStatusDto();
        requestUpdate.setStatus(Order.Status.PROCESSING);
        expected.setStatus(requestUpdate.getStatus());

        // When
        orderService.updateOrderStatus(expected.getId(), requestUpdate);

        // Then
        verify(orderRepository, times(1)).save(expected);
    }

    @Test
    @DisplayName("Update order status by invalid order id should return exception")
    public void updateOrderStatus_ByInvalidId_ShouldReturnException() {
        // Given
        Long orderId = 999L;
        OrderUpdateStatusDto requestUpdate = new OrderUpdateStatusDto();

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, requestUpdate)
        );

        // Then
        String expected = "Can't find order by id: " + orderId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    private OrderResponseDto createOrderResponse(
            Set<OrderItemResponseDto> orderItems,
            LocalDateTime dateTime,
            BigDecimal total,
            Order.Status status
    ) {
        return new OrderResponseDto(
                1L,
                1L,
                orderItems,
                dateTime,
                total,
                status.name()
        );
    }

    private Set<OrderItemResponseDto> createSetOrderItemResponseDtos() {
        Set<OrderItemResponseDto> setOrderItemsResponse = new HashSet<>();
        setOrderItemsResponse.add(new OrderItemResponseDto(1L, 1L, 1));
        setOrderItemsResponse.add(new OrderItemResponseDto(2L, 2L, 1));
        return setOrderItemsResponse;
    }

    private Order createOrder(Long id, BigDecimal price) {
        User user = createUser(1L, "user@example.com", "123456789");
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderItems(Collections.emptySet());
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(price);
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress("Address");
        return order;
    }

    private ShoppingCart createShoppingCart(Set<CartItem> cartItems) {
        User user = createUser(1L, "user@example.ocm", "123456789");
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setUser(user);
        shoppingCart.setCartItems(cartItems);
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

    private Set<CartItem> createSetCartItems() {
        Book book1 = createBook(1L, "Title 1", "111111");
        Book book2 = createBook(2L, "Title 2", "222222");
        Set<CartItem> cartItems = new HashSet<>();
        cartItems.add(createCartItem(1L, book1, 1));
        cartItems.add(createCartItem(2L, book2, 1));
        return cartItems;
    }

    private Set<OrderItem> createSetOrderItems(Order order) {
        Book book1 = createBook(1L, "Title 1", "111111");
        Book book2 = createBook(2L, "Title 2", "222222");
        Set<OrderItem> orderItems = new HashSet<>();
        orderItems.add(createOrderItem(1L, book1, BigDecimal.valueOf(15), order, 1));
        orderItems.add(createOrderItem(2L, book2, BigDecimal.valueOf(15), order, 1));
        return orderItems;
    }

    private CartItem createCartItem(Long id, Book book, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private OrderItem createOrderItem(
            Long id, Book book, BigDecimal price, Order order, int quantity
    ) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setBook(book);
        orderItem.setPrice(price);
        orderItem.setOrder(order);
        orderItem.setQuantity(quantity);
        return orderItem;
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
