package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
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

    private static final Product 강정치킨 = Product.builder()
        .id(1L)
        .name("강정치킨")
        .price(BigDecimal.valueOf(17_000))
        .build();

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

        given(productDao.save(product)).willReturn(강정치킨);
        Product savedProduct = productService.create(product);

        assertThat(savedProduct.getId()).isNotNull();
    }
}