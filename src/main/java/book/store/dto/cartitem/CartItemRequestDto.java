package book.store.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequestDto {
    @NotNull
    private Long bookId;
    private int quantity;
}
