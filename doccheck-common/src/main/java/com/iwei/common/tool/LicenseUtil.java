package com.iwei.common.tool;

import cn.hutool.core.net.NetUtil;
import com.alibaba.fastjson2.JSON;
import com.iwei.common.Exception.ArgBusinessException;
import com.iwei.common.entity.LicenseEntity;
import com.iwei.common.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @Author: 4611200202
 * @Date: 2024/10/12
 * @Descrmaction:
 */
@Slf4j
public class LicenseUtil {
    public static boolean checkLicense(String content, String key) {
        String jsonStr;
        try {
            jsonStr = AesUtil.decrypt2(content, key);
        } catch (Exception e) {
            log.error("license文件解析失败，请检查license配置");
            log.error(e.getMessage());
            throw new ArgBusinessException(ResultCodeEnum.LICENSE_AUTH_FAILD, "license文件解析失败，请检查license配置");
        }

        LicenseEntity licenseEntity = JSON.parseObject(jsonStr, LicenseEntity.class);
        Date date = licenseEntity.getDate();
        Date nowDate = new Date();
        if (nowDate.after(date)) {
            log.info("授权截止时间：{}", date.toString());
            log.error("已超过授权时间！");
            throw new ArgBusinessException(ResultCodeEnum.LICENSE_AUTH_FAILD, "已超过授权时间！");
        }
        return true;
//        List<String> macList = licenseEntity.getMacList();
        //获取宿主机ip
//        String ip = System.getenv("HOST_ip");
//        String ip = "172.29.61.1";
//        String mac = NetUtil.getLocalMacAddress();
//        if (StringUtils.isNotBlank(mac)) {
//            log.info("当前mac：{}", mac);
//            log.info("授权mac集合：{}", JSON.toJSON(macList));
//            boolean contains = macList.contains(mac);
//            if (macList.contains(mac)) {
//                return true;
//            }
//        }
//        log.error("当前mac:[{}]不在授权范围内！已授权mac：【{}】", mac, JSON.toJSON(macList));
//        throw new ArgBusinessException(ResultCodeEnum.LICENSE_AUTH_FAILD, "当前mac:[" + mac + "]不在授权范围内！");
    }


}
