package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.category.CategoryRequestDto;
import book.store.dto.category.CategoryResponseDto;
import book.store.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryResponseDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Category toEntity(CategoryRequestDto request);
}
