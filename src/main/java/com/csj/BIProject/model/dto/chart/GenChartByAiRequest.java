package com.csj.BIProject.model.dto.chart;


import lombok.Data;

@Data
public class GenChartByAiRequest {

    private String chartName;//图标的名字
    private String goal;//需要分析的目标
    private String chartType;//类型
}
