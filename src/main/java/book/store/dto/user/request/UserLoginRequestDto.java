package book.store.dto.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotEmpty
        @Size(min = 10, max = 30)
        String email,
        @NotEmpty
        @Size(min = 8, max = 30)
        String password
) {
}
