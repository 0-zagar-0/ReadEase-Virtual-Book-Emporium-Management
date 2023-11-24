package book.store.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import book.store.dto.book.BookSearchParametersDto;
import book.store.model.Book;
import book.store.repository.SpecificationBuilder;
import book.store.repository.SpecificationProvider;
import book.store.repository.SpecificationProviderManager;
import book.store.repository.book.spec.AuthorSpecificationProvider;
import book.store.repository.book.spec.PriceSpecificationProvider;
import book.store.repository.book.spec.TitleSpecificationProvider;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    private SpecificationProviderManager<Book> specificationProviderManager;
    private SpecificationBuilder<Book, BookSearchParametersDto> specificationBuilder;

    @Test
    @Sql(scripts = "classpath:database/books/add-three-book-to-books-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/books/delete-all-books-from-books-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find all books by valid parameters should return all book with search parameters")
    void findAllBySearchParams_ValidSearchParams_ShouldReturnAllBookWithParams() {

        Specification<Book> specification = getSpecification(
                new String[] {"Author"},
                new String[] {"Title"},
                new String[] {"15"}
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> actual = bookRepository.findAll(specification, pageable).getContent();
        assertEquals(2, actual.size());
        assertEquals("Title 1", actual.get(0).getTitle());
    }

    @Test
    @Sql(scripts = "classpath:database/books/add-three-book-to-books-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/books/delete-all-books-from-books-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find all books by none search parameters should return all books")
    void findAllBySearchParams_NoneSearchParams_ShouldReturnAllBook() {
        Specification<Book> specification = getSpecification(
                new String[] {},
                new String[] {},
                new String[] {}
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> actual = bookRepository.findAll(specification, pageable).getContent();
        assertEquals(3, actual.size());
    }

    @Test
    @Sql(scripts = "classpath:database/books/add-three-book-to-books-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/books/delete-all-books-from-books-table.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find all books by Invalid parameters should return empty list")
    void findAllBySearchParams_InvalidSearchParams_ShouldReturnEmptyList() {
        Specification<Book> specification = getSpecification(
                new String[] {"none"},
                new String[] {"none"},
                new String[] {"100"}
        );
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> actual = bookRepository.findAll(specification, pageable).getContent();
        assertEquals(Collections.emptyList(), actual);
    }

    @NotNull
    private Specification<Book> getSpecification(
            String[] authors,
            String[] titles,
            String[] prices
    ) {
        List<SpecificationProvider<Book>> specificationProviders = getSpecificationProviders();

        specificationProviderManager = new BookSpecificationProviderManager(specificationProviders);
        specificationBuilder = new BookSpecificationBuilder(specificationProviderManager);
        BookSearchParametersDto searchParametersDto =
                new BookSearchParametersDto(titles, authors, prices);
        return specificationBuilder.build(searchParametersDto);
    }

    @NotNull
    private List<SpecificationProvider<Book>> getSpecificationProviders() {
        SpecificationProvider<Book> authorSpecificationProvider =
                new AuthorSpecificationProvider();
        SpecificationProvider<Book> titleSpecificationProvider = new TitleSpecificationProvider();
        SpecificationProvider<Book> priceSpecificationProvider = new PriceSpecificationProvider();
        List<SpecificationProvider<Book>> specificationProviders = List.of(
                authorSpecificationProvider,
                titleSpecificationProvider,
                priceSpecificationProvider);
        return specificationProviders;
    }

}
