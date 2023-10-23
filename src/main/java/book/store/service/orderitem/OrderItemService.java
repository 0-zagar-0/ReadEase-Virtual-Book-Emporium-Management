package book.store.service.orderitem;

import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.model.CartItem;
import book.store.model.Order;
import book.store.model.OrderItem;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderItemService {
    OrderItem createOrderItemFromCartItemAndOrder(CartItem cartItem, Order order);

    List<OrderItemResponseDto> getAllByOrderId(Pageable pageable, Long orderId);

    OrderItemResponseDto getByOrderIdAndItemId(Long orderId, Long itemId);
}
