package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.cartitem.CartItemResponseDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;
import book.store.model.CartItem;
import book.store.model.ShoppingCart;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {
    @Mapping(target = "userId", source = "shoppingCart.user.id")
    @Mapping(target = "cartItems",
            source = "shoppingCart.cartItems",
            qualifiedByName = "changeCartItems")
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @Named("changeCartItems")
    default Set<CartItemResponseDto> changeCartItems(Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    return new CartItemResponseDto(
                            cartItem.getId(),
                            cartItem.getBook().getId(),
                            cartItem.getBook().getTitle(),
                            cartItem.getQuantity());
                })
                .collect(Collectors.toSet());
    }
}
