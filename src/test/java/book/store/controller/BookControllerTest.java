package book.store.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.store.dto.book.BookDto;
import book.store.dto.book.BookSearchParametersDto;
import book.store.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            callSqlQueryFromFile(connection, "add-three-book-to-books-table.sql");
            callSqlQueryFromFile(connection, "add-two-categories-to-categories-table.sql");
            callSqlQueryFromFile(connection,
                    "add-ids-book-and-category-to-books_categories-table.sql"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            callSqlQueryFromFile(connection, "delete-all-ids-from-books-categories-table.sql");
            callSqlQueryFromFile(connection, "delete-all-categories-from-categories-table.sql");
            callSqlQueryFromFile(connection, "delete-all-books-from-books-table.sql");
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = {"classpath:database/books/delete-new-bookid-for-books-categories-table.sql",
            "classpath:database/books/delete-newBook-book-from-books-table.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new book with valid request")
    void createBook_ValidRequest_Success() throws Exception {
        // Given
        CreateBookRequestDto requestDto = createRequestDto();

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = getResultFromPostRequest(status().isCreated(), jsonRequest);

        // Then
        BookDto expected = createResponseDto(5L, requestDto);
        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookDto.class
        );
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Create a new book with empty categories should return exception")
    void createBook_InvalidRequest_ShouldReturnException() throws Exception {
        // Given
        CreateBookRequestDto requestDto = createRequestDto();
        requestDto.setCategoriesIds(Collections.emptyList());

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = getResultFromPostRequest(status().isConflict(), jsonRequest);

        // Then

        String expected = "Can't find categories";
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        Assertions.assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get all books from database")
    void getAll_GivenBookInDatabase_ShouldReturnAllBooks() throws Exception {
        // Given
        List<BookDto> expected = createExpectedList();

        // When
        MvcResult result = mockMvc.perform(
                get("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<List<BookDto>>() {}
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get book by valid id")
    void getById_ValidId_ShouldReturnBook() throws Exception {
        // Given
        BookDto expected = createBookDto(
                2L, "Title 2", "Author 2", "222222", BigDecimal.valueOf(15)
        );

        // When
        MvcResult result = getResultFromGetRequest(expected.id(), status().isOk());

        // Then
        BookDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BookDto.class
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get book by invalid id should return exception")
    void getById_InvalidId_ShouldReturnException() throws Exception {
        // Given
        Long bookId = 500L;

        // Then
        MvcResult result = getResultFromGetRequest(bookId, status().isConflict());

        // Then
        String expected = "Can't get book by id: " + bookId;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        Assertions.assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/books/add-newBook-to-book-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Delete book by valid id")
    void deleteById_ValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long bookId = 4L;

        // When
        MvcResult result = getResultFromDeleteRequest(bookId, status().isNoContent());

        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Delete book by invalid id should return Exception")
    void deleteById_InvalidId_ShouldReturnException() throws Exception {
        // When
        Long bookId = 999L;

        // When
        MvcResult result = getResultFromDeleteRequest(bookId, status().isConflict());

        // Then
        String expected = "Can't find book by id: " + bookId;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        Assertions.assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/books/restore-book-to-last-state.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update book by id with valid data should return updated book")
    void updateById_WithValidData_ShouldReturnUpdatedBook() throws Exception {
        // Given
        CreateBookRequestDto requestDto = createRequestDto();
        BookDto expected = createResponseDto(1L, requestDto);

        // When
        String requestString = objectMapper.writeValueAsString(requestDto);
        MvcResult result = getResultFromPutRequest(expected.id(), status().isOk(), requestString);

        // Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class
        );
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/books/restore-book-to-last-state.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update book by id with invalid data should return exception")
    void updateById_WithInvalidCategoriesIds_ShouldReturnException() throws Exception {
        // Given
        CreateBookRequestDto createBookRequestDto = createRequestDto();
        createBookRequestDto.setCategoriesIds(Collections.emptyList());

        // When
        String requestString = objectMapper.writeValueAsString(createBookRequestDto);
        MvcResult result = getResultFromPutRequest(1L, status().isConflict(), requestString);

        // Then
        String expected = "Can't find categories";
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        Assertions.assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Update book by invalid id should return exception")
    void updateById_WithInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long bookId = 500L;
        CreateBookRequestDto createBookRequestDto = createRequestDto();

        // When
        String requestString = objectMapper.writeValueAsString(createBookRequestDto);
        MvcResult result = getResultFromPutRequest(bookId, status().isConflict(), requestString);

        // Then
        String expected = "Can't find book by id: " + bookId;
        Assertions.assertTrue(result.getResponse().getContentAsString().contains(expected));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Search books with valid parameters should return book with search parameters")
    void searchBook_WithValidData_ShouldReturnAllBookWithParam() throws Exception {
        // Given
        List<BookDto> expected = createExpectedList();
        expected.remove(2);
        BookSearchParametersDto bookSearchParametersDto = new BookSearchParametersDto(
                        new String[] {"Title"},
                        new String[] {"author"},
                        new String[] {"15"}
        );

        // When
        MvcResult result = getResultFromGetRequestWithSearchParam(bookSearchParametersDto);

        // Then
        String responseContent = result.getResponse().getContentAsString();
        List<BookDto> actual = objectMapper.readValue(
                responseContent,
                new TypeReference<List<BookDto>>() {}
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Search book with invalid parameter should return empty list")
    void searchBook_WithInvalidParameters_ShouldReturnEmptyList() throws Exception {
        // Given
        BookSearchParametersDto bookSearchParametersDto = new BookSearchParametersDto(
                new String[] {"none"},
                new String[] {"none"},
                new String[] {"100"}
        );

        // When
        MvcResult result = getResultFromGetRequestWithSearchParam(bookSearchParametersDto);

        // Then
        List<BookDto> actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<BookDto>>() {}
        );
        Assertions.assertEquals(Collections.emptyList(), actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Search books with out params should return all books")
    void searchBooks_WithEmptyParams_ShouldReturnAllBooks() throws Exception {
        // Given
        List<BookDto> expected = createExpectedList();

        // When
        MvcResult result = mockMvc.perform(get("/books/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        List<BookDto> actual = objectMapper.readValue(
                responseContent,
                new TypeReference<List<BookDto>>() {}
        );
        Assertions.assertEquals(expected, actual);
    }

    private static void callSqlQueryFromFile(Connection connection, String fileName) {
        ScriptUtils.executeSqlScript(
                connection,
                new ClassPathResource("database/books/" + fileName)
        );
    }

    private MvcResult getResultFromGetRequestWithSearchParam(BookSearchParametersDto parametersDto)
            throws Exception {
        return mockMvc.perform(get("/books/search")
                        .param("titles", parametersDto.titles())
                        .param("authors", parametersDto.authors())
                        .param("prices", parametersDto.prices())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult getResultFromPostRequest(ResultMatcher resultMatcher, String request)
            throws Exception {
        return mockMvc.perform(
                        post("/books")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private MvcResult getResultFromGetRequest(Long id, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        get("/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private MvcResult getResultFromPutRequest(Long id, ResultMatcher matcher, String request)
            throws Exception {
        return mockMvc.perform(
                        put("/books/{id}", id)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromDeleteRequest(Long id, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        delete("/books/{id}", id))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private List<BookDto> createExpectedList() {
        List<BookDto> expected = new ArrayList<>();
        expected.add(createBookDto(
                1L, "Title 1", "Author 1", "111111", BigDecimal.valueOf(15))
        );
        expected.add(createBookDto(
                2L, "Title 2", "Author 2", "222222", BigDecimal.valueOf(15))
        );
        expected.add(createBookDto(
                3L, "Title 3", "Author 3", "333333", BigDecimal.valueOf(30))
        );
        return expected;
    }

    private BookDto createResponseDto(Long id, CreateBookRequestDto requestDto) {
        return new BookDto(id,
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getIsbn(),
                requestDto.getPrice(),
                requestDto.getDescription(),
                requestDto.getCoverImage(),
                requestDto.getCategoriesIds()
        );
    }

    private CreateBookRequestDto createRequestDto() {
        return new CreateBookRequestDto()
                .setTitle("New book")
                .setAuthor("New Author")
                .setIsbn("111222333")
                .setPrice(BigDecimal.valueOf(22))
                .setDescription("Description")
                .setCoverImage("Image")
                .setCategoriesIds(List.of(1L, 2L));

    }

    private BookDto createBookDto(
            Long id, String title, String author, String isbn, BigDecimal price
    ) {
        return new BookDto(
                id,
                title,
                author,
                isbn,
                price,
                "Description",
                "Image",
                List.of(2L, 1L));
    }
}
