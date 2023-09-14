package book.store.mapper;

import book.store.config.MapperConfig;
import book.store.dto.BookDto;
import book.store.dto.CreateBookRequestDto;
import book.store.model.Book;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto requestDto);
}
