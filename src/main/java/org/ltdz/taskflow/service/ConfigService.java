package org.ltdz.taskflow.service;

import lombok.RequiredArgsConstructor;
import org.ltdz.taskflow.enums.ConfigControlType;
import org.ltdz.taskflow.mapper.ConfigurationMapper;
import org.ltdz.taskflow.model.bo.ConfigItemBo;
import org.ltdz.taskflow.model.bo.ConfigValueBo;
import org.ltdz.taskflow.model.dto.UpdateUserConfigDto;
import org.ltdz.taskflow.model.po.ConfigurationPo;
import org.ltdz.taskflow.model.vo.ConfigurationVo;
import org.ltdz.taskflow.model.vo.UserConfigVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设置项服务。
 * <p>
 * 读：把 configuration.value 里的 JSON 字符串原样返回前端。
 * 写：先读全量、逐条反序列化，只改 value 属性，再序列化回存。
 */
@Service
@RequiredArgsConstructor
public class ConfigService {

    private static final int CAN_BE_SET = 1;

    private final ConfigurationMapper configurationMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取该用户的全部可编辑设置项（按 order 升序）。
     */
    public UserConfigVo getUserConfig(Long userId) {
        List<ConfigurationVo> items = configurationMapper.selectByUserId(userId).stream()
                .filter(po -> po.getCanBeSet() != null && po.getCanBeSet() == CAN_BE_SET)
                .map(this::toVo)
                .toList();

        UserConfigVo vo = new UserConfigVo();
        vo.setUserId(userId);
        vo.setConfigurations(items);
        return vo;
    }

    /**
     * 全量更新设置值。
     * <p>
     * 前端只传纯值，这里先查全量、逐条合并，避免控件描述（type / choose）被覆盖丢失。
     */
    @Transactional
    public void updateUserConfig(Long userId, UpdateUserConfigDto dto) {
        if (dto == null || dto.getConfigurations() == null || dto.getConfigurations().isEmpty()) {
            throw new IllegalArgumentException("设置项不能为空");
        }

        // ① 先取该用户全部配置
        Map<Long, ConfigurationPo> owned = configurationMapper.selectByUserId(userId).stream()
                .collect(Collectors.toMap(ConfigurationPo::getId, Function.identity()));

        for (ConfigItemBo item : dto.getConfigurations()) {
            if (item.getId() == null || item.getValue() == null) {
                throw new IllegalArgumentException("设置项 id 与值不能为空");
            }

            ConfigurationPo po = owned.get(item.getId());
            if (po == null) {
                throw new IllegalArgumentException("设置项不存在：" + item.getId());
            }
            if (po.getCanBeSet() == null || po.getCanBeSet() != CAN_BE_SET) {
                throw new IllegalArgumentException("设置项不可修改：" + po.getContent());
            }

            // ② 反序列化
            ConfigValueBo valueBo = objectMapper.readValue(po.getValue(), ConfigValueBo.class);
            validate(valueBo, po.getContent(), item.getValue());

            // ③ 只改 value
            valueBo.setValue(item.getValue());

            // ④ 序列化回存
            configurationMapper.updateValueById(po.getId(), objectMapper.writeValueAsString(valueBo));
        }
    }

    /**
     * 按控件类型校验新值（下拉框必须在 choose 内）。
     */
    private void validate(ConfigValueBo valueBo, String content, String newValue) {
        if (valueBo.getType() == ConfigControlType.SELECT) {
            List<String> choose = valueBo.getChoose();
            if (choose == null || !choose.contains(newValue)) {
                throw new IllegalArgumentException("「" + content + "」的值不在可选范围内：" + newValue);
            }
        }
    }

    private ConfigurationVo toVo(ConfigurationPo po) {
        ConfigurationVo vo = new ConfigurationVo();
        vo.setId(po.getId());
        vo.setContent(po.getContent());
        vo.setDescription(po.getDescription());
        vo.setValue(po.getValue());
        return vo;
    }
}
