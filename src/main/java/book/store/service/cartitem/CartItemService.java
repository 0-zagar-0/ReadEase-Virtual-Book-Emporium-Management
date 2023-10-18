package book.store.service.cartitem;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.model.CartItem;

public interface CartItemService {
    CartItem createCartItem(CartItemRequestDto request);

    CartItem updateQuantityById(Long id, int quantity);
}
