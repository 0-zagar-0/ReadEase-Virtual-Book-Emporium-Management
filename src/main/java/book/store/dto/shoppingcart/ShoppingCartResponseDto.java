package book.store.dto.shoppingcart;

import book.store.dto.cartitem.CartItemResponseDto;
import java.util.Set;

public record ShoppingCartResponseDto(
        Long id,
        Long userId,
        Set<CartItemResponseDto> cartItems
) {
}
