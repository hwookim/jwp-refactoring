package kitchenpos.application;

import static kitchenpos.Fixture.TABLE1;
import static kitchenpos.Fixture.TABLE2;
import static kitchenpos.Fixture.TABLE_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Arrays;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.TableGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceTest {

    @Mock
    private OrderTableDao tableDao;

    @Mock
    private TableGroupDao tableGroupDao;

    @InjectMocks
    private TableGroupService tableGroupService;

    @DisplayName("테이블 그룹 추가")
    @Test
    void create() {
        TableGroup tableGroup = TableGroup.builder()
            .orderTables(Arrays.asList(TABLE1, TABLE2))
            .createdDate(LocalDateTime.now())
            .build();

        given(tableDao.findAllByIdIn(any())).willReturn(Arrays.asList(TABLE1, TABLE2));
        given(tableGroupDao.save(tableGroup)).willReturn(TABLE_GROUP);
        TableGroup savedTableGroup = tableGroupService.create(tableGroup);

        assertThat(savedTableGroup.getId()).isNotNull();
    }
}