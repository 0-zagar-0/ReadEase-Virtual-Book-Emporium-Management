package book.store.service.category;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import book.store.dto.book.BookDtoWithoutCategoryIds;
import book.store.dto.category.CategoryRequestDto;
import book.store.dto.category.CategoryResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.BookMapper;
import book.store.mapper.CategoryMapper;
import book.store.model.Book;
import book.store.model.Category;
import book.store.repository.book.BookRepository;
import book.store.repository.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Create category with valid data should return category")
    public void createCategory_WithValidData_ShouldReturnCategory() {
        // Given
        CategoryRequestDto requestDto = createRequestDto("Category 1");
        Category category = createCategory(1L, "Category 1");
        CategoryResponseDto expected = createResponseDto(1L, "Category 1");

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        CategoryResponseDto actual = categoryService.save(requestDto);

        // Then
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
        verify(categoryMapper, times(1)).toEntity(requestDto);
        verify(categoryMapper, times(1)).toDto(category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Get category by valid id should return category")
    public void getCategory_ByValidId_ShouldReturnCategory() {
        // Given
        Long id = 2L;
        Category category = createCategory(2L, "Category 2");
        CategoryResponseDto expected = createResponseDto(2L, "Category 2");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(expected);

        // When
        CategoryResponseDto actual = categoryService.getById(id);

        // Then
        assertEquals(actual, expected);
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @DisplayName("Get category by invalid id should return exception")
    public void getCategory_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 999L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getById(id)
        );

        // Then
        String expected = "Can't find category by id: " + id;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Get all categories should return page with all categories")
    public void getAllCategories_PageWithCategories_ShouldReturnAllCategories() {
        // Given
        Category category1 = createCategory(1L, "Category 1");
        Category category2 = createCategory(2L, "Category 2");
        Category category3 = createCategory(3L, "Category 3");
        CategoryResponseDto responseDto1 = createResponseDto(1L, "Category 1");
        CategoryResponseDto responseDto2 = createResponseDto(2L, "Category 2");
        CategoryResponseDto responseDto3 = createResponseDto(3L, "Category 3");

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category1, category2, category3);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // When
        when(categoryMapper.toDto(category1)).thenReturn(responseDto1);
        when(categoryMapper.toDto(category2)).thenReturn(responseDto2);
        when(categoryMapper.toDto(category3)).thenReturn(responseDto3);

        List<CategoryResponseDto> actual = categoryService.getAll(pageable);
        List<CategoryResponseDto> expected = List.of(responseDto1, responseDto2, responseDto3);

        // Then
        assertEquals(actual, expected);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(3)).toDto(any(Category.class));
    }

    @Test
    @DisplayName("Update category by valid id should return updated category")
    public void updateCategory_ByValidId_ShouldReturnUpdatedCategory() {
        // Given
        Long id = 2L;
        when(categoryRepository.existsById(id)).thenReturn(true);
        CategoryRequestDto request = createRequestDto("New Category");
        request.setDescription("Description");

        Category category = new Category();
        category.setId(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        // When
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        CategoryResponseDto expected = new CategoryResponseDto(
                2L,
                request.getName(),
                request.getDescription()
        );
        when(categoryMapper.toDto(category)).thenReturn(expected);
        CategoryResponseDto actual = categoryService.update(id, request);

        // Then
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(id);
        verify(categoryMapper, times(1)).toEntity(request);
        verify(categoryRepository, times(1)).save(category);
        verify(categoryMapper, times(1)).toDto(category);
    }

    @Test
    @DisplayName("Update category by invalid id should return exception")
    public void updateCategory_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 333L;
        CategoryRequestDto requestDto = createRequestDto("Category 1");
        when(categoryRepository.existsById(id)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.update(id, requestDto)
        );

        // Then
        String expected = "It is not possible to update a category by id: " + id;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Get all books without categories ids by valid category id should return all book")
    public void getAllBook_ByValidCategoryId_ShouldReturnBooksWithoutCategories() {
        // Given
        Category category1 = createCategory(1L, "Category 1");
        Category category2 = createCategory(2L, "Category 2");
        Book book1 = createBook(1L, "Book 1", Set.of(category1, category2));
        Book book2 = createBook(2L, "Book 2", Set.of(category1, category2));
        BookDtoWithoutCategoryIds bookDto1 = createBookWithoutCategories(
                book1.getId(),
                book1.getTitle());
        BookDtoWithoutCategoryIds bookDto2 = createBookWithoutCategories(
                book2.getId(),
                book2.getTitle());
        List<Book> booksFromDatabase = List.of(book1, book2);
        when(categoryRepository.existsById(category1.getId())).thenReturn(true);
        when(bookRepository.findAllByCategoriesId(category1.getId())).thenReturn(booksFromDatabase);

        // When
        List<BookDtoWithoutCategoryIds> expected = List.of(bookDto1, bookDto2);
        when(bookMapper.toDtoWithoutCategories(book1)).thenReturn(bookDto1);
        when(bookMapper.toDtoWithoutCategories(book2)).thenReturn(bookDto2);
        List<BookDtoWithoutCategoryIds> actual =
                categoryService.getBooksByCategoryId(category1.getId());

        //Then
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(category1.getId());
        verify(bookRepository, times(1)).findAllByCategoriesId(category1.getId());
        verify(bookMapper, times(2)).toDtoWithoutCategories(any(Book.class));
    }

    @Test
    @DisplayName("""
            Get all books without categories ids by invalid category id should return exception 
            """)
    public void getAllBook_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 999L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.getBooksByCategoryId(id)
        );

        // Then
        String expected = "Can't find category by id: " + id;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(id);
    }

    @Test
    @DisplayName("Delete category by valid")
    public void deleteCategory_ByValidId() {
        // Given
        Long id = 2L;
        when(categoryRepository.existsById(id)).thenReturn(true);

        // When
        categoryService.deleteById(id);

        // Then
        assertDoesNotThrow(() -> categoryService.deleteById(id));
        verify(categoryRepository, times(2)).existsById(id);
        verify(categoryRepository, times(2)).deleteById(id);
    }

    @Test
    @DisplayName("Delete category by invalid id should return exception")
    public void deleteCategory_ByInvalidId_ShouldReturnException() {
        // Given
        Long id = 55L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        // When
        Exception exception = assertThrows(
                EntityNotFoundException.class,
                () -> categoryService.deleteById(id)
        );

        // Then
        String expected = "It is not possible to delete a category by id: " + id;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).existsById(id);
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Avada kedavra");
        return category;
    }

    private CategoryRequestDto createRequestDto(String name) {
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName(name);
        request.setDescription("Avada kedavra");
        return request;
    }

    private CategoryResponseDto createResponseDto(Long id, String name) {
        return new CategoryResponseDto(id, name, "Avada kedavra");
    }

    private Book createBook(Long id, String name, Set<Category> categories) {
        Book book = new Book();
        book.setId(id);
        book.setAuthor(name);
        book.setTitle("Kobzar");
        book.setDescription("Avada kedavra");
        book.setIsbn("123456");
        book.setPrice(BigDecimal.valueOf(15));
        book.setCoverImage("asdada");
        book.setCategories(categories);
        return book;
    }

    private BookDtoWithoutCategoryIds createBookWithoutCategories(Long id, String title) {
        BookDtoWithoutCategoryIds book = new BookDtoWithoutCategoryIds(
                id,
                title,
                "Author",
                "123456789",
                BigDecimal.valueOf(30),
                "Description",
                "Cover image"
        );
        return book;
    }

}
