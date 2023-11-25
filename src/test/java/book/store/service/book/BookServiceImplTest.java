package book.store.service.book;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import book.store.dto.book.BookDto;
import book.store.dto.book.BookSearchParametersDto;
import book.store.dto.book.CreateBookRequestDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.BookMapper;
import book.store.model.Book;
import book.store.model.Category;
import book.store.repository.book.BookRepository;
import book.store.repository.book.BookSpecificationBuilder;
import book.store.repository.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Save book with valid data was returned bookDto when book exists")
    public void saveBook_WithValidData_ShouldReturnBookDto() {
        // Given
        Category categoryOne = createCategory(1L, "CategoryOne");
        Category categoryTwo = createCategory(2L, "CategoryTwo");
        Category categoryThree = createCategory(3L, "CategoryThree");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryOne));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(categoryTwo));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryThree));
        Book book = createBook(1L, "Author", "Title", "123456", getCategories());
        BookDto expected = createBookDto(book);
        CreateBookRequestDto requestDto = createRequestDto();

        // When
        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        // Then
        BookDto actual = bookService.save(requestDto);
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);

        verify(categoryRepository, times(3)).findById(anyLong());
        verify(bookMapper, times(1)).toDto(book);
        verify(bookMapper, times(1)).toEntity(requestDto);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    @DisplayName("Save book with empty categoriesIds should throw Exception")
    public void saveBook_WithEmptyCategoriesIds_ShouldThrowException() {
        // Given
        CreateBookRequestDto requestDto = createRequestDto();
        requestDto.setCategoriesIds(Collections.emptyList());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.save(requestDto)
        );

        // Then
        String expected = "Can't find categories";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get book by valid id should return book")
    public void getBooK_ByValidId_ShouldReturnBook() {
        // Given
        Book book = createBook(1L, "Author", "Title", "123456", getCategories());
        BookDto expected = createBookDto(book);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        // When
        BookDto actual = bookService.getBookById(book.getId());

        // Then
        assertEquals(actual, expected);
        verify(bookRepository, times(1)).findById(book.getId());
        verify(bookMapper, times(1)).toDto(book);
    }

    @Test
    @DisplayName("Get an exception when the book by this index does not exist")
    public void getBook_WithNonExistingBookId_ShouldThrowException() {
        // Given
        Long bookId = 50L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getBookById(bookId)
        );

        // Then
        String expected = "Can't get book by id: " + bookId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Get all books with pageable arguments should return all books from page")
    public void getAll_GetPageWithBookInDatabase_ShouldReturnAllBook() {
        // Given
        Book book1 = createBook(1L, "Author", "Title", "123456", getCategories());
        Book book2 = createBook(2L, "Author2", "Title2", "1234562", getCategories());
        BookDto bookDto1 = createBookDto(book1);
        BookDto bookDto2 = createBookDto(book2);
        List<Book> booksFromRepository = Arrays.asList(book1, book2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(
                booksFromRepository, pageable, booksFromRepository.size()
        );

        // When
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);
        when(bookMapper.toDto(book2)).thenReturn(bookDto2);

        // Then
        List<BookDto> expected = Arrays.asList(bookDto1, bookDto2);
        List<BookDto> actual = bookService.getAll(pageable);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(2)).toDto(any(Book.class));
    }

    @Test
    @DisplayName("Delete a book by invalid id should throw an exception")
    public void deleteBook_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 999L;
        when(bookRepository.existsById(id)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.deleteBookById(id)
        );

        // Then
        String expected = "Can't find book by id: " + id;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Delete book by valid id should return nothing")
    public void deleteBook_ByValidId() {
        // Given
        Book book = createBook(1L, "Author", "Title", "123456", getCategories());
        when(bookRepository.existsById(book.getId())).thenReturn(true);

        // When
        bookService.deleteBookById(book.getId());

        // Then
        assertDoesNotThrow(() -> bookService.deleteBookById(book.getId()));
        verify(bookRepository, times(2)).existsById(book.getId());
        verify(bookRepository, times(2)).deleteById(book.getId());
    }

    @Test
    @DisplayName("Update book by non existing book id should return EntityNotFoundException")
    public void updateBook_WithNonExistingBookId_ShouldReturnException() {
        // Given
        Long bookId = 50L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        CreateBookRequestDto requestDto = createRequestDto();

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.updateBookById(bookId, requestDto)
        );

        // Then
        String expected = "Can't find book by id: " + bookId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("Update book with empty categoriesIds from request should return Exception ")
    public void updateBook_WithEmptyCategoriesIds_ShouldReturnException() {
        // Given
        Book book = createBook(2L, "Author2", "Title2", "1234562", getCategories());
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));

        CreateBookRequestDto requestDto = createRequestDto();
        requestDto.setCategoriesIds(Collections.emptyList());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.updateBookById(2L, requestDto)
        );

        // Then
        String expected = "Can't find categories";
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Update book with valid data should return updated book")
    public void update_WithValidData_ShouldReturnUpdatedBook() {
        // Given
        Book book = createBook(2L, "Author2", "Title2", "1234562", getCategories());
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        CreateBookRequestDto requestDto = createRequestDto();

        // When
        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        book.setId(2L);
        when(bookRepository.save(book)).thenReturn(book);
        BookDto expected = new BookDto(
                book.getId(),
                "Title",
                "Author",
                "123456",
                BigDecimal.valueOf(15),
                "Avada kedavra",
                "asdada",
                getCategoriesIds()
        );
        when(bookMapper.toDto(book)).thenReturn(expected);

        //Then
        BookDto actual = bookService.updateBookById(2L, requestDto);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(2L);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toEntity(requestDto);
        verify(bookMapper, times(1)).toDto(book);
    }

    @Test
    @DisplayName("Search book from valid parameters should return all books with search parameters")
    public void searchBook_WithValidSearchParameters_ShouldReturnAllBook() {
        // Given
        Book book1 = createBook(1L, "Author", "Title", "123456", getCategories());
        Book book2 = createBook(2L, "Author2", "Title2", "1234562", getCategories());
        BookDto bookDto1 = createBookDto(book1);
        BookDto bookDto2 = createBookDto(book2);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> expectedFromRepository = Arrays.asList(book1, book2);
        Page<Book> expectedBookPage = new PageImpl<>(
                expectedFromRepository, pageable, expectedFromRepository.size()
        );

        BookSearchParametersDto bookSearchParametersDto = getBookSearchParametersDto();
        Specification<Book> specification = bookSpecificationBuilder.build(bookSearchParametersDto);
        // When
        when(bookRepository.findAll(specification, pageable)).thenReturn(expectedBookPage);
        when(bookMapper.toDto(expectedBookPage.getContent().get(0))).thenReturn(bookDto1);
        when(bookMapper.toDto(expectedBookPage.getContent().get(1))).thenReturn(bookDto2);

        // Then
        List<BookDto> expected = Arrays.asList(bookDto1, bookDto2);
        List<BookDto> actual = bookService.searchBooks(bookSearchParametersDto, pageable);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findAll(specification, pageable);
        verify(bookMapper, times(2)).toDto(any(Book.class));
    }

    @Test
    @DisplayName("Search book from invalid parameters should return empty list")
    public void searchBook_WithInvalidSearchParameters_ShouldReturnEmptyList() {
        // Given
        BookSearchParametersDto bookSearchParametersDto = getBookSearchParametersDto();
        Specification<Book> specification = bookSpecificationBuilder.build(bookSearchParametersDto);
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> bookFromDatabase = Collections.emptyList();
        Page<Book> pageWithBook = new PageImpl<>(
                bookFromDatabase, pageable, bookFromDatabase.size()
        );

        // When
        when(bookRepository.findAll(specification, pageable)).thenReturn(pageWithBook);

        // Then
        List<BookDto> expected = Collections.emptyList();
        List<BookDto> actual = bookService.searchBooks(bookSearchParametersDto, pageable);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findAll(specification, pageable);
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Avada kedavra");
        return category;
    }

    private CreateBookRequestDto createRequestDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setAuthor("Author");
        requestDto.setTitle("Kobzar");
        requestDto.setDescription("Avada kedavra");
        requestDto.setIsbn("123456");
        requestDto.setPrice(BigDecimal.valueOf(15));
        requestDto.setCoverImage("asdada");
        requestDto.setCategoriesIds(getCategoriesIds());
        return requestDto;
    }

    private Book createBook(
            Long id,
            String author,
            String title,
            String isbn,
            Set<Category> categories
    ) {
        Book book1 = new Book();
        book1.setId(id);
        book1.setAuthor(author);
        book1.setTitle(title);
        book1.setDescription("Avada kedavra");
        book1.setIsbn(isbn);
        book1.setPrice(BigDecimal.valueOf(15));
        book1.setCoverImage("asdada");
        book1.setCategories(categories);
        return book1;
    }

    private BookDto createBookDto(Book book) {
        BookDto bookDto = new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                book.getCoverImage(),
                getCategoriesIds()
                );
        return bookDto;
    }

    private Set<Category> getCategories() {
        return Set.of(
                createCategory(1L, "CategoryOne"),
                createCategory(2L, "CategoryTwo"),
                createCategory(3L, "CategoryThree")
        );
    }

    private List<Long> getCategoriesIds() {
        return List.of(1L, 2L, 3L);
    }

    private BookSearchParametersDto getBookSearchParametersDto() {
        String[] titles = new String[] {"Kobzar"};
        String[] authors = new String[] {"Author"};
        String[] prices = new String[] {"15"};
        return new BookSearchParametersDto(titles, authors, prices);
    }

}
