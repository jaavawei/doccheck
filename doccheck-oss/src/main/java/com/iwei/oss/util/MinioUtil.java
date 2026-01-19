package com.iwei.oss.util;

import com.iwei.oss.entity.FileInfo;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * minio文件操作工具
 *
 * @author: zhaokangwei
 */
@Component
@ConditionalOnProperty(name = "storage.service.type", havingValue = "minio")
public class MinioUtil {

    @Resource
    private MinioClient minioClient;

    /**
     * 创建bucket桶
     */
    public void createBucket(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            // 1. 创建桶
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build());

            // 2. 设置桶策略为公开可读
            String policyJson = String.format(
                            "{\n" +
                            "  \"Version\": \"2012-10-17\",\n" +
                            "  \"Statement\": [\n" +
                            "    {\n" +
                            "      \"Effect\": \"Allow\",\n" +
                            "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                            "      \"Action\": [\"s3:GetObject\"],\n" +
                            "      \"Resource\": [\"arn:aws:s3:::%s/*\"]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}", bucket);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(policyJson)
                            .build());
        }
    }

    /**
     * 上传文件
     */
    public void uploadFile(InputStream inputStream, String bucket, String objectName) throws Exception {
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName)
                .stream(inputStream, -1, 5242889L).build());
    }

    /**
     * 列出所有桶
     */
    public List<String> getAllBucket() throws Exception {
        List<Bucket> buckets = minioClient.listBuckets();
        return buckets.stream().map(Bucket::name).collect(Collectors.toList());
    }

    /**
     * 列出当前桶及文件
     */
    public List<FileInfo> getAllFile(String bucket) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket).build());
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (Result<Item> result : results) {
            FileInfo fileInfo = new FileInfo();
            Item item = result.get();
            fileInfo.setFileName(item.objectName());
            fileInfo.setDirectoryFlag(item.isDir());
            fileInfo.setEtag(item.etag());
            fileInfoList.add(fileInfo);
        }
        return fileInfoList;
    }

    /**
     * 下载文件
     */
    public InputStream downLoad(String bucket, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
    }

    /**
     * 删除桶
     */
    public void deleteBucket(String bucket) throws Exception {
        minioClient.removeBucket(
                RemoveBucketArgs.builder().bucket(bucket).build()
        );
    }

    /**
     * 删除文件
     */
    public void deleteObject(String bucket, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket).object(objectName).build()
        );
    }

    /**
     * 获取文件url
     */
    public String getPreviewFileUrl(String bucketName, String objectName) throws Exception{
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName).object(objectName).build();
            return minioClient.getPresignedObjectUrl(args);
    }

    /**
     * 桶是否存在
     */
    public Boolean bucketExists(String bucket) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    }

}
