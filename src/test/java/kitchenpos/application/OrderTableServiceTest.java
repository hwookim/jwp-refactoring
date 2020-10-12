package kitchenpos.application;

import static kitchenpos.TestObjectFactory.createOrderTableIdRequest;
import static kitchenpos.TestObjectFactory.createTableGroupRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.OrderTableIdRequest;
import kitchenpos.dto.OrderTableRequest;
import kitchenpos.dto.OrderTableResponse;
import kitchenpos.dto.TableGroupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/deleteAll.sql")
class OrderTableServiceTest {

    @Autowired
    private OrderTableService orderTableService;

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderTableDao orderTableDao;

    @DisplayName("테이블 추가")
    @Test
    void create() {
        OrderTableRequest request = createTableRequest(0, true);

        OrderTableResponse savedTable = orderTableService.create(request);

        assertThat(savedTable.getId()).isNotNull();
    }

    @DisplayName("테이블 전체 조회")
    @Test
    void list() {
        OrderTableRequest request = createTableRequest(0, true);
        orderTableService.create(request);
        orderTableService.create(request);

        List<OrderTableResponse> list = orderTableService.list();

        assertThat(list).hasSize(2);
    }

    @DisplayName("주문 등록 불가 여부 변경")
    @Test
    void changeEmpty() {
        OrderTableRequest table = createTableRequest(0, true);
        OrderTableResponse savedTable = orderTableService.create(table);
        OrderTableRequest request = createTableRequest(false);

        OrderTableResponse changedTable = orderTableService
            .changeEmpty(savedTable.getId(), request);

        assertThat(changedTable.isEmpty()).isEqualTo(request.getEmpty());
    }

    @DisplayName("[예외] 존재하지 않는 테이블의 주문 등록 불가 여부 변경")
    @Test
    void changeEmpty_Fail_With_NotExistTable() {
        OrderTableRequest request = createTableRequest(false);

        assertThatThrownBy(() -> orderTableService.changeEmpty(1000L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 그룹에 포함된 테이블의 주문 등록 불가 여부 변경")
    @Test
    void changeEmpty_Fail_With_TableInGroup() {
        OrderTableRequest table = createTableRequest(0, true);
        OrderTableResponse savedTable1 = orderTableService.create(table);
        OrderTableResponse savedTable2 = orderTableService.create(table);
        OrderTableIdRequest tableIdRequest1 = createOrderTableIdRequest(savedTable1.getId());
        OrderTableIdRequest tableIdRequest2 = createOrderTableIdRequest(savedTable2.getId());
        List<OrderTableIdRequest> tables = Arrays.asList(tableIdRequest1, tableIdRequest2);
        TableGroupRequest tableGroup = createTableGroupRequest(tables);
        tableGroupService.create(tableGroup);

        OrderTableRequest request = createTableRequest(false);

        assertThatThrownBy(() -> orderTableService.changeEmpty(savedTable1.getId(), request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 조리, 식사 중인 테이블의 주문 등록 불가 여부 변경")
    @Test
    void changeEmpty_Fail_With_TableInProgress() {
        OrderTableRequest table = createTableRequest(0, true);
        OrderTableResponse orderTableResponse = orderTableService.create(table);

        OrderTable savedTable = orderTableDao.findById(orderTableResponse.getId()).get();
        Order order = Order.builder()
            .orderTable(savedTable)
            .orderStatus(OrderStatus.COOKING.name())
            .orderedTime(LocalDateTime.now())
            .build();
        orderDao.save(order);

        OrderTableRequest request = createTableRequest(true);

        assertThatThrownBy(() -> orderTableService.changeEmpty(savedTable.getId(), request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("손님 수 변경")
    @Test
    void changeNumberOfGuests() {
        OrderTableRequest table = createTableRequest(0, false);
        OrderTableResponse savedTable = orderTableService.create(table);
        OrderTableRequest request = createTableRequest(10);

        OrderTableResponse changedTable = orderTableService
            .changeNumberOfGuests(savedTable.getId(), request);

        assertThat(changedTable.getNumberOfGuests()).isEqualTo(request.getNumberOfGuests());
    }

    @DisplayName("[예외] 0보다 작은 수로 손님 수 변경")
    @Test
    void changeNumberOfGuests_Fail_With_InvalidNumberOfGuest() {
        OrderTableRequest table = createTableRequest(0, true);
        OrderTableResponse savedTable = orderTableService.create(table);
        OrderTableRequest request = createTableRequest(-1);

        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(savedTable.getId(), request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 존재하지 않는 테이블의 손님 수 변경")
    @Test
    void changeNumberOfGuests_Fail_With_NotExistTable() {
        OrderTableRequest request = createTableRequest(100);

        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(1000L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("[예외] 빈 테이블의 손님 수 변경")
    @Test
    void changeNumberOfGuests_Fail_With_EmptyTable() {
        OrderTableRequest emptyTable = createTableRequest(0, true);
        OrderTableResponse savedEmptyTable = orderTableService.create(emptyTable);

        OrderTableRequest request = createTableRequest(100);

        assertThatThrownBy(
            () -> orderTableService.changeNumberOfGuests(savedEmptyTable.getId(), request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private OrderTableRequest createTableRequest(int numberOfGuests, boolean empty) {
        return OrderTableRequest.builder()
            .numberOfGuests(numberOfGuests)
            .empty(empty)
            .build();
    }

    private OrderTableRequest createTableRequest(boolean empty) {
        return OrderTableRequest.builder()
            .numberOfGuests(null)
            .empty(empty)
            .build();
    }

    private OrderTableRequest createTableRequest(int numberOfGuests) {
        return OrderTableRequest.builder()
            .numberOfGuests(numberOfGuests)
            .empty(null)
            .build();
    }
}