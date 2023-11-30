package book.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.store.dto.book.BookDtoWithoutCategoryIds;
import book.store.dto.category.CategoryRequestDto;
import book.store.dto.category.CategoryResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
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
class CategoryControllerTest {
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
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/categories/add-three-category-to-categories-table.sql"
                    )
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
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/categories/delete-all-categories-from-categories-table.sql"
                    )
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/categories/delete-new-category-from-categories-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create category should return category")
    public void createCategory_ShouldReturnCategory() throws Exception {
        // Given
        CategoryRequestDto request = new CategoryRequestDto();
        request.setName("New Category");
        request.setDescription("New Description");

        // When
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        CategoryResponseDto expexted = new CategoryResponseDto(
                4L,
                request.getName(),
                request.getDescription()
        );
        CategoryResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryResponseDto.class
        );
        EqualsBuilder.reflectionEquals(expexted, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get category by valid id should return category")
    public void getCategory_ByValidId_ShouldReturnCategory() throws Exception {
        // Given
        CategoryResponseDto expected = new CategoryResponseDto(2L, "Category 2", "Description 2");

        // When
        MvcResult result = getResultFromGetRequest(expected.id(), status().isOk());

        // Then
        CategoryResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryResponseDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get category by invalid id should return exception")
    public void getCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;

        // When
        MvcResult result = getResultFromGetRequest(id, status().isConflict());

        // Then
        String expected = "Can't find category by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get all categories from database")
    public void getAll_GivenCategoriesFromDatabase_ShouldReturnAllCategories() throws Exception {
        // Given
        List<CategoryResponseDto> expected = createListCategories();

        // When
        MvcResult result = mockMvc.perform(
                        get("/categories")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<CategoryResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<CategoryResponseDto>>() {}
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/categories/restore-category-to-last-state.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update category by valid id should return updated category")
    public void updateCategory_ByValidId_ShouldReturnUpdatedCategory() throws Exception {
        // Given
        Long id = 2L;
        CategoryRequestDto requestDto = createCategoryRequest();

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = getResultFromPutRequest(id, status().isOk(), jsonRequest);

        // Then
        CategoryResponseDto expected = new CategoryResponseDto(
                id,
                requestDto.getName(),
                requestDto.getDescription()
        );

        CategoryResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryResponseDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Update category by invalid id should return exception")
    public void updateCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;
        CategoryRequestDto requestDto = createCategoryRequest();

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = getResultFromPutRequest(id, status().isConflict(), jsonRequest);

        // Then
        String expected = "It is not possible to update a category by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Sql(scripts = "classpath:database/categories/add-poem-category-to-categorise-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Delete category by valid id should return noting")
    public void deleteCategory_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long id = 4L;

        // When
        MvcResult result = getResultFromDeleteRequest(id, status().isNoContent());

        // Then
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Delete category by invalid id should return exception")
    public void deleteCategory_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;

        // When
        MvcResult result = getResultFromDeleteRequest(id, status().isConflict());

        // Then
        String expected = "It is not possible to delete a category by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @Sql(scripts = {"classpath:database/categories/add-three-books-to-books-table.sql",
        "classpath:database/categories/add-bookid-and-categoryid-to-books-categories-table.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/categories/delete-all-ids-from-books-categories-table.sql",
            "classpath:database/categories/delete-all-books-from-books-table.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get book without categories ids by valid category id should return all books "
            + "with category id ")
    public void getBooks_ByValidCategoryId_ShouldReturnAllBooksWithoutCategoriesIds()
            throws Exception {
        // Given
        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(createBookWithoutCategories(1L, "Title 1", "111111", BigDecimal.valueOf(15)));
        expected.add(createBookWithoutCategories(2L, "Title 2", "222222", BigDecimal.valueOf(15)));
        Long categoryId = 1L;

        // When
        MvcResult result = getResultWithBookFromGetRequest(categoryId, status().isOk());

        // Then
        List<BookDtoWithoutCategoryIds> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookDtoWithoutCategoryIds>>() {}
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Get books without categories ids by invalid category id should return exception")
    public void getBooks_ByInvalidCategoryId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;

        // When
        MvcResult result = getResultWithBookFromGetRequest(id, status().isConflict());

        // Then
        String expected = "Can't find category by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    private BookDtoWithoutCategoryIds createBookWithoutCategories(
            Long id,
            String title,
            String isbn,
            BigDecimal price
    ) {
        return new BookDtoWithoutCategoryIds(
                id,
                title,
                "Author",
                isbn,
                price,
                "Description",
                "Image"
        );
    }

    private MvcResult getResultWithBookFromGetRequest(Long id, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        get("/categories/{id}/books", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private MvcResult getResultFromGetRequest(Long id, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        get("/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private MvcResult getResultFromPutRequest(Long id, ResultMatcher matcher, String request)
            throws Exception {
        return mockMvc.perform(
                        put("/categories/{id}", id)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromDeleteRequest(Long id, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        delete("/categories/{id}", id))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private List<CategoryResponseDto> createListCategories() {
        List<CategoryResponseDto> categories = new ArrayList<>();
        categories.add(new CategoryResponseDto(1L, "Category 1", "Description 1"));
        categories.add(new CategoryResponseDto(2L, "Category 2", "Description 2"));
        categories.add(new CategoryResponseDto(3L, "Category 3", "Description 3"));
        return categories;
    }

    private CategoryRequestDto createCategoryRequest() {
        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setName("Lyrics");
        requestDto.setDescription("Lyrics");
        return requestDto;
    }

}
