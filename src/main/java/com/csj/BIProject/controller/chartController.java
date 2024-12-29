package com.csj.BIProject.controller;

import com.csj.BIProject.common.BaseResponse;
import com.csj.BIProject.common.ErrorCode;
import com.csj.BIProject.common.ResultUtils;
import com.csj.BIProject.exception.BusinessException;
import com.csj.BIProject.model.dto.chart.GenChartByAiRequest;
import com.csj.BIProject.utils.DouBaoUtils;
import com.csj.BIProject.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chart")
public class chartController {

    @PostMapping("/gen")
    public BaseResponse<String> genChartByAi(@RequestPart("file")MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest){
        StringBuilder stringBuilder=new StringBuilder();
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        if (StringUtils.isEmpty(chartName)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图标名称为空");
        }
        if (StringUtils.isEmpty(goal)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"目标为空");
        }
//        if (StringUtils.isEmpty(chartType)){
//            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"类型为空");
//        }


        stringBuilder.append("你是一个数据分析师,我会给你一些数据，按照我的要求告诉我分析的结果。").append("\n");
        stringBuilder.append("分析目标:"+goal).append("\n");
        StringBuilder data = ExcelUtils.getData(multipartFile);
        stringBuilder.append("数据:"+data);

        String message = DouBaoUtils.GenerateByAi(stringBuilder.toString());
        message=message.replace("\n","");
        message=message.replace("- ","\n");
        return ResultUtils.success(message);
    }

}
