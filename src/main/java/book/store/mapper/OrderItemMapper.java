package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.model.CartItem;
import book.store.model.Order;
import book.store.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "bookId", source = "orderItem.book.id")
    OrderItemResponseDto toDto(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "book", source = "cartItem.book")
    @Mapping(target = "order", source = "order")
    @Mapping(target = "price", source = "cartItem.book.price")
    @Mapping(target = "quantity", source = "cartItem.quantity")
    OrderItem toEntity(CartItem cartItem, Order order);
}
