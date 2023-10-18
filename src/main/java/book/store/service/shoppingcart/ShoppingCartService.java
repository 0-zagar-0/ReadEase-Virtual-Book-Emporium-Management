package book.store.service.shoppingcart;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;

public interface ShoppingCartService {
    ShoppingCartResponseDto addCartItemToShoppingCart(CartItemRequestDto cartItemRequestDto);

    ShoppingCartResponseDto getUserShoppingCart();

    ShoppingCartResponseDto updateQuantityFromCartItemById(Long id, int quantity);

    void deleteBookFromShoppingCartById(Long id);
}
