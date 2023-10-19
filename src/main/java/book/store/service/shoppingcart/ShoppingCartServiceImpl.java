package book.store.service.shoppingcart;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.ShoppingCartMapper;
import book.store.model.CartItem;
import book.store.model.ShoppingCart;
import book.store.model.User;
import book.store.repository.cartitem.CartItemRepository;
import book.store.repository.shoppingcart.ShoppingCartRepository;
import book.store.service.cartitem.CartItemService;
import book.store.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemService cartItemService;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserService userService;
    private final CartItemRepository cartItemRepository;

    @Override
    public ShoppingCartResponseDto addCartItemToShoppingCart(
            CartItemRequestDto cartItemRequestDto) {
        CartItem cartItem = cartItemService.createCartItem(cartItemRequestDto);
        ShoppingCart shoppingCart = getShoppingCart();
        shoppingCart.getCartItems().add(cartItem);
        return shoppingCartMapper.toDto(shoppingCartRepository.save(shoppingCart));
    }

    @Override
    public ShoppingCartResponseDto getUserShoppingCart() {
        return shoppingCartMapper.toDto(getShoppingCart());
    }

    @Override
    public ShoppingCartResponseDto updateQuantityFromCartItemById(Long id, int quantity) {
        cartItemService.updateQuantityById(id, quantity);
        return getUserShoppingCart();
    }

    @Override
    public void deleteBookFromShoppingCartById(Long id) {
        if (!cartItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find cart item by id: " + id);
        }

        cartItemRepository.deleteById(id);
    }

    private ShoppingCart getShoppingCart() {
        User user = userService.getAuthenticatedUser();
        return shoppingCartRepository.findByUserEmail(
                user.getEmail()).orElseThrow(() -> new EntityNotFoundException(
                "Can't find shopping cart by user: " + user)
        );
    }
}
