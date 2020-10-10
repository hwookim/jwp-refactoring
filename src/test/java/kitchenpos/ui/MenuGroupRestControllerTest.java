package kitchenpos.ui;

import static kitchenpos.TestObjectFactory.createMenuGroup;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.application.MenuGroupService;
import kitchenpos.domain.MenuGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(MenuGroupRestController.class)
class MenuGroupRestControllerTest {

    private static final String URL = "/api/menu-groups";

    @MockBean
    private MenuGroupService menuGroupService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilter(new CharacterEncodingFilter("UTF-8", true))
            .build();
    }

    @DisplayName("POST " + URL)
    @Test
    void create() throws Exception {
        MenuGroup menuGroup = createMenuGroup(0L, "추천메뉴");
        String request = objectMapper.writeValueAsString(menuGroup);

        given(menuGroupService.create(any())).willReturn(menuGroup);

        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(redirectedUrl(URL + "/" + menuGroup.getId()));
    }
}
