package book.store.service.category;

import book.store.dto.book.BookDtoWithoutCategoryIds;
import book.store.dto.category.CategoryRequestDto;
import book.store.dto.category.CategoryResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponseDto save(CategoryRequestDto request);

    CategoryResponseDto getById(Long id);

    List<CategoryResponseDto> getAll(Pageable pageable);

    CategoryResponseDto update(Long id, CategoryRequestDto request);

    void deleteById(Long id);

    List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id);
}
