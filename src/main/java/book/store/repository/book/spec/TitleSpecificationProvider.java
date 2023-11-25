package book.store.repository.book.spec;

import book.store.model.Book;
import book.store.repository.SpecificationProvider;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TitleSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public Specification<Book> getSpecification(String[] params) {
        return ((root, query, criteriaBuilder) -> {
            Predicate[] predicates = Arrays.stream(params)
                    .map(param -> criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + param + "%"))
                    .toArray(Predicate[]::new);
            return criteriaBuilder.or(predicates);
        });
    }

    @Override
    public String getKey() {
        return "title";
    }
}
