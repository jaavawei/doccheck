//package com.iwei.oss.service.impl;
//
//import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
//import com.github.tobato.fastdfs.domain.fdfs.StorePath;
//import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
//import com.github.tobato.fastdfs.service.FastFileStorageClient;
//import com.iwei.oss.service.FileService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.Resource;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.Objects;
//
///**
// * FastDFS文件服务
// *
// * @auther: zhaokangwei
// */
//@Slf4j
//public class FastDFSFileServiceImpl implements FileService {
//
//    @Resource
//    private FastFileStorageClient storageClient;
//
//    @Resource
//    private FdfsWebServer fdfsWebServer;
//
//    /**
//     * 上传文件（无需指定分组，由FastDFS自动分配）
//     * @param file 待上传的文件
//     * @param path 存储路径，无需理会
//     * @return 全局唯一文件ID（格式：group/路径，如group1/M00/00/00/xxx.jpg）
//     */
//    public String uploadFile(MultipartFile file, String container, String path) {
//        try {
//            // 获取文件扩展名（如jpg、png）
//            String fileExt = getFileExt(Objects.requireNonNull(file.getOriginalFilename()));
//            // 自动分配分组，上传文件
//            StorePath storePath = storageClient.uploadFile(
//                    file.getInputStream(),
//                    file.getSize(),
//                    fileExt,
//                    null // 元数据（可选，如文件描述）
//            );
//            // 返回完整文件ID（包含分组，用于后续操作）
//            return storePath.getFullPath();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 下载文件
//     * @param url 文件url
//     * @return 文件输入流（需调用方自行关闭）
//     */
//    public InputStream downloadFile(String url) {
//        try {
//            URL urlPath = new URL(url);
//            // 获取URL的路径部分，即为文件id
//            String path = urlPath.getPath();
//            // 从文件ID中解析分组和路径（自动处理分组细节）
//            StorePath storePath = StorePath.parseFromUrl(path);
//            // 下载文件字节数组
//            byte[] fileBytes = storageClient.downloadFile(
//                    storePath.getGroup(),
//                    storePath.getPath(),
//                    new DownloadByteArray()
//            );
//            return new ByteArrayInputStream(fileBytes);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 删除文件
//     * @param url 文件url
//     */
//    public void deleteFile(String url) {
//        try {
//            URL urlPath = new URL(url);
//            // 获取URL的路径部分，即为文件id
//            String path = urlPath.getPath();
//            StorePath storePath = StorePath.parseFromUrl(path);
//            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
//        } catch (Exception e) {
//            log.error("delete file error", e);
//        }
//    }
//
//    /**
//     * 提取文件扩展名（不含"."）
//     */
//    private String getFileExt(String fileName) {
//        if (fileName.contains(".")) {
//            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
//        }
//        return ""; // 无扩展名时返回空
//    }
//
//}
