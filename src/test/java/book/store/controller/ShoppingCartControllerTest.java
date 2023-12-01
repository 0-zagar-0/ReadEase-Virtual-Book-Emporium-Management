package book.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.dto.cartitem.CartItemResponseDto;
import book.store.dto.cartitem.CartItemUpdateRequestDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext webApplicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            callSqlQueryFromFile(connection, "add-four-book-to-books-table.sql");
            callSqlQueryFromFile(connection, "add-two-cart-item-to-cart-items-table.sql");
            callSqlQueryFromFile(connection, "add-role-to-roles-table.sql");
            callSqlQueryFromFile(connection, "add-user-to-users-table.sql");
            callSqlQueryFromFile(connection, "add-user-and-role-ids-to-users-roles-table.sql");
            callSqlQueryFromFile(connection, "add-cart-to-carts-table.sql");
            callSqlQueryFromFile(connection,
                    "add-cart-and-cart-item-ids-to-carts-cart-items-table.sql");
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
            callSqlQueryFromFile(connection, "delete-all-from-carts-cart-items-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-carts-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-cart-items-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-books-table.sql");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @Sql(scripts = {"classpath:database/shoppingCarts/"
            + "delete-carts-id-and-cart-item-id-from-carts-cart-items-table.sql",
            "classpath:database/shoppingCarts/delete-new-cart-item-from-cart-items-table.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Add cart item with valid data should return shopping cart")
    public void addCartItem_WithValidData_ShouldReturnShoppingCart() throws Exception {
        // Given
        Set<CartItemResponseDto> cartItems = createSetCartItems();
        cartItems.add(new CartItemResponseDto(3L, 3L, "Title 3", 1));
        CartItemRequestDto request = createCartItemRequest(3L, 1);

        // When
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = getResultFromPostRequest(jsonRequest, status().isCreated());

        // Then
        ShoppingCartResponseDto expected = createResponseCart(cartItems);
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ShoppingCartResponseDto.class);

        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Add cart item with invalid book id should return exception")
    public void addCartItem_WithInvalidBookId_ShouldReturnException() throws Exception {
        // Given
        CartItemRequestDto request = createCartItemRequest(999L, 1);

        // When
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = getResultFromPostRequest(jsonRequest, status().isConflict());

        // Then
        String expected = "Can't find book by id: " + request.getBookId();
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get user's shopping cart")
    public void getShoppingCart_FromUser_ShouldReturnShoppingCart() throws Exception {
        // Given
        ShoppingCartResponseDto expected = createResponseCart(createSetCartItems());

        // When
        MvcResult result = getResultFromGetRequest(status().isOk());

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ShoppingCartResponseDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("Get shopping cart from user without role user should return status Forbidden")
    public void getShoppingCart_FromUserWithoutUserRole_ShouldReturnStatus() throws Exception {
        getResultFromGetRequest(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @Sql(scripts = "classpath:database/shoppingCarts/restore-cart-item-to-last-state.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update quantity by valid cart item id should return updated shopping cart")
    public void updateQuantity_ByValidCartItemId_ShouldReturnUpdatedShoppingCart()
            throws Exception {
        // Given
        Set<CartItemResponseDto> setCartItems = Set.of(
                new CartItemResponseDto(1L, 1L, "Title 1", 1),
                new CartItemResponseDto(2L, 2L, "Title 2", 3));
        ShoppingCartResponseDto expected = createResponseCart(setCartItems);

        // When
        String jsonRequest = objectMapper.writeValueAsString(createUpdateRequest(3));
        MvcResult result = getResultFromPutRequest(2L, jsonRequest, status().isOk());

        // Then
        ShoppingCartResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ShoppingCartResponseDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Update quantity by invalid cart item id should return exception")
    public void updateQuantity_ByInvalidCartItemId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;

        // When
        String jsonRequest = objectMapper.writeValueAsString(createUpdateRequest(3));
        MvcResult result = getResultFromPutRequest(id, jsonRequest, status().isConflict());

        // Then
        String expected = "Can't find cart item by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @Sql(scripts = {"classpath:database/shoppingCarts/add-cart-item-to-cart-items-table.sql",
            "classpath:database/shoppingCarts/"
                    + "add-cart-item-and-cart-ids-to-carts-cart-items-table.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/shoppingCarts/"
            + "delete-carts-id-and-cart-item-id-from-carts-cart-items-table.sql",
            "classpath:database/shoppingCarts/delete-new-cart-item-from-cart-items-table.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete cart item by valid id should return nothing")
    public void deleteCartItem_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long id = 3L;

        // When
        MvcResult result = getResultFromDeleteRequest(id, status().isNoContent());

        // Then
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Delete cart item by invalid id should return exception")
    public void deleteCartItem_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long id = 999L;

        // When
        MvcResult result = getResultFromDeleteRequest(id, status().isConflict());

        // Then
        String expected = "Can't find cart item by id: " + id;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    private static void callSqlQueryFromFile(Connection connection, String fileName) {
        ScriptUtils.executeSqlScript(
                connection,
                new ClassPathResource("database/shoppingCarts/beforeAndAfter/" + fileName)
        );
    }

    private MvcResult getResultFromDeleteRequest(Long id, ResultMatcher matcher)
            throws Exception {
        return mockMvc.perform(
                        delete("/cart/cart-items/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromPutRequest(Long id, String request, ResultMatcher matcher)
            throws Exception {
        return mockMvc.perform(
                put("/cart/cart-items/{id}", id)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromPostRequest(String request, ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                post("/cart")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private MvcResult getResultFromGetRequest(ResultMatcher resultMatcher)
            throws Exception {
        return mockMvc.perform(
                        get("/cart")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andReturn();
    }

    private CartItemUpdateRequestDto createUpdateRequest(int quantity) {
        return new CartItemUpdateRequestDto(quantity);
    }

    private ShoppingCartResponseDto createResponseCart(Set<CartItemResponseDto> cartItems) {
        return new ShoppingCartResponseDto(1L, 1L, cartItems);
    }

    private Set<CartItemResponseDto> createSetCartItems() {
        Set<CartItemResponseDto> cartItems = new HashSet<>();
        cartItems.add(new CartItemResponseDto(1L, 1L, "Title 1", 1));
        cartItems.add(new CartItemResponseDto(2L, 2L, "Title 2", 1));
        return cartItems;
    }

    private CartItemRequestDto createCartItemRequest(Long bookId, int quantity) {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(bookId);
        requestDto.setQuantity(quantity);
        return requestDto;
    }

}
