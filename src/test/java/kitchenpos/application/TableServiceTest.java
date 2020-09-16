package kitchenpos.application;

import static kitchenpos.Fixture.NOT_EMPTY_TABLE;
import static kitchenpos.Fixture.TABLE1;
import static kitchenpos.Fixture.TABLE2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderStatus;
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
    private OrderDao orderDao;

    @Mock
    private OrderTableDao tableDao;

    @InjectMocks
    private TableService tableService;

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

    @DisplayName("주문 등록 불가 여부 변경")
    @Test
    void changeEmpty() {
        OrderTable table = OrderTable.builder()
            .empty(false)
            .build();

        given(tableDao.findById(TABLE1.getId())).willReturn(Optional.of(TABLE1));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(
            TABLE1.getId(),
            Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()
        ))).willReturn(false);
        given(tableDao.save(TABLE1)).willReturn(TABLE1);

        OrderTable changedTable = tableService.changeEmpty(TABLE1.getId(), table);
        assertThat(changedTable.isEmpty()).isFalse();
    }

    @DisplayName("[예외] 그룹에 속한 테이블의 주문 등록 불가 여부 변경")
    @Test
    void changeEmpty_Fail_With_GroupedTable() {
        OrderTable groupedTable = OrderTable.builder()
            .id(1L)
            .tableGroupId(1L)
            .empty(true)
            .build();

        OrderTable table = OrderTable.builder()
            .empty(false)
            .build();

        given(tableDao.findById(groupedTable.getId())).willReturn(Optional.of(groupedTable));
        assertThatThrownBy(() -> tableService.changeEmpty(groupedTable.getId(), table))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 조리, 식사가 진행 중인 테이블의 주문 등록 불가 여부 변경")
    @Test
    void changeEmpty_Fail_With_TableInProgress() {
        OrderTable table = OrderTable.builder()
            .empty(false)
            .build();

        given(tableDao.findById(TABLE1.getId())).willReturn(Optional.of(TABLE1));

        given(orderDao.existsByOrderTableIdAndOrderStatusIn(
            TABLE1.getId(),
            Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()
            ))).willReturn(true);
        assertThatThrownBy(() -> tableService.changeEmpty(TABLE1.getId(), table))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("손님 수 변경")
    @Test
    void changeNumberOfGuests() {
        OrderTable table = OrderTable.builder()
            .numberOfGuests(10)
            .build();

        given(tableDao.findById(NOT_EMPTY_TABLE.getId())).willReturn(Optional.of(NOT_EMPTY_TABLE));
        given(tableDao.save(NOT_EMPTY_TABLE)).willReturn(NOT_EMPTY_TABLE);
        OrderTable changedTable = tableService.changeNumberOfGuests(NOT_EMPTY_TABLE.getId(), table);

        assertThat(changedTable.getNumberOfGuests()).isEqualTo(10);
    }
}