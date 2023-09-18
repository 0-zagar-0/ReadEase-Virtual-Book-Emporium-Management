package book.store.repository.book;

import book.store.model.Book;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.title = :title, b.author = :author, b.isbn = :isbn, "
            + "b.price = :price, b.description = :description, b.coverImage = :coverImage "
            + "WHERE b.id = :id")
    int updateBookByIdAndTitleAndAuthorAndIsbnAndPriceAndDescriptionAndCoverImage(Long id,
                                                                              String title,
                                                                              String author,
                                                                              String isbn,
                                                                              BigDecimal price,
                                                                              String description,
                                                                              String coverImage);
}
