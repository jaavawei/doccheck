package com.iwei.common.enums;

import lombok.Getter;

/**
 * 专业类别枚举类
 *
 * @author: zhaokangwei
 */
@Getter
public enum ProjectTypeEnum {
    DIAN_WANG_JI_JIAN(1, "电网基建"),
    DIAN_WANG_XIAO_XING_JI_JIAN(2, "电网小型基建"),
    DIAN_LI_SHI_CHANG_YING_XIAO(3, "电力市场营销"),
    SHENG_CHAN_JI_GAI(4, "生产技改"),
    SHENG_CHAN_DA_XIU(6, "生产大修"),
    LING_XING_GOU_ZHI(8, "零星购置"),
    YAN_JIU_KAI_FA(9, "研究开发"),
    DIAN_WANG_SHU_ZI_HUA(10, "电网数字化"),
    GUAN_LI_ZI_XUN(11, "管理咨询"),
    JIAO_YU_PEI_XUN(12, "教育培训"),
    GU_QUAN_TOU_ZI(13, "股权投资"),
    SHENG_CHAN_FU_ZHU_JI_GAI(15, "生产辅助技改"),
    SHENG_CHAN_FU_ZHU_DA_XIU(16, "生产辅助大修");

    private final Integer code;
    private final String name;

    ProjectTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据专业代码获取对应的枚举实例
     *
     * @param codeVal 专业代码
     * @return 对应的枚举实例，若未找到则返回null
     */
    public static ProjectTypeEnum getByCode(Integer codeVal) {
        for (ProjectTypeEnum speciality : ProjectTypeEnum.values()) {
            if (speciality.code.equals(codeVal)) {
                return speciality;
            }
        }
        return null;
    }
}