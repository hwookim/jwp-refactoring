package kitchenpos.application;

import static kitchenpos.Fixture.MENU1;
import static kitchenpos.Fixture.NOT_EMPTY_TABLE;
import static kitchenpos.Fixture.ORDER1;
import static kitchenpos.Fixture.ORDER2;
import static kitchenpos.Fixture.ORDER_LINE_ITEM;
import static kitchenpos.Fixture.TABLE1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
            .menuId(MENU1.getId())
            .quantity(1)
            .build();

        Order order = Order.builder()
            .orderTableId(NOT_EMPTY_TABLE.getId())
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

    @DisplayName("[예외] 주문 항목이 없는 주문 추가")
    @Test
    void create_Fail_With_LessOrderItem() {
        Order order = Order.builder()
            .orderTableId(NOT_EMPTY_TABLE.getId())
            .orderedTime(LocalDateTime.now())
            .build();

        assertThatThrownBy(() -> orderService.create(order))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 존재하지 않는 메뉴를 포함한 주문 추가")
    @Test
    void create_Fail_With_NotExistMenu() {
        OrderLineItem orderLineItem = OrderLineItem.builder()
            .menuId(100L)
            .quantity(1)
            .build();

        Order order = Order.builder()
            .orderTableId(NOT_EMPTY_TABLE.getId())
            .orderLineItems(Arrays.asList(orderLineItem))
            .orderedTime(LocalDateTime.now())
            .build();

        List<OrderLineItem> orderLineItems = order.getOrderLineItems();
        List<Long> menuIds = orderLineItems.stream()
            .map(OrderLineItem::getMenuId)
            .collect(Collectors.toList());
        given(menuDao.countByIdIn(menuIds)).willReturn(0L);

        assertThatThrownBy(() -> orderService.create(order))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 빈 테이블을 포함한 주문 추가")
    @Test
    void create_Fail_With_EmptyTable() {
        OrderLineItem orderLineItem = OrderLineItem.builder()
            .menuId(MENU1.getId())
            .quantity(1)
            .build();

        Order order = Order.builder()
            .orderTableId(TABLE1.getId())
            .orderLineItems(Arrays.asList(orderLineItem))
            .orderedTime(LocalDateTime.now())
            .build();

        List<OrderLineItem> orderLineItems = order.getOrderLineItems();
        List<Long> menuIds = orderLineItems.stream()
            .map(OrderLineItem::getMenuId)
            .collect(Collectors.toList());
        given(menuDao.countByIdIn(menuIds)).willReturn(1L);
        given(tableDao.findById(order.getOrderTableId())).willReturn(Optional.of(TABLE1));

        assertThatThrownBy(() -> orderService.create(order))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("전체 주문 조회")
    @Test
    void list() {
        given(orderService.list()).willReturn(Arrays.asList(ORDER1, ORDER2));
        List<Order> orders = orderService.list();

        assertThat(orders).hasSize(2);
    }

    @DisplayName("주문 상태 변경")
    @Test
    void changeOrderStatus() {
        Order order = Order.builder()
            .id(3L)
            .orderTableId(NOT_EMPTY_TABLE.getId())
            .orderLineItems(Arrays.asList(ORDER_LINE_ITEM))
            .orderStatus(OrderStatus.COOKING.name())
            .orderedTime(LocalDateTime.now())
            .build();
        Order targetOrder = Order.builder()
            .orderStatus(OrderStatus.MEAL.name())
            .build();

        given(orderDao.findById(order.getId())).willReturn(Optional.of(order));
        given(orderDao.save(order)).willReturn(order);
        Order changedOrder = orderService.changeOrderStatus(order.getId(), targetOrder);

        assertThat(changedOrder.getOrderStatus()).isEqualTo(targetOrder.getOrderStatus());
    }
}