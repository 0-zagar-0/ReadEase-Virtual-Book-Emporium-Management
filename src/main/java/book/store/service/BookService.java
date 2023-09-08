package book.store.service;

import book.store.model.Book;
import java.util.List;

public interface BookService {
    Book save(Book book);

    List<Book> fingAll();
}
