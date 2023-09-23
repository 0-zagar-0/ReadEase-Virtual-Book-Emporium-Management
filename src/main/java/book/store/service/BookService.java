package book.store.service;

import book.store.dto.BookDto;
import book.store.dto.BookSearchParametersDto;
import book.store.dto.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto book);

    BookDto getBookById(Long id);

    List<BookDto> getAll(Pageable pageable);

    void updateBookById(Long id, CreateBookRequestDto updateDto);

    void deleteBookById(Long id);

    List<BookDto> searchBooks(BookSearchParametersDto parametersDto);
}
