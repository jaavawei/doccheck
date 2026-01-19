package com.iwei.common.tool;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * MinerU工具类
 *
 * @author:zhaokangwei
 */
public class MinerUUtil {

    public static final String minerUUrl = "http://172.29.59.43:30000/pdf-parse/";
    public static final String token = "Bearer mineru_f9276825-fa14-44ac-bc4c-4ba1401d25c9";

    /*
     * 向 minerU 服务发送请求解析 pdf
     */
    @SneakyThrows
    public static String parsePDF(InputStream file) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(300000, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // 将 InputStream 转换为 byte[]
        byte[] fileBytes = toByteArray(file);

        // 构建 MultipartBody
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"file.pdf\""),
                        RequestBody.create(fileBytes, MediaType.parse("application/pdf"))
                )
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(minerUUrl)
                .addHeader("Authorization", token) // 添加  Authorization 头
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非  200 状态码（如 401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Map<String, String>> responseMap = JSON.parseObject(responseJson, Map.class);
            String md = responseMap.get("result").get("md_content");
            return md;
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
