package book.store.repository.book;

import book.store.dto.BookSearchParametersDto;
import book.store.model.Book;
import book.store.repository.SpecificationBuilder;
import book.store.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements
        SpecificationBuilder<Book, BookSearchParametersDto> {
    private final SpecificationProviderManager<Book> providerManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = Specification.where(null);

        if (searchParameters.getAuthors() != null && searchParameters.getAuthors().length > 0) {
            specification = specification.and(providerManager.getSpecificationProvider("author")
                    .getSpecification(searchParameters.getAuthors()));
        }

        if (searchParameters.getTitles() != null && searchParameters.getTitles().length > 0) {
            specification = specification.and(providerManager.getSpecificationProvider("title")
                    .getSpecification(searchParameters.getTitles()));
        }

        if (searchParameters.getPrices() != null && searchParameters.getPrices().length > 0) {
            specification = specification.and(providerManager.getSpecificationProvider("price")
                    .getSpecification(searchParameters.getPrices()));
        }
        return specification;
    }
}
