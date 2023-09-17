package book.store.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public interface SpecificationProvider<T> {
    Specification<T> getSpecification(String[] params);

    String getKey();
}
