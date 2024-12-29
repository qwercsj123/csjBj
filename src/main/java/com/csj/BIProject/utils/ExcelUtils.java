package com.csj.BIProject.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelUtils {
    public static void main(String[] args) {

    }
    public static  StringBuilder getData(MultipartFile multipartFile){

        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (CollectionUtil.isEmpty(list)){
            return null;
        }
        StringBuilder stringBuilder=new StringBuilder();

        LinkedHashMap<Integer, String> hashMap = (LinkedHashMap) list.get(0);
        List<String> collect = hashMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());


        stringBuilder.append(StringUtils.join(collect,',')).append("\n");

        for (int i=1;i<list.size();i++){
            Map<Integer, String> integerStringMap = list.get(i);
            List<String> dataList = integerStringMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,',')).append("\n");
        }

        return stringBuilder;
    }
}
