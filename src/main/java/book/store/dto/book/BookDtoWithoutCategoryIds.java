package book.store.dto.book;

import java.math.BigDecimal;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public record BookDtoWithoutCategoryIds(
        Long id,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        String description,
        String coverImage
) {
}
