package book.store.controller;

import book.store.dto.cartitem.CartItemRequestDto;
import book.store.dto.cartitem.CartItemUpdateRequestDto;
import book.store.dto.shoppingcart.ShoppingCartResponseDto;
import book.store.service.shoppingcart.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart manager", description = "Endpoints for managing shopping cart")
@RestController
@RequestMapping(value = "/cart")
@RequiredArgsConstructor
@Transactional
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Add book", description = "Add book to shopping cart")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCartResponseDto addBookToShoppingCart(@RequestBody CartItemRequestDto request) {
        return shoppingCartService.addCartItemToShoppingCart(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get shopping cart", description = "Get user's shopping cart")
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartResponseDto getUserShoppingCart() {
        return shoppingCartService.getUserShoppingCart();
    }

    @PutMapping("/cart-items/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update quantity", description = "Update quantity from cart item")
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartResponseDto updateQuantityFromCartItemById(
            @PathVariable Long id,
            @RequestBody CartItemUpdateRequestDto request
    ) {
        return shoppingCartService.updateQuantityFromCartItemById(id, request.quantity());
    }

    @DeleteMapping("/cart-items/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Delete cart item", description = "Delete cart item from shopping cart")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCartItemById(@PathVariable Long id) {
        shoppingCartService.deleteBookFromShoppingCartById(id);
    }
}
