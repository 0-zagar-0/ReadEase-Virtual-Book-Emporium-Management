package book.store.dto.order;

import book.store.dto.orderitem.OrderItemResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderResponseDto(
        Long id,
        Long userId,
        Set<OrderItemResponseDto> orderItems,
        LocalDateTime orderDate,
        BigDecimal total,
        String status
) {
}
