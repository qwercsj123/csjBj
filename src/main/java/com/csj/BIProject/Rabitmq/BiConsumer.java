package com.csj.BIProject.Rabitmq;
import com.csj.BIProject.common.ErrorCode;
import com.csj.BIProject.config.ThreadPoolExecutorConfig;
import com.csj.BIProject.exception.BusinessException;
import com.csj.BIProject.model.dto.chart.GenChartByAiResponse;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.model.enums.ChartStatusEnum;
import com.csj.BIProject.service.ChartService;
import com.csj.BIProject.utils.DouBaoUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class BiConsumer {
    @Resource
    private ThreadPoolExecutorConfig threadPoolExecutorConfig;
    @Resource
    private ChartService chartService;


    //从消息队列中取数据
    @RabbitListener(queues= {BiConstant.QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String messageInfo, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        long chartId=Long.parseLong(messageInfo);
        log.info("id为:"+chartId);


        Chart chart = chartService.getById(chartId);

        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求:"+"\n").append(goal+"\n").append("并且使用"+chartType+"\n"+"类型来展示");
        stringBuilder.append("原始数据如下:"+"\n"+csvData);

        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorConfig.threadPoolExecutor();

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
                channel.basicNack(deliveryTag,false,false); //生成失败
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成错误");
            }
            String genChart = splits[1];
            String genResult = splits[2];



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
                channel.basicNack(deliveryTag,false,false);
                boolean result = saveErrorStatus(chart.getId(), "数据库保存信息失败");
                if (!result){
                    throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
                }
            }
            channel.basicAck(deliveryTag,false);
    }

    /**
     * 死信队列的实现
     * @param messageInfo
     * @param channel
     * @param deliveryTag
     * @throws IOException
     */
    @RabbitListener(queues = BiConstant.DLQ_QUEUE,ackMode = "MANUAL")
    public void receiveDeadLetterMessage(String messageInfo,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        long chartId=Long.parseLong(messageInfo);
        Chart chart = new Chart();
        chart.setStatus(ChartStatusEnum.FAILURE.getValue());
        chart.setId(chartId);
        chart.setExecuteMessage("图表生成失败");

        boolean result = chartService.updateById(chart);
        if (!result){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"图表状态保存失败");
        }
        channel.basicNack(deliveryTag,false,false);
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

}