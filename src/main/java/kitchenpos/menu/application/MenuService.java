package kitchenpos.menu.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.repository.MenuDao;
import kitchenpos.menu_group.repository.MenuGroupDao;
import kitchenpos.product.repository.ProductDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu_group.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.product.domain.Product;
import kitchenpos.menu.dto.MenuProductRequest;
import kitchenpos.menu.dto.MenuRequest;
import kitchenpos.menu.dto.MenuResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    private final MenuDao menuDao;
    private final MenuGroupDao menuGroupDao;
    private final ProductDao productDao;

    public MenuService(
        final MenuDao menuDao,
        final MenuGroupDao menuGroupDao,
        final ProductDao productDao
    ) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.productDao = productDao;
    }

    @Transactional
    public MenuResponse create(final MenuRequest request) {
        MenuGroup menuGroup = menuGroupDao.findById(request.getMenuGroupId())
            .orElseThrow(IllegalArgumentException::new);

        List<Product> products = findProducts(request.getMenuProducts());
        List<MenuProduct> menuProducts = convertMenuProducts(request.getMenuProducts(), products);

        Menu menu = Menu.builder()
            .name(request.getName())
            .price(request.getPrice())
            .menuGroup(menuGroup)
            .menuProducts(menuProducts)
            .build();

        Menu savedMenu = menuDao.save(menu);
        return MenuResponse.from(savedMenu);
    }

    private List<Product> findProducts(final List<MenuProductRequest> request) {
        List<Long> productIds = request.stream()
            .map(MenuProductRequest::getProductId)
            .collect(Collectors.toList());
        List<Product> products = productDao.findAllById(productIds);
        validateSavedProduct(productIds, products);

        return products;
    }

    private void validateSavedProduct(final List<Long> productIds, final List<Product> products) {
        if (productIds.size() != products.size()) {
            throw new IllegalArgumentException();
        }
    }

    private List<MenuProduct> convertMenuProducts(
        final List<MenuProductRequest> requests,
        final List<Product> products
    ) {
        return requests.stream()
            .map(request -> convertMenuProduct(request, products))
            .collect(Collectors.toList());
    }

    private MenuProduct convertMenuProduct(
        final MenuProductRequest request,
        final List<Product> products
    ) {
        Product product = findProduct(request, products);
        return MenuProduct.builder()
            .product(product)
            .quantity(request.getQuantity())
            .build();
    }

    private Product findProduct(final MenuProductRequest request, final List<Product> products) {
        return products.stream()
            .filter(product -> product.isSameId(request.getProductId()))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional
    public List<MenuResponse> list() {
        List<Menu> menus = menuDao.findAll();
        return MenuResponse.listFrom(menus);
    }
}