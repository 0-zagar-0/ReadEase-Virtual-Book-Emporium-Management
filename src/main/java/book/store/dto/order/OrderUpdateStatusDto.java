package book.store.dto.order;

import book.store.model.Order;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderUpdateStatusDto {
    @NotEmpty
    private Order.Status status;
}
