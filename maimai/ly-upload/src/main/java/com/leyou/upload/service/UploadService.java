package com.leyou.upload.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
   // private static final List<String> ALLOW_TYPES=Arrays.asList("image/jpeg","image/png","image/bmp");  //把数组转换成集合
    @Autowired
    private UploadProperties uploadProperties;

    public String uploadImage(MultipartFile file) {
        try {
         //校验文件类型
         String contenType=file.getContentType();  //获取文件类型
         if(!uploadProperties.getAllowTypes().contains(contenType)){
             throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
         }
         //校验文件内容
         BufferedImage image = ImageIO.read(file.getInputStream()); //如果不是图片就返回空
         if(image==null){
             throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
         }
         //准备目标路径
        File dest=new File("F:\\upload\\images",file.getOriginalFilename()); //得到上传时的文件名
        //保存文件到本地
         file.transferTo(dest);
         //返回路径
          return "F:\\upload\\"+file.getOriginalFilename();
        } catch (IOException e) {
           //上传失败
            log.error("上传文件失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
