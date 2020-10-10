package kitchenpos.application;

import static kitchenpos.TestObjectFactory.createMenu;
import static kitchenpos.TestObjectFactory.createMenuGroup;
import static kitchenpos.TestObjectFactory.createMenuProduct;
import static kitchenpos.TestObjectFactory.createProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.List;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/deleteAll.sql")
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuGroupDao menuGroupDao;

    @Autowired
    private ProductDao productDao;

    @DisplayName("메뉴 추가")
    @Test
    void create() {
        MenuGroup savedMenuGroup = menuGroupDao.save(createMenuGroup("강정메뉴"));

        Product savedProduct = productDao.save(createProduct(18_000));
        MenuProduct menuProduct = createMenuProduct(savedProduct);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        Menu menu = createMenu(18_000, savedMenuGroup, menuProducts);
        Menu savedMenu = menuService.create(menu);

        assertAll(
            () -> assertThat(savedMenu.getId()).isNotNull(),
            () -> assertThat(savedMenu.getMenuProducts().get(0).getSeq()).isNotNull(),
            () -> assertThat(savedMenu.getMenuProducts().get(1).getSeq()).isNotNull()
        );
    }

    @DisplayName("[예외] 가격이 0보다 작은 메뉴 추가")
    @Test
    void create_Fail_With_InvalidPrice() {
        MenuGroup savedMenuGroup = menuGroupDao.save(createMenuGroup("강정메뉴"));

        Product savedProduct = productDao.save(createProduct(18_000));
        MenuProduct menuProduct = createMenuProduct(savedProduct);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        Menu menu = createMenu(-1, savedMenuGroup, menuProducts);

        assertThatThrownBy(() -> menuService.create(menu))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 존재하지 않는 메뉴 그룹에 속한 메뉴 추가")
    @Test
    void create_Fail_With_NotExistMenuGroup() {
        MenuGroup notSavedMenuGroup = createMenuGroup(1000L, "강정메뉴");

        Product product = productDao.save(createProduct(18_000));
        MenuProduct menuProduct = createMenuProduct(product);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        Menu menu = createMenu(18_000, notSavedMenuGroup, menuProducts);

        assertThatThrownBy(() -> menuService.create(menu))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 존재하지 않는 상품을 포함한 메뉴 추가")
    @Test
    void create_Fail_With_NotExistProduct() {
        MenuGroup savedMenuGroup = menuGroupDao.save(createMenuGroup("강정메뉴"));

        Product notSavedProduct = createProduct(1000L, 18_000);
        MenuProduct menuProduct = createMenuProduct(notSavedProduct);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        Menu menu = createMenu(18_000, savedMenuGroup, menuProducts);

        assertThatThrownBy(() -> menuService.create(menu))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 포함한 상품의 가격 총합보다 높은 가격의 메뉴 추가")
    @Test
    void create_Fail_With_OverPrice() {
        MenuGroup savedMenuGroup = menuGroupDao.save(createMenuGroup("강정메뉴"));

        Product savedProduct = productDao.save(createProduct(18_000));
        MenuProduct menuProduct = createMenuProduct(savedProduct);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        Menu menu = createMenu(100_000_000, savedMenuGroup, menuProducts);

        assertThatThrownBy(() -> menuService.create(menu))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 전체 조회")
    @Test
    void list() {
        MenuGroup savedMenuGroup = menuGroupDao.save(createMenuGroup("강정메뉴"));

        Product savedProduct = productDao.save(createProduct(18_000));
        MenuProduct menuProduct = createMenuProduct(savedProduct);
        List<MenuProduct> menuProducts = Arrays.asList(menuProduct, menuProduct);

        menuService.create(createMenu(18_000, savedMenuGroup, menuProducts));
        menuService.create(createMenu(18_000, savedMenuGroup, menuProducts));

        List<Menu> list = menuService.list();

        assertThat(list).hasSize(2);
    }
}