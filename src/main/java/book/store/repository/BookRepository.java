package book.store.repository;

import book.store.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);

    Optional<Book> findBookById(Long id);

    List<Book> findAll();
}
