package book.store.service.book;

import book.store.dto.book.BookDto;
import book.store.dto.book.BookSearchParametersDto;
import book.store.dto.book.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto book);

    BookDto getBookById(Long id);

    List<BookDto> getAll(Pageable pageable);

    BookDto updateBookById(Long id, CreateBookRequestDto updateDto);

    void deleteBookById(Long id);

    List<BookDto> searchBooks(BookSearchParametersDto parametersDto, Pageable pageable);
}
