package kitchenpos.application;

import static kitchenpos.Fixture.NOT_EMPTY_TABLE;
import static kitchenpos.Fixture.ORDER_LINE_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private MenuDao menuDao;

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderLineItemDao orderLineItemDao;

    @Mock
    private OrderTableDao tableDao;

    @InjectMocks
    private OrderService orderService;

    @DisplayName("주문 추가")
    @Test
    void create() {
        OrderLineItem orderLineItem = OrderLineItem.builder()
            .menuId(1L)
            .quantity(1)
            .build();

        Order order = Order.builder()
            .orderTableId(3L)
            .orderLineItems(Arrays.asList(orderLineItem))
            .orderedTime(LocalDateTime.now())
            .build();

        List<OrderLineItem> orderLineItems = order.getOrderLineItems();
        List<Long> menuIds = orderLineItems.stream()
            .map(OrderLineItem::getMenuId)
            .collect(Collectors.toList());
        given(menuDao.countByIdIn(menuIds)).willReturn(1L);
        given(tableDao.findById(order.getOrderTableId())).willReturn(Optional.of(NOT_EMPTY_TABLE));

        Order savedOrder = Order.builder()
            .id(1L)
            .orderTableId(NOT_EMPTY_TABLE.getId())
            .orderStatus(OrderStatus.COOKING.name())
            .build();
        given(orderDao.save(order)).willReturn(savedOrder);
        given(orderLineItemDao.save(orderLineItem)).willReturn(ORDER_LINE_ITEM);

        Order createdOrder = orderService.create(order);

        assertAll(
            () -> assertThat(createdOrder.getId()).isNotNull(),
            () -> assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name()),
            () -> assertThat(createdOrder.getOrderLineItems().get(0).getSeq()).isNotNull()
        );
    }
}