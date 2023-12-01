package book.store.service.cartitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.exception.EntityNotFoundException;
import book.store.model.Book;
import book.store.model.CartItem;
import book.store.model.Category;
import book.store.repository.book.BookRepository;
import book.store.repository.cartitem.CartItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @InjectMocks
    private CartItemServiceImpl cartItemService;

    @Test
    @DisplayName("Create cart item with valid data should return cart item")
    public void createCartItem_WithValidData_ShouldReturnCartItem() {
        // Given
        Book book = createBook(1L, "Title", "123456");
        CartItemRequestDto request = createRequest(book.getId(), 1);
        CartItem expected = createCartItem(1L, book, 1);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        // When
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(expected);

        // Then
        CartItem actual = cartItemService.createCartItem(request);
        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(book.getId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Create cart item with invalid book id should return exception")
    public void createCartItem_WithInvalidBookId_ShouldReturnException() {
        // Given
        CartItemRequestDto requestDto = createRequest(999L, 1);
        when(bookRepository.findById(requestDto.getBookId())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> cartItemService.createCartItem(requestDto)
        );

        // Then
        String expected = "Can't find book by id: " + requestDto.getBookId();
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(requestDto.getBookId());
    }

    @Test
    @DisplayName("Update quantity by valid id should return updated cart item")
    public void updateQuantity_ByValidId_ShouldReturnUpdatedCartItem() {
        // Given
        Book book = createBook(1L, "Title", "321654");
        CartItem expected = createCartItem(1L, book, 1);
        when(cartItemRepository.findById(expected.getId())).thenReturn(Optional.of(expected));

        // When
        expected.setQuantity(3);
        when(cartItemRepository.save(expected)).thenReturn(expected);

        // Then
        CartItem actual = cartItemService.updateQuantityById(1L, 3);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update quantity by invalid id should return exception")
    public void updateQuantity_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 999L;
        when(cartItemRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> cartItemService.updateQuantityById(id, 3)
        );

        // Then
        String expected = "Can't find cart item by id: " + id;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).findById(id);
    }

    private CartItemRequestDto createRequest(Long bookId, int quantity) {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(bookId);
        requestDto.setQuantity(quantity);
        return requestDto;
    }

    private CartItem createCartItem(Long id, Book book, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private Book createBook(Long id, String title, String isbn) {
        Book book1 = new Book();
        book1.setId(id);
        book1.setAuthor("Author");
        book1.setTitle(title);
        book1.setDescription("Avada kedavra");
        book1.setIsbn(isbn);
        book1.setPrice(BigDecimal.valueOf(15));
        book1.setCoverImage("asdada");
        book1.setCategories(getCategories());
        return book1;
    }

    private Set<Category> getCategories() {
        return Set.of(
                createCategory(1L, "Category 1"),
                createCategory(2L, "Category 2"),
                createCategory(3L, "Category 3")
        );
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Description");
        return category;
    }

}
