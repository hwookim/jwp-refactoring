package kitchenpos.application;

import static kitchenpos.Fixture.TABLE1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

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
}