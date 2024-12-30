package com.csj.BIProject.controller;
import com.csj.BIProject.common.BaseResponse;
import com.csj.BIProject.common.ErrorCode;
import com.csj.BIProject.common.ResultUtils;
import com.csj.BIProject.exception.BusinessException;
import com.csj.BIProject.model.dto.chart.GenChartByAiRequest;
import com.csj.BIProject.model.dto.chart.GenChartByAiResponse;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.model.entity.User;
import com.csj.BIProject.service.ChartService;
import com.csj.BIProject.service.UserService;
import com.csj.BIProject.utils.DouBaoUtils;
import com.csj.BIProject.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/chart")
public class chartController {


    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;

    @PostMapping("/gen")
    public BaseResponse<GenChartByAiResponse> genChartByAi(@RequestPart("file")MultipartFile multipartFile,
                                                           GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){
        StringBuilder stringBuilder=new StringBuilder();
        String chartName = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();


        User loginUser = userService.getLoginUser(request);

        if (StringUtils.isEmpty(chartName)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表名称为空");
        }
        if (StringUtils.isEmpty(goal)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"需要生成的目标为空");
        }
        if (StringUtils.isEmpty(chartType)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"类型为空");
        }

        StringBuilder data = ExcelUtils.getData(multipartFile);
        stringBuilder.append("分析需求:"+"\n").append(goal+"\n");
        stringBuilder.append("原始数据如下:"+"\n"+data);


        String message = DouBaoUtils.GenerateByAi(stringBuilder.toString());
        message=message.replace("\n","");
        message=message.replace("- ","\n");


        String[] splits = message.split("【【【【【");

        if (splits.length<3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成错误");
        }

        GenChartByAiResponse genChartByAiResponse = new GenChartByAiResponse();
        String genChart = splits[1];



        String genResult = splits[2];

        genChartByAiResponse.setGenChart(genChart);
        genChartByAiResponse.setGenResult(genResult);



        //将生成的数据保存到数据库中
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(data.toString());
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());

        boolean save = chartService.save(chart);

        if (!save){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表信息保存失败");
        }


        return ResultUtils.success(genChartByAiResponse);
    }

}
