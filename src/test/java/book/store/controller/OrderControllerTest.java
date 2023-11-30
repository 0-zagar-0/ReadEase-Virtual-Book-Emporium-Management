package book.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.store.dto.order.OrderRequestDto;
import book.store.dto.order.OrderResponseDto;
import book.store.dto.order.OrderUpdateStatusDto;
import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.model.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
class OrderControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
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
            callSqlQueryFromFile(connection, "add-order-to-orders-table.sql");
            callSqlQueryFromFile(connection, "add-order-items-to-order-items-table.sql");
            callSqlQueryFromFile(
                    connection, "add-ids-order-and-order-item-to-orders-order-items-table.sql"
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
            callSqlQueryFromFile(
                    connection, "delete-all-ids-from-orders-order-items-table.sql"
            );
            callSqlQueryFromFile(connection, "delete-all-from-order-items-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-orders-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-carts-cart-items-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-carts-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-cart-items-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-books-table.sql");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @Sql(scripts = {"classpath:database/orders/delete-new-ids-from-orders-order-items-table.sql",
            "classpath:database/orders/delete-new-orders-items-from-order-items-table.sql",
            "classpath:database/orders/delete-new-order-from-orders-table.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create order with valid data should return order")
    public void createOrder_WithValidData_ShouldReturnOrder() throws Exception {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setShippingAddress("shipping Address");

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                post("/orders")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        OrderResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderResponseDto.class
        );
        OrderResponseDto expeected = new OrderResponseDto(
                3L,
                1L,
                createSetOrderItemResponse(7L, 6L),
                actual.orderDate(),
                BigDecimal.valueOf(30),
                Order.Status.PENDING.name()
        );
        assertEquals(expeected, actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get all order from user should return all orders")
    public void getAllOrder_FromUser_ShouldReturnAllOrders() throws Exception {
        // Given
        OrderResponseDto responseDto1 = new OrderResponseDto(
                1L,
                1L,
                createSetOrderItemResponse(1L, 2L),
                LocalDateTime.of(2023, 11,28, 14, 22, 57),
                BigDecimal.valueOf(30),
                Order.Status.PENDING.name()
        );

        Set<OrderItemResponseDto> setOrderItemResponse = createSetOrderItemResponse(3L, 4L);
        setOrderItemResponse.add(new OrderItemResponseDto(5L, 3L, 1));
        OrderResponseDto responseDto2 = new OrderResponseDto(
                2L,
                1L,
                setOrderItemResponse,
                LocalDateTime.of(2023, 11,28, 14, 22, 57),
                BigDecimal.valueOf(45),
                Order.Status.PENDING.name()
        );

        List<OrderResponseDto> expected = List.of(responseDto1, responseDto2);

        // When
        MvcResult result = mockMvc.perform(
                get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<OrderResponseDto> actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        new TypeReference<List<OrderResponseDto>>() {}
                );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @Sql(scripts = "classpath:database/orders/restore-updated-order-to-last-state.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update status by valid id should return status ok")
    public void updateStatus_ByValidId_ShouldReturnNothing() throws Exception {
        // Given
        Long orderId = 1L;
        OrderUpdateStatusDto updateRequest = new OrderUpdateStatusDto();
        updateRequest.setStatus(Order.Status.PROCESSING);

        // When
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);
        MvcResult result = getResultFromPatchRequest(orderId, jsonRequest, status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("Update status by invalid id should return exception")
    public void updateStatus_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long orderId = 999L;
        OrderUpdateStatusDto updateRequest = new OrderUpdateStatusDto();
        updateRequest.setStatus(Order.Status.PROCESSING);

        // When
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);
        MvcResult result = getResultFromPatchRequest(orderId, jsonRequest, status().isConflict());

        // Then
        String expected = "Can't find order by id: " + orderId;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get all order items by valid order id should return all order items")
    public void getAllOrderItems_ByValidOrderId_ShouldReturnAllOrderItems() throws Exception {
        // Given
        Long orderId = 1L;
        Set<OrderItemResponseDto> expected = createSetOrderItemResponse(1L, 2L);

        // When
        MvcResult result = getResultFromGetRequest(orderId, status().isOk());

        // Then
        Set<OrderItemResponseDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                        new TypeReference<Set<OrderItemResponseDto>>() {}
                );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get all order items by invalid order id should return exception")
    public void getAllOrderItems_ByInvalidId_ShouldReturnException() throws Exception {
        // Given
        Long orderId = 999L;

        // When
        MvcResult result = getResultFromGetRequest(orderId, status().isConflict());

        // Then
        String expected = "Can't find order by id: " + orderId;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get order item by valid order id and item id should return order item")
    public void getOrderItem_ByValidOrderIdAndOrderItemId_ShouldReturnOrderItem() throws Exception {
        // Given
        OrderItemResponseDto expected = new OrderItemResponseDto(2L, 2L, 1);
        Long orderId = 1L;

        // When
        MvcResult result = getResultFromGetRequestWithTwoId(
                orderId, expected.id(), status().isOk()
        );

        // Then
        OrderItemResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                        OrderItemResponseDto.class
                );
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = "USER")
    @DisplayName("Get order item by invalid order id and item id should return exception")
    public void getOrderItem_ByInvalidOrderIdAndItemId_ShouldReturnException() throws Exception {
        // Given
        Long orderId = 999L;
        Long orderItemId = 999L;

        // When
        MvcResult result = getResultFromGetRequestWithTwoId(
                orderId, orderItemId, status().isConflict()
        );

        // Then
        String expected = "Can't find order item by order id: " + orderId + ", and item id: "
                + orderItemId;
        boolean actual = result.getResponse().getContentAsString().contains(expected);
        assertTrue(actual);
    }

    private static void callSqlQueryFromFile(Connection connection, String fileName) {
        ScriptUtils.executeSqlScript(
                connection,
                new ClassPathResource("database/orders/beforeAndAfter/" + fileName)
        );
    }

    private MvcResult getResultFromGetRequestWithTwoId(
            Long orderId, Long orderItemId, ResultMatcher matcher
    ) throws Exception {
        return mockMvc.perform(
                get("/orders/{orderId}/items/{orderItem}", orderId, orderItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromGetRequest(Long id, ResultMatcher matcher
    ) throws Exception {
        return mockMvc.perform(
                        get("/orders/{id}/items", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private MvcResult getResultFromPatchRequest(Long id, String request, ResultMatcher matcher
    ) throws Exception {
        return mockMvc.perform(
                        patch("/orders/{id}", id)
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn();
    }

    private Set<OrderItemResponseDto> createSetOrderItemResponse(Long id1, Long id2) {
        Set<OrderItemResponseDto> orderItemResponseDtos = new HashSet<>();
        orderItemResponseDtos.add(new OrderItemResponseDto(id1, 1L, 1));
        orderItemResponseDtos.add(new OrderItemResponseDto(id2, 2L, 1));
        return orderItemResponseDtos;
    }
}
