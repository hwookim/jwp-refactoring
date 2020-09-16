package kitchenpos.application;

import static kitchenpos.Fixture.MENU_GROUP1;
import static kitchenpos.Fixture.MENU_GROUPS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.domain.MenuGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {

    @Mock
    private MenuGroupDao menuGroupDao;

    @InjectMocks
    private MenuGroupService menuGroupService;

    @DisplayName("메뉴 그룹 추가")
    @Test
    void create() {
        MenuGroup menuGroup = MenuGroup.builder()
            .name("추천메뉴")
            .build();

        given(menuGroupDao.save(menuGroup)).willReturn(MENU_GROUP1);
        MenuGroup savedMenuGroup = menuGroupService.create(menuGroup);

        assertThat(savedMenuGroup.getId()).isNotNull();
    }

    @DisplayName("전체 메뉴 그룹 조회")
    @Test
    void list() {
        given(menuGroupDao.findAll()).willReturn(MENU_GROUPS);
        List<MenuGroup> menuGroups = menuGroupService.list();

        assertThat(menuGroups).hasSize(2);
    }
}