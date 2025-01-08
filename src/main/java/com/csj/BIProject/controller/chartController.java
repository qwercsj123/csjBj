package com.csj.BIProject.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csj.BIProject.Rabitmq.BiProducer;
import com.csj.BIProject.common.BaseResponse;
import com.csj.BIProject.common.ErrorCode;
import com.csj.BIProject.common.ResultUtils;
import com.csj.BIProject.config.ThreadPoolExecutorConfig;
import com.csj.BIProject.exception.BusinessException;
import com.csj.BIProject.model.dto.chart.ChartQueryRequest;
import com.csj.BIProject.model.dto.chart.GenChartByAiRequest;
import com.csj.BIProject.model.dto.chart.GenChartByAiResponse;
import com.csj.BIProject.model.dto.chart.QueryChartResponse;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.model.entity.User;
import com.csj.BIProject.model.enums.ChartStatusEnum;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/chart")
public class chartController {

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;


    @Resource
    private ThreadPoolExecutorConfig threadPoolExecutorConfig;



    @Resource
    private BiProducer biProducer;


    /**
     * 利用ai自动生成图表的代码和分析结果（同步生成结果）
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
     * 利用ai自动生成图表的代码和分析结果（异步生产结果）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/asyc")
    public BaseResponse<GenChartByAiResponse> genChartByAiAsyc(@RequestPart("file")MultipartFile multipartFile,
                                                           GenChartByAiRequest genChartByAiRequest, HttpServletRequest request){

        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorConfig.threadPoolExecutor();


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


        //将生成的数据保存到数据库中
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(data.toString());
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());

        boolean save = chartService.save(chart);

        if (!save){
            //报错失败的话   要写入数据库
            boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
            if (!result){
                throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
            }
        }

        GenChartByAiResponse genChartByAiResponse = new GenChartByAiResponse();


        //进行异步执行方法的地方
        CompletableFuture.runAsync(()->{

            /**
             * 此需需要进行等待
             */
            if (threadPoolExecutor.getCorePoolSize()==2 && threadPoolExecutor.getMaximumPoolSize()==4 &&
                    threadPoolExecutor.getQueue().size()==5){


                Chart updateChart = new Chart();
                updateChart.setStatus(ChartStatusEnum.WAITING.getValue());
                updateChart.setId(chart.getId());
                boolean newResult = chartService.updateById(updateChart);
                if (!newResult){
                    boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
                    if (!result){
                        throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
                    }
                }
            }



            Chart updateChart = new Chart();
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            updateChart.setId(chart.getId());
            boolean newResult = chartService.updateById(updateChart);
            if (!newResult){
                boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
                if (!result){
                    throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
                }
            }




            String message = DouBaoUtils.GenerateByAi(stringBuilder.toString());
            message=message.replace("\n","");
            message=message.replace("- ","\n");


            String[] splits = message.split("【【【【【");

            if (splits.length<3){
                saveErrorStatus(chart.getId(),"AI生产的数据根式类型错误");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成错误");
            }
            String genChart = splits[1];
            String genResult = splits[2];

            genChartByAiResponse.setGenChart(genChart);
            genChartByAiResponse.setGenResult(genResult);


            /**
             * 生成成功之后要将状态保存到数据库中
             */

            Chart updateChart1 = new Chart();
            updateChart1.setStatus(ChartStatusEnum.SUCCESS.getValue());
            updateChart1.setId(chart.getId());
            updateChart1.setGenChart(genChart);
            updateChart1.setGenResult(genResult);

            boolean newResult1 = chartService.updateById(updateChart1);
            if (!newResult1){
                boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
                if (!result){
                    throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
                }
            }


        }, threadPoolExecutor);



        genChartByAiResponse.setChartId(chart.getId());
        return ResultUtils.success(genChartByAiResponse);
    }



    /**
     * 利用ai自动生成图表的代码和分析结果（利用消息队列来实现）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/asyc/mq")
    public BaseResponse<GenChartByAiResponse> genChartByAiAsycMq(@RequestPart("file")MultipartFile multipartFile,
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


        //将生成的数据保存到数据库中
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(data.toString());
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());

        boolean save = chartService.save(chart);

        if (!save){
            //报错失败的话   要写入数据库
            boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
            if (!result){
                throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
            }
        }

        GenChartByAiResponse genChartByAiResponse = new GenChartByAiResponse();

        biProducer.sendMessage(chart.getId().toString());

        return ResultUtils.success(genChartByAiResponse);
    }


    /**
     * 下面的方法是针对一切失败的情况
     * @param chartId
     * @param executorMessage
     * @return
     */
    public boolean  saveErrorStatus(Long  chartId,String executorMessage){
        Chart updateChart = new Chart();
        updateChart.setId(chartId);//设置id
        updateChart.setExecuteMessage(executorMessage);//设置报错之后的信息
        updateChart.setStatus(ChartStatusEnum.FAILURE.getValue());//设置当前的状态
        boolean result = chartService.updateById(updateChart);//进行保存

        return  result;
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
