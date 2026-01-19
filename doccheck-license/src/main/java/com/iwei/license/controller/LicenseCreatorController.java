package com.iwei.license.controller;


import com.alibaba.fastjson2.JSON;
import com.iwei.common.entity.Result;
import com.iwei.license.entity.CheckLicenseParam;
import com.iwei.license.entity.LicenseParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于生成证书文件，不能放在给客户部署的代码里
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/license")
public class LicenseCreatorController {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    @PostMapping("initLicense")
    public Result<String> initLicense(@RequestBody LicenseParam param) throws Exception {
        Map<String, Object> map = new HashMap<>(2);
        map.put("ipList", param.getIpList());
        map.put("date", param.getDate());
        String jsonStr = JSON.toJSONString(map);
        String encrypt = encrypt(jsonStr, param.getKey());
        return Result.ok(encrypt);
    }

    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @PostMapping("checkLicense")
    public Result<String> checkLicense(@RequestBody CheckLicenseParam param) throws Exception {
        String decrypt = decrypt(param.getLicense(), param.getKey());
        return Result.ok(decrypt);
    }

    public static String decrypt(String encryptedData, String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] keyBytes = secretKey.getEncoded();

        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
