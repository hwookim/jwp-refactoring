package kitchenpos.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuProductRequest;
import kitchenpos.dto.MenuRequest;
import kitchenpos.dto.MenuResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    private final MenuDao menuDao;
    private final MenuGroupDao menuGroupDao;
    private final MenuProductDao menuProductDao;
    private final ProductDao productDao;

    public MenuService(
        final MenuDao menuDao,
        final MenuGroupDao menuGroupDao,
        final MenuProductDao menuProductDao,
        final ProductDao productDao
    ) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.menuProductDao = menuProductDao;
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
        menuProductDao.saveAll(menuProducts);
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
