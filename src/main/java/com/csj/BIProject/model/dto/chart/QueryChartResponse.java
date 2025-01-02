package com.csj.BIProject.model.dto.chart;


import lombok.Data;

@Data
/**
 * 根据ai生成的数据然后返回给前端
 */
public class QueryChartResponse {

    private Long id;

    private String goal;

    private String chartName;

    private String chartType;

    private String genChart; //chart的代码

    private String  genResult; //ai分析之后的结论

}
