package book.store.dto.user.request;

import book.store.validation.FieldMatch;
import book.store.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldMatch(field = "password",
        fieldMatch = "repeatPassword",
        message = "Passwords values don't match!")
public class UserRegisterRequestDto {
    @NotBlank
    @Size(min = 10, max = 30)
    @Email
    private String email;
    @NotBlank
    @Size(min = 8, max = 50)
    @Password
    private String password;
    @NotBlank
    @Size(min = 8, max = 50)
    @Password
    private String repeatPassword;
    @NotBlank
    @Size(min = 1, max = 20)
    private String firstName;
    @NotBlank
    @Size(min = 1, max = 20)
    private String lastName;
    @NotBlank
    @Size(min = 4, max = 100)
    private String shippingAddress;
}
