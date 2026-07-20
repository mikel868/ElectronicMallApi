package com.rabbiter.em.storage.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;  // 引入Hutool的StrUtil
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.storage.entity.MyFile;
import com.rabbiter.em.shared.exception.ServiceException;
import com.rabbiter.em.storage.mapper.FileMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@Service
public class FileService extends ServiceImpl<FileMapper, MyFile> {
    // 优化依赖注入：使用final + 构造函数注入
    private final FileMapper fileMapper;

    public FileService(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public String upload(MultipartFile uploadFile){
        String originalFilename = uploadFile.getOriginalFilename(); //文件原始名字
        String type = originalFilename.substring(originalFilename.lastIndexOf(".")+1);  //文件后缀
        long size = uploadFile.getSize() / 1024; //文件大小，单位kb
        String url;
        MyFile myFile = new MyFile();  //用于保存于数据库的实体类Files
        myFile.setName(originalFilename);
        myFile.setSize(size);
        myFile.setType(type);

        //通过md5判断文件是否已经存在，防止在服务器存储相同文件
        InputStream inputStream = null;
        try {
            inputStream = uploadFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String md5 = SecureUtil.md5(inputStream);
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<MyFile> dbMyFileList = fileMapper.selectList(queryWrapper);
        if(dbMyFileList.size() != 0){
            //数据库中已有该md5，则拷贝其url
            url = dbMyFileList.get(0) .getUrl();
            myFile.setUrl(url);
        }else{
            //文件不存在，则保存文件
            File folder = new File(Constants.fileFolderPath);
            if(!folder.exists()){
                folder.mkdir();
            }
            String folderPath = folder.getAbsolutePath()+"/";   //文件存储文件夹的位置
            System.out.println("文件存储地址"+folderPath);
            //将文件保存为UUID的名字，通过uuid生成url
            String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            String finalFileName = uuid+"."+type;
            File targetFile = new File(folderPath + finalFileName);
            try {
                uploadFile.transferTo(targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            url = "/file/"+finalFileName;
            myFile.setUrl(url);
        }
        myFile.setMd5(md5);

        fileMapper.insert(myFile);
        System.out.println("文件"+originalFilename+" "+url);
        return url;
    }

    //根据文件名下载文件
    public void download(String fileName, HttpServletResponse response){
        File file = new File(Constants.fileFolderPath+fileName);
        if(!file.exists()){
            throw new ServiceException(Constants.CODE_500,"文件不存在");
        }
        try {
            ServletOutputStream os = response.getOutputStream();
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));
            response.setContentType("application/octet-stream");
            os.write(FileUtil.readBytes(file));
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int fakeDelete(int id){
        UpdateWrapper<MyFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id).set("is_delete",true);
        return fileMapper.update(null,updateWrapper);
    }

    public IPage<MyFile> selectPage(int pageNum,int pageSize,String fileName) {
        IPage<MyFile> filesPage = new Page<>(pageNum, pageSize);
        QueryWrapper<MyFile> filesQueryWrapper = new QueryWrapper<>();
        // 替换Util.isEmptyString()为StrUtil.isEmpty()
        if(!StrUtil.isEmpty(fileName)){
            filesQueryWrapper.like("name",fileName);
        }
        filesQueryWrapper.eq("is_delete",false);
        filesQueryWrapper.orderByDesc("id");
        return page(filesPage, filesQueryWrapper);
    }

    public int changeEnable(int id, boolean enable) {
        UpdateWrapper<MyFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id).set("enable",enable);
        return fileMapper.update(null, updateWrapper);
    }
}
