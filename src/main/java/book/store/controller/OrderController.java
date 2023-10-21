package book.store.controller;

import book.store.dto.order.OrderRequestDto;
import book.store.dto.order.OrderResponseDto;
import book.store.dto.order.OrderUpdateStatusDto;
import book.store.dto.orderitem.OrderItemResponseDto;
import book.store.service.order.OrderService;
import book.store.service.orderitem.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order manager", description = "Endpoints for managing orders")
@RestController
@RequestMapping(value = "/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create order", description = "Place An Order")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto placeAnOrder(@RequestBody OrderRequestDto request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all", description = "Get all orders")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getAll(Pageable pageable) {
        return orderService.getAll(pageable);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update status", description = "Update order status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatusById(@PathVariable Long id, @RequestBody OrderUpdateStatusDto request) {
        orderService.updateOrderStatus(id, request);
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all items", description = "Get list items from order id")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderItemResponseDto> getAllOrderItems(Pageable pageable, @PathVariable Long id) {
        return orderItemService.getAllByOrderId(pageable, id);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get item", description = "Get item from order by order id and item id")
    @ResponseStatus(HttpStatus.OK)
    public OrderItemResponseDto getItemByOrderIdAndItemId(@PathVariable Long orderId,
                                                          @PathVariable Long itemId) {
        return orderItemService.getByOrderIdAndItemId(orderId, itemId);
    }
}
