package book.store.repository.book;

import book.store.exception.SpecificationNotFoundException;
import book.store.model.Book;
import book.store.repository.SpecificationProvider;
import book.store.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> specificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return specificationProviders.stream()
                .filter(param -> param.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new SpecificationNotFoundException(
                        "Can't find correct specification provider for key: " + key));
    }
}
