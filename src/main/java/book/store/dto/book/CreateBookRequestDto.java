package book.store.dto.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateBookRequestDto {
    @NotNull
    private String title;
    @NotNull
    private String author;
    @NotNull
    private String isbn;
    @NotNull
    @Min(0)
    private BigDecimal price;
    private String description;
    private String coverImage;
    @NotNull
    private List<Long> categoriesIds;
}
