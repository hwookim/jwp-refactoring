package kitchenpos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.TableGroup;

public class Fixture {

    public static final Product PRODUCT1 = Product.builder()
        .id(1L)
        .name("강정치킨")
        .price(BigDecimal.valueOf(17_000))
        .build();
    public static final Product PRODUCT2 = Product.builder()
        .id(2L)
        .name("치즈치킨")
        .price(BigDecimal.valueOf(17_000))
        .build();

    public static final MenuGroup MENU_GROUP1 = MenuGroup.builder()
        .id(1L)
        .name("추천메뉴")
        .build();
    public static final MenuGroup MENU_GROUP2 = MenuGroup.builder()
        .id(2L)
        .name("자신메뉴")
        .build();

    public static final MenuProduct MENU_PRODUCT1 = MenuProduct.builder()
        .seq(1L)
        .menuId(1L)
        .productId(1L)
        .quantity(2)
        .build();
    public static final MenuProduct MENU_PRODUCT2 = MenuProduct.builder()
        .seq(1L)
        .menuId(2L)
        .productId(2L)
        .quantity(2)
        .build();

    public static final Menu MENU1 = Menu.builder()
        .id(1L)
        .name("강정치킨")
        .price(BigDecimal.valueOf(30_000))
        .menuGroupId(1L)
        .menuProducts(Arrays.asList(MENU_PRODUCT1))
        .build();
    public static final Menu MENU2 = Menu.builder()
        .id(2L)
        .name("강정치즈치킨")
        .price(BigDecimal.valueOf(30_000))
        .menuGroupId(1L)
        .menuProducts(Arrays.asList(MENU_PRODUCT1, MENU_PRODUCT2))
        .build();

    public static final OrderTable TABLE1 = OrderTable.builder()
        .id(1L)
        .empty(true)
        .build();
    public static final OrderTable TABLE2 = OrderTable.builder()
        .id(2L)
        .empty(true)
        .build();

    public static final TableGroup TABLE_GROUP = TableGroup.builder()
        .id(1L)
        .orderTables(Arrays.asList(TABLE1, TABLE2))
        .createdDate(LocalDateTime.now())
        .build();
}
