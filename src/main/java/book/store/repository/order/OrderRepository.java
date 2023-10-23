package book.store.repository.order;

import book.store.model.Order;
import book.store.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "user")
    Order save(Order order);

    @EntityGraph(attributePaths = "orderItems")
    List<Order> findAllByUser(Pageable pageable, User user);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> findById(Long id);
}
