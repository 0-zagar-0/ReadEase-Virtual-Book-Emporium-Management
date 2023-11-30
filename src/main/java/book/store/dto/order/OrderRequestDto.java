package book.store.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderRequestDto {
    @NotEmpty
    private String shippingAddress;
}
