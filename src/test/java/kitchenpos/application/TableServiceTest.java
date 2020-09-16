package kitchenpos.application;

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
            .id(1L)
            .empty(true)
            .build();
        OrderTable targetTable = OrderTable.builder()
            .empty(false)
            .build();

        given(tableDao.findById(table.getId())).willReturn(Optional.of(table));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(
            table.getId(),
            Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()
            ))).willReturn(false);
        given(tableDao.save(table)).willReturn(table);

        OrderTable changedTable = tableService.changeEmpty(table.getId(), targetTable);
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
        OrderTable notEmptyTable = OrderTable.builder()
            .id(3L)
            .empty(false)
            .build();
        OrderTable targetTable = OrderTable.builder()
            .numberOfGuests(10)
            .build();

        given(tableDao.findById(notEmptyTable.getId())).willReturn(Optional.of(notEmptyTable));
        given(tableDao.save(notEmptyTable)).willReturn(notEmptyTable);
        OrderTable changedTable = tableService
            .changeNumberOfGuests(notEmptyTable.getId(), targetTable);

        assertThat(changedTable.getNumberOfGuests()).isEqualTo(10);
    }

    @DisplayName("[예외] 0미만의 손님 수 변경")
    @Test
    void changeNumberOfGuests_With_LessNumberOfGuests() {
        OrderTable table = OrderTable.builder()
            .numberOfGuests(-1)
            .build();

        assertThatThrownBy(() -> tableService.changeNumberOfGuests(TABLE1.getId(), table))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 빈 테이블 손님 수 변경")
    @Test
    void changeNumberOfGuests_With_EmptyTable() {
        OrderTable table = OrderTable.builder()
            .numberOfGuests(10)
            .build();

        given(tableDao.findById(TABLE1.getId())).willReturn(Optional.of(TABLE1));
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(TABLE1.getId(), table))
            .isInstanceOf(IllegalArgumentException.class);
    }
}