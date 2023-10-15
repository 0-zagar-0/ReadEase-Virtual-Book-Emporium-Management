package book.store.repository.book;

import book.store.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @EntityGraph(attributePaths = "categories")
    List<Book> findAllByCategoriesId(@Param("id") Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Book> findById(Long id);

    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(Specification<Book> specification, Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    Book save(Book book);
}
