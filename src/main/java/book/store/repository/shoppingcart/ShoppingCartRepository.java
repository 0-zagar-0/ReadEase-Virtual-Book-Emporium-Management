package book.store.repository.shoppingcart;

import book.store.model.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @EntityGraph(attributePaths = "cartItems")
    ShoppingCart save(ShoppingCart shoppingCart);

    @EntityGraph(attributePaths = "cartItems")
    Optional<ShoppingCart> findByUserEmail(String email);
}
