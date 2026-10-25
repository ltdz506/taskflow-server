package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.ltdz.taskflow.enums.ConfigControlType;
import org.ltdz.taskflow.mapper.ConfigurationMapper;
import org.ltdz.taskflow.model.bo.ConfigItemBo;
import org.ltdz.taskflow.model.bo.ConfigValueBo;
import org.ltdz.taskflow.model.dto.UpdateUserConfigDto;
import org.ltdz.taskflow.model.po.ConfigurationPo;
import org.ltdz.taskflow.model.vo.ConfigurationVo;
import org.ltdz.taskflow.model.vo.UserConfigVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigService} 集成测试（真实库）。
 * <p>
 * 固定验证 id = 3759984249237669503（Agent 单轮操作超时）。
 */
@SpringBootTest
class ConfigServiceTest {

    private static final long USER_ID = 1145141919810L;
    private static final long CONFIG_ID = 3759984249237669503L;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigurationMapper configurationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 读：返回 5 条可编辑设置项，指定那条的 value 是 JSON 字符串。
     */
    @Test
    void getUserConfig_shouldReturnAllEditableItems() {
        UserConfigVo vo = configService.getUserConfig(USER_ID);

        assertEquals(USER_ID, vo.getUserId());
        assertEquals(5, vo.getConfigurations().size(), "应有 5 个可编辑设置项");

        ConfigurationVo target = vo.getConfigurations().stream()
                .filter(c -> c.getId() == CONFIG_ID)
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到目标配置项"));

        assertEquals("Agent 单轮操作超时", target.getContent());
        assertTrue(target.getValue().contains("\"type\":\"SELECT\""), "value 应为 JSON 且 type=SELECT");
        assertTrue(target.getValue().contains("\"value\":\"30\""), "默认值应为 30");

        System.out.println("[getUserConfig] count = " + vo.getConfigurations().size());
        vo.getConfigurations().forEach(c ->
                System.out.println("  " + c.getId() + " | " + c.getContent() + " | " + c.getValue()));
    }

    /**
     * 写：把目标项改成 60，验证只改了 value、控件描述保留（事务回滚，不污染数据库）。
     */
    @Test
    @Transactional
    void updateUserConfig_shouldChangeOnlyValue(){
        configService.updateUserConfig(USER_ID, dtoOf("60"));

        ConfigurationPo po = configurationMapper.selectById(CONFIG_ID);
        ConfigValueBo bo = objectMapper.readValue(po.getValue(), ConfigValueBo.class);

        assertEquals("60", bo.getValue(), "值应已改为 60");
        assertEquals(ConfigControlType.SELECT, bo.getType(), "type 不应被改动");
        assertEquals(List.of("15", "30", "60", "120"), bo.getChoose(), "choose 不应被改动");

        System.out.println("[updateUserConfig] new json = " + po.getValue());
    }

    /**
     * 写：提交不在 choose 内的值时拒绝。
     */
    @Test
    void updateUserConfig_shouldRejectValueNotInChoose() {
        UpdateUserConfigDto dto = dtoOf("999");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> configService.updateUserConfig(USER_ID, dto));

        assertTrue(ex.getMessage().contains("不在可选范围内"));
        System.out.println("[reject] " + ex.getMessage());
    }

    private UpdateUserConfigDto dtoOf(String value) {
        ConfigItemBo item = new ConfigItemBo();
        item.setId(ConfigServiceTest.CONFIG_ID);
        item.setValue(value);

        UpdateUserConfigDto dto = new UpdateUserConfigDto();
        dto.setConfigurations(List.of(item));
        return dto;
    }
}
