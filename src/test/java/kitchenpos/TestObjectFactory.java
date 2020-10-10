package kitchenpos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.TableGroup;

public class TestObjectFactory {

    public static Order createOrder(OrderTable table, List<OrderLineItem> orderLineItems) {
        return Order.builder()
            .orderTable(table)
            .orderStatus(OrderStatus.COOKING.name())
            .orderLineItems(orderLineItems)
            .orderedTime(LocalDateTime.now())
            .build();
    }

    public static Order createOrder(OrderTable table) {
        return createOrder(table, null);
    }

    public static OrderLineItem createOrderLineItem(Menu menu) {
        return OrderLineItem.builder()
            .menu(menu)
            .quantity(2)
            .build();
    }

    public static Menu createMenu(int price, MenuGroup menuGroup, List<MenuProduct> menuProducts) {
        return Menu.builder()
            .name("강정치킨")
            .price(BigDecimal.valueOf(price))
            .menuGroup(menuGroup)
            .menuProducts(menuProducts)
            .build();
    }

    public static MenuGroup createMenuGroup(Long id, String name) {
        return MenuGroup.builder()
            .id(id)
            .name(name)
            .build();
    }

    public static MenuGroup createMenuGroup(String name) {
        return createMenuGroup(null, name);
    }

    public static MenuProduct createMenuProduct(Product product) {
        return MenuProduct.builder()
            .product(product)
            .quantity(1)
            .build();
    }

    public static Product createProduct(Long id, int price) {
        return Product.builder()
            .id(id)
            .name("강정치킨")
            .price(BigDecimal.valueOf(price))
            .build();
    }

    public static Product createProduct(int price) {
        return createProduct(null, price);
    }

    public static TableGroup createTableGroup(List<OrderTable> tables) {
        return TableGroup.builder()
            .orderTables(tables)
            .createdDate(LocalDateTime.now())
            .build();
    }

    public static OrderTable createTable(Long id, boolean empty) {
        return OrderTable.builder()
            .id(id)
            .numberOfGuests(0)
            .empty(empty)
            .build();
    }

    public static OrderTable createTable(boolean empty) {
        return createTable(null, empty);
    }
}
