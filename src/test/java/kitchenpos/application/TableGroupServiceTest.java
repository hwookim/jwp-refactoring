package kitchenpos.application;

import static kitchenpos.Fixture.TABLE1;
import static kitchenpos.Fixture.TABLE2;
import static kitchenpos.Fixture.TABLE_GROUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
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
    private OrderDao orderDao;

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

        assertAll(
            () -> assertThat(savedTableGroup.getId()).isNotNull(),
            () -> assertThat(savedTableGroup.getOrderTables().get(0).isEmpty()).isFalse(),
            () -> assertThat(savedTableGroup.getOrderTables().get(1).isEmpty()).isFalse()
        );
    }

    @DisplayName("[예외] 2개 미만의 테이블을 포함한 테이블 그룹 추가")
    @Test
    void create_Fail_With_LessTable() {
        TableGroup tableGroup = TableGroup.builder()
            .orderTables(Arrays.asList(TABLE1))
            .createdDate(LocalDateTime.now())
            .build();

        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 저장되지 않은 테이블을 포함한 테이블 그룹 추가")
    @Test
    void create_Fail_With_NotExistTable() {
        TableGroup tableGroup = TableGroup.builder()
            .orderTables(Arrays.asList(TABLE1, TABLE2))
            .createdDate(LocalDateTime.now())
            .build();

        given(tableDao.findAllByIdIn(any())).willReturn(Arrays.asList(TABLE1));
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 빈 상태가 아닌 테이블을 포함한 테이블 그룹 추가")
    @Test
    void create_Fail_With_NotEmptyTable() {
        OrderTable NOT_EMPTY_TABLE = OrderTable.builder()
            .empty(false)
            .build();

        TableGroup tableGroup = TableGroup.builder()
            .orderTables(Arrays.asList(TABLE1, NOT_EMPTY_TABLE))
            .createdDate(LocalDateTime.now())
            .build();

        given(tableDao.findAllByIdIn(any())).willReturn(Arrays.asList(TABLE1, NOT_EMPTY_TABLE));
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 아마 다른 그룹에 속한 테이블을 포함한 테이블 그룹 추가")
    @Test
    void create_Fail_With_OthersTable() {
        OrderTable OTHERS_TABLE = OrderTable.builder()
            .empty(true)
            .tableGroupId(2L)
            .build();

        TableGroup tableGroup = TableGroup.builder()
            .orderTables(Arrays.asList(TABLE1, OTHERS_TABLE))
            .createdDate(LocalDateTime.now())
            .build();

        given(tableDao.findAllByIdIn(any())).willReturn(Arrays.asList(TABLE1, OTHERS_TABLE));
        assertThatThrownBy(() -> tableGroupService.create(tableGroup))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블 그룹 해제")
    @Test
    void ungroup() {
        List<OrderTable> tables = TABLE_GROUP.getOrderTables();
        given(tableDao.findAllByTableGroupId(TABLE_GROUP.getId()))
            .willReturn(tables);

        List<Long> orderTableIds = tables.stream()
            .map(OrderTable::getId)
            .collect(Collectors.toList());
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(
            orderTableIds,
            Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
            .willReturn(false);

        given(tableDao.save(TABLE1)).willReturn(TABLE1);
        given(tableDao.save(TABLE2)).willReturn(TABLE2);
        tableGroupService.ungroup(TABLE_GROUP.getId());

        assertAll(
            () -> assertThat(tables.get(0).getTableGroupId()).isNull(),
            () -> assertThat(tables.get(1).getTableGroupId()).isNull(),
            () -> assertThat(tables.get(0).isEmpty()).isFalse(),
            () -> assertThat(tables.get(1).isEmpty()).isFalse()
        );
    }


    @DisplayName("[예외] 조리, 식사가 진행 중인 테이블을 포함한 테이블 그룹 해제")
    @Test
    void ungroup_Fail_With_TableInProgress() {
        List<OrderTable> tables = TABLE_GROUP.getOrderTables();
        given(tableDao.findAllByTableGroupId(TABLE_GROUP.getId()))
            .willReturn(tables);

        List<Long> orderTableIds = tables.stream()
            .map(OrderTable::getId)
            .collect(Collectors.toList());
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(
            orderTableIds,
            Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
            .willReturn(true);
        assertThatThrownBy(() -> tableGroupService.ungroup(TABLE_GROUP.getId()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}