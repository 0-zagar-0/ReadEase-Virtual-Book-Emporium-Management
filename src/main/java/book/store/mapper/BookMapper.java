package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.book.BookDto;
import book.store.dto.book.BookDtoWithoutCategoryIds;
import book.store.dto.book.CreateBookRequestDto;
import book.store.model.Book;
import book.store.model.Category;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    @Mapping(target = "categoriesIds", source = "categories", qualifiedByName = "setCategoryId")
    BookDto toDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Book toEntity(CreateBookRequestDto requestDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @Named("setCategoryId")
    default List<Long> setCategoryId(Set<Category> categories) {
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
    }
}
