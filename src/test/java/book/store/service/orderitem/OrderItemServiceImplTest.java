package book.store.service.orderitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.OrderItemMapper;
import book.store.model.Book;
import book.store.model.CartItem;
import book.store.model.Order;
import book.store.model.OrderItem;
import book.store.model.Role;
import book.store.model.User;
import book.store.repository.orderitem.OrderItemRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
class OrderItemServiceImplTest {
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    @DisplayName("Create order item with valid data should return order item")
    public void createOrderItem_WithValidData_ShouldReturnOrderItem() {
        // Given
        Book book = createBook(1L, "Title", "123456");
        CartItem cartItem = createCartItem(1L, book, 1);
        Order order = createOrder(1L, cartItem.getBook().getPrice());
        OrderItem expected = createOrderItem(
                1L, book, cartItem.getBook().getPrice(), order, cartItem.getQuantity()
        );

        // When
        when(orderItemMapper.toEntity(cartItem, order)).thenReturn(expected);
        when(orderItemRepository.save(expected)).thenReturn(expected);

        // Then
        OrderItem actual = orderItemService.createOrderItemFromCartItemAndOrder(cartItem, order);
        assertEquals(expected, actual);
        verify(orderItemMapper, times(1)).toEntity(cartItem, order);
        verify(orderItemRepository, times(1)).save(expected);
    }

    @Test
    @DisplayName("Get all order items by valid order id should return all order items")
    public void getAllOrderItems_ByValidOrderId_ShouldReturnAllOrderItems() {
        // Given
        Order order = createOrder(1L, BigDecimal.ZERO);
        Book book1 = createBook(1L, "Title 1", "123456");
        Book book2 = createBook(2L, "Title 2", "112233");
        OrderItem orderItem1 = createOrderItem(1L, book1, book1.getPrice(), order, 1);
        OrderItem orderItem2 = createOrderItem(2L, book2, book2.getPrice(), order, 1);
        OrderItemResponseDto responseDto1 = createOrderItemResponse(1L, 1L, 1);
        OrderItemResponseDto responseDto2 = createOrderItemResponse(2L, 2L, 1);
        List<OrderItem> orderItemsFromRepo = List.of(orderItem1, orderItem2);
        Pageable pageable = PageRequest.of(0, 10);
        when(orderItemRepository.existsById(order.getId())).thenReturn(true);

        // When
        when(orderItemRepository.findAllByOrderId(pageable, order.getId()))
                .thenReturn(orderItemsFromRepo);
        when(orderItemMapper.toDto(orderItem1)).thenReturn(responseDto1);
        when(orderItemMapper.toDto(orderItem2)).thenReturn(responseDto2);

        // Then
        List<OrderItemResponseDto> expected = List.of(responseDto1, responseDto2);
        List<OrderItemResponseDto> actual = orderItemService.getAllByOrderId(
                pageable, order.getId()
        );
        assertEquals(expected, actual);
        verify(orderItemRepository, times(1)).existsById(order.getId());
        verify(orderItemRepository, times(1)).findAllByOrderId(pageable, order.getId());
        verify(orderItemMapper, times(2)).toDto(any(OrderItem.class));
    }

    @Test
    @DisplayName("Get all order items by invalid order id should return exception")
    public void getAllOrderItems_ByInvalidOrderId_ShouldReturnException() {
        // Given
        Long orderId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        when(orderItemRepository.existsById(orderId)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderItemService.getAllByOrderId(pageable, orderId)
        );

        // Then
        String expected = "Can't find order by id: " + orderId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(orderItemRepository, times(1)).existsById(orderId);
    }

    @Test
    @DisplayName("Get order item by valid order id and order item id should return order item")
    public void getOrderItem_ByValidOrderIdAndItemId_ShouldReturnOrderItem() {
        // Given
        Book book = createBook(1L, "Title", "123456");
        Order order = createOrder(1L, BigDecimal.valueOf(15));
        OrderItem orderItem = createOrderItem(1L, book, BigDecimal.valueOf(15), order, 1);
        OrderItemResponseDto expected = createOrderItemResponse(1L, 1L, 1);

        // When
        when(orderItemRepository.findByOrderIdAndId(
                order.getId(), orderItem.getId())).thenReturn(Optional.of(orderItem)
        );
        when(orderItemMapper.toDto(orderItem)).thenReturn(expected);

        // Then
        OrderItemResponseDto actual = orderItemService.getByOrderIdAndItemId(
                order.getId(), orderItem.getId()
        );
        assertEquals(expected, actual);
        verify(orderItemRepository, times(1)).findByOrderIdAndId(order.getId(), orderItem.getId());
        verify(orderItemMapper, times(1)).toDto(orderItem);
    }
    
    @Test
    public void getOrderItem_ByInvalidOrderIdAndOrderItemId_ShouldReturnException() {
        // Given
        Long orderId = 999L;
        Long orderItemId = 999L;
        when(orderItemRepository.findByOrderIdAndId(orderId, orderItemId))
                .thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderItemService.getByOrderIdAndItemId(orderId, orderItemId)
        );

        // Then
        String expected = "Can't find order item by order id: " + orderId + ", and item id: "
                + orderItemId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(orderItemRepository, times(1))
                .findByOrderIdAndId(orderId, orderItemId);
    }

    private OrderItemResponseDto createOrderItemResponse(Long id, Long bookId, int quantity) {
        return new OrderItemResponseDto(id, bookId, quantity);
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
