package kitchenpos.application;

import static kitchenpos.Fixture.MENU1;
import static kitchenpos.Fixture.MENU2;
import static kitchenpos.Fixture.MENU_PRODUCT1;
import static kitchenpos.Fixture.PRODUCT1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuDao menuDao;

    @Mock
    private MenuGroupDao menuGroupDao;

    @Mock
    private MenuProductDao menuProductDao;

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private MenuService menuService;

    @DisplayName("메뉴 추가")
    @Test
    void create() {
        MenuProduct menuProduct = MenuProduct.builder()
            .productId(1L)
            .quantity(2)
            .build();
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct);

        Menu menu = Menu.builder()
            .name("강정치킨")
            .price(BigDecimal.valueOf(30_000))
            .menuGroupId(1L)
            .menuProducts(menuProducts)
            .build();

        given(menuGroupDao.existsById(anyLong())).willReturn(true);
        given(productDao.findById(anyLong())).willReturn(Optional.of(PRODUCT1));
        given(menuDao.save(menu)).willReturn(MENU1);
        given(menuProductDao.save(menuProduct)).willReturn(MENU_PRODUCT1);
        Menu savedMenu = menuService.create(menu);

        assertThat(savedMenu.getId()).isNotNull();
    }

    @DisplayName("전체 메뉴 조회")
    @Test
    void list() {
        given(menuDao.findAll()).willReturn(Arrays.asList(MENU1, MENU2));
        given(menuProductDao.findAllByMenuId(anyLong()))
            .willReturn(MENU1.getMenuProducts());
        given(menuProductDao.findAllByMenuId(anyLong()))
            .willReturn(MENU2.getMenuProducts());
        List<Menu> menus = menuService.list();

        assertAll(
            () -> assertThat(menus).hasSize(2),
            () -> assertThat(menus.get(1).getMenuProducts()).hasSize(2)
        );
    }
}