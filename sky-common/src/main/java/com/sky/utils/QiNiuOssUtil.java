package com.sky.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class QiNiuOssUtil {

    //...生成上传凭证，然后准备上传
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String url;

    public String upload(byte[] uploadBytes, String objectName){
        /*Region region = Region.region2();
        log.info("{}",region);*/

        //构造一个带指定 Region 对象的配置类
        Configuration cfg = Configuration.create(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = objectName;

        //byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(uploadBytes, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            //System.out.println(putRet.key);
            //System.out.println(putRet.hash);


        } catch (QiniuException ex) {
            ex.printStackTrace();
            if (ex.response != null) {
                System.err.println(ex.response);
                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                }
            }
        }

        // TODO 需要返回文件存储在云端的文件绝对路径
        StringBuilder sb=new StringBuilder("http://");

        sb.append(url).append("/").append(objectName);
        //sb.append("http://t1rafhhu1.hn-bkt.clouddn.com/objectName");

        log.info("文件上传到: {}",sb);

        return sb.toString();
    }
}
