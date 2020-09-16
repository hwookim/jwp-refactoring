package kitchenpos.application;

import static kitchenpos.Fixture.TABLE1;
import static kitchenpos.Fixture.TABLE2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    OrderTableDao tableDao;

    @InjectMocks
    TableService tableService;

    @DisplayName("테이블 추가")
    @Test
    void create() {
        OrderTable table = OrderTable.builder()
            .id(1L)
            .empty(true)
            .build();

        given(tableDao.save(table)).willReturn(TABLE1);
        OrderTable savedTable = tableService.create(table);

        assertThat(savedTable.getId()).isNotNull();
    }

    @DisplayName("전체 테이블 조회")
    @Test
    void list() {
        given(tableDao.findAll()).willReturn(Arrays.asList(TABLE1, TABLE2));
        List<OrderTable> tables = tableService.list();

        assertThat(tables).hasSize(2);
    }
}