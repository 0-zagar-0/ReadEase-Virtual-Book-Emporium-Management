package book.store.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequestDto {
    @NotNull
    private String name;
    @NotNull
    private String description;
}
