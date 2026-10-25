package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.ltdz.taskflow.enums.ConfigControlType;
import org.ltdz.taskflow.mapper.ConfigurationMapper;
import org.ltdz.taskflow.model.bo.ConfigValueBo;
import org.ltdz.taskflow.model.po.ConfigurationPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证设置项 value 的「反序列化 → 改值 → 序列化」流程。
 * <p>
 * 针对 id = 3759984249237669503（Agent 单轮操作超时）的真实配置。
 */
@SpringBootTest
class ConfigurationValueJsonTest {

    /** Agent 单轮操作超时 */
    private static final long CONFIG_ID = 3759984249237669503L;

    @Autowired
    private ConfigurationMapper configurationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 步骤 1：读出 value，反序列化，断言类型 / 选项 / 当前值。
     */
    @Test
    void readValue_shouldDeserializeJson() {
        ConfigurationPo po = configurationMapper.selectById(CONFIG_ID);
        assertNotNull(po, "配置项应存在");

        ConfigValueBo bo = objectMapper.readValue(po.getValue(), ConfigValueBo.class);

        assertEquals(ConfigControlType.SELECT, bo.getType(), "应为下拉框");
        assertEquals(List.of("15", "30", "60", "120"), bo.getChoose(), "选项应为 15/30/60/120");
        assertEquals("30", bo.getValue(), "当前值应为 30");

        System.out.println("[read] raw json = " + po.getValue());
        System.out.println("[read] type = " + bo.getType() + ", choose = " + bo.getChoose() + ", value = " + bo.getValue());
    }

    /**
     * 步骤 2：反序列化 → 只改 value 属性为 60 → 重新序列化，验证 type / choose 未丢。
     * <p>
     * 本用例只验证序列化结果，不写库（真实写入留给 updateUserConfig 接口）。
     */
    @Test
    void changeValueTo60_shouldKeepTypeAndChoose() {
        ConfigurationPo po = configurationMapper.selectById(CONFIG_ID);
        assertNotNull(po, "配置项应存在");

        // ① 反序列化
        ConfigValueBo bo = objectMapper.readValue(po.getValue(), ConfigValueBo.class);

        // ② 只改 value
        bo.setValue("60");

        // ③ 序列化
        String newJson = objectMapper.writeValueAsString(bo);

        // ④ 回读校验：值变了，控件描述没变
        ConfigValueBo check = objectMapper.readValue(newJson, ConfigValueBo.class);
        assertEquals("60", check.getValue(), "值应已改为 60");
        assertEquals(ConfigControlType.SELECT, check.getType(), "type 不应被改动");
        assertEquals(List.of("15", "30", "60", "120"), check.getChoose(), "choose 不应被改动");

        System.out.println("[change] new json = " + newJson);
    }
}
