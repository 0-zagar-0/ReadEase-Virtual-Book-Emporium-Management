package book.store.service.cartitem;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.exception.EntityNotFoundException;
import book.store.model.Book;
import book.store.model.CartItem;
import book.store.repository.book.BookRepository;
import book.store.repository.cartitem.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem createCartItem(CartItemRequestDto request) {
        Book book = bookRepository.findById(request.getBookId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + request.getBookId())
        );
        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(request.getQuantity());
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem updateQuantityById(Long id, int quantity) {
        CartItem cartItem = cartItemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find cart item by id: " + id)
        );
        cartItem.setId(id);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
}
