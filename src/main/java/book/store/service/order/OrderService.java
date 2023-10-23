package book.store.service.order;

import book.store.dto.order.OrderRequestDto;
import book.store.dto.order.OrderResponseDto;
import book.store.dto.order.OrderUpdateStatusDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto request);

    List<OrderResponseDto> getAll(Pageable pageable);

    void updateOrderStatus(Long orderId, OrderUpdateStatusDto request);
}
