package com.csj.BIProject.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csj.BIProject.common.BaseResponse;
import com.csj.BIProject.common.ErrorCode;
import com.csj.BIProject.common.ResultUtils;
import com.csj.BIProject.exception.BusinessException;
import com.csj.BIProject.model.dto.chart.ChartQueryRequest;
import com.csj.BIProject.model.dto.chart.GenChartByAiRequest;
import com.csj.BIProject.model.dto.chart.GenChartByAiResponse;
import com.csj.BIProject.model.dto.chart.QueryChartResponse;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.model.entity.User;
import com.csj.BIProject.service.ChartService;
import com.csj.BIProject.service.UserService;
import com.csj.BIProject.utils.DouBaoUtils;
import com.csj.BIProject.utils.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chart")
public class chartController {

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;


    /**
     * 利用ai自动生成图表的代码和分析结果
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
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


        /**
         * 控制用户上传文件的大小
         */
        final Long MAX_LENGTH=1024*1024l;

        long size = multipartFile.getSize();
        if (size>MAX_LENGTH){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"您上传的文件过大");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> suffixList= Arrays.asList("xlsx","xls");
        if (!suffixList.contains(suffix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请上传excel格式的文件");
        }
        StringBuilder data = ExcelUtils.getData(multipartFile);
        stringBuilder.append("分析需求:"+"\n").append(goal+"\n").append("并且使用"+chartType+"\n"+"类型来展示");
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


        genChartByAiResponse.setChartId(chart.getId());
        return ResultUtils.success(genChartByAiResponse);
    }



    /**
     * 分页查询生成的表格
     * @param
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QueryChartResponse>> listPostByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                                 HttpServletRequest request) {

        List<QueryChartResponse> dataList=new ArrayList<>();
        long current = chartQueryRequest.getCurrent(); //当前页面
        long size = chartQueryRequest.getPageSize();//一个页面展示多少数据
        Page<Chart> postPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest,request));

        List<Chart> records = postPage.getRecords();
        for (Chart data:records){
            QueryChartResponse queryChartResponse = new QueryChartResponse();
            BeanUtil.copyProperties(data,queryChartResponse);

            dataList.add(queryChartResponse);
        }
        Page<QueryChartResponse> pageInfo=new Page<>();

        BeanUtil.copyProperties(postPage,pageInfo,"records");

        pageInfo.setRecords(dataList);

        return ResultUtils.success(pageInfo);
    }

}
