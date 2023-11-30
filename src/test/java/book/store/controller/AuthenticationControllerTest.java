package book.store.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import book.store.dto.user.request.UserLoginRequestDto;
import book.store.dto.user.request.UserRegisterRequestDto;
import book.store.dto.user.response.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp(@Autowired DataSource dataSource,
                      @Autowired WebApplicationContext webApplicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            callSqlQueryFromFile(connection, "add-role-to-roles-table.sql");
            callSqlQueryFromFile(connection, "add-user-to-users-table.sql");
            callSqlQueryFromFile(connection, "add-user-and-role-ids-to-users-roles-table.sql");
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
            callSqlQueryFromFile(connection, "delete-all-from-carts-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-roles-table.sql");
            callSqlQueryFromFile(connection, "delete-all-from-users-table.sql");
        }
    }

    @Test
    @DisplayName("Register user should return user")
    public void registerUser_ShouldReturnUser() throws Exception {
        // Given
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setEmail("newuser@example.com");
        requestDto.setPassword("Sanekkk=123456789");
        requestDto.setRepeatPassword("Sanekkk=123456789");
        requestDto.setFirstName("Alice");
        requestDto.setLastName("Alison");
        requestDto.setShippingAddress("1-st Awenye");

        UserResponseDto expected = new UserResponseDto(
                3L,
                requestDto.getEmail(),
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getShippingAddress()
                );

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                post("/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Login user should return bearer token")
    public void loginUser_ShouldReturnBearerToken() throws Exception {
        // Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "user1@example.com",
                "Sanek=123456789"
        );

        // When
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(
                post("/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String expected = ".*\"token\":\".*\\..*\\..*\".*";
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.matches(expected));
    }

    private static void callSqlQueryFromFile(Connection connection, String fileName) {
        ScriptUtils.executeSqlScript(
                connection,
                new ClassPathResource("database/users/" + fileName)
        );
    }

}
