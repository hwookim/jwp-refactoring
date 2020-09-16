package kitchenpos.application;

import static kitchenpos.Fixture.PRODUCT1;
import static kitchenpos.Fixture.PRODUCT2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductService productService;

    @DisplayName("상품 추가")
    @Test
    void create() {
        Product product = Product.builder()
            .name("강정치킨")
            .price(BigDecimal.valueOf(17_000))
            .build();

        given(productDao.save(product)).willReturn(PRODUCT1);
        Product savedProduct = productService.create(product);

        assertThat(savedProduct.getId()).isNotNull();
    }

    @DisplayName("[예외] 가격이 잘못된 상품 추가")
    @Test
    void create_Fail_With_InvalidPrice() {
        Product product = Product.builder()
            .name("강정치킨")
            .price(BigDecimal.valueOf(-1))
            .build();

        assertThatThrownBy(() -> productService.create(product))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("전체 상품 조회")
    @Test
    void list() {
        given(productDao.findAll()).willReturn(Arrays.asList(PRODUCT1, PRODUCT2));
        List<Product> products = productService.list();

        assertThat(products).hasSize(2);
    }
}