package book.store.service.order;

import book.store.dto.order.OrderRequestDto;
import book.store.dto.order.OrderResponseDto;
import book.store.dto.order.OrderUpdateStatusDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.OrderMapper;
import book.store.model.CartItem;
import book.store.model.Order;
import book.store.model.OrderItem;
import book.store.model.ShoppingCart;
import book.store.model.User;
import book.store.repository.order.OrderRepository;
import book.store.service.orderitem.OrderItemService;
import book.store.service.shoppingcart.ShoppingCartService;
import book.store.service.user.UserService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartService shoppingCartService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final UserService userService;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        ShoppingCart shoppingCart = shoppingCartService.getShoppingCart();
        Order savedOrder = orderRepository.save(orderMapper.toEntity(request, shoppingCart));
        savedOrder.setOrderItems(getOrderItemsFromCartItems(
                shoppingCart.getCartItems(),
                savedOrder)
        );
        return orderMapper.toDto(orderRepository.save(savedOrder));
    }

    @Override
    public List<OrderResponseDto> getAll(Pageable pageable) {
        User user = userService.getAuthenticatedUser();
        return orderRepository.findAllByUser(pageable, user).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderUpdateStatusDto request) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id: " + orderId)
        );
        order.setStatus(request.getStatus());
        orderRepository.save(order);
    }

    private Set<OrderItem> getOrderItemsFromCartItems(Set<CartItem> cartItems, Order order) {
        return cartItems.stream()
                .map(cartItem ->
                        orderItemService.createOrderItemFromCartItemAndOrder(cartItem, order)
                )
                .collect(Collectors.toSet());
    }
}
