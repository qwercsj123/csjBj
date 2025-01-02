package com.csj.BIProject.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.csj.BIProject.model.dto.chart.ChartQueryRequest;
import com.csj.BIProject.model.dto.chart.QueryChartResponse;
import com.csj.BIProject.model.dto.post.PostQueryRequest;
import com.csj.BIProject.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.csj.BIProject.model.entity.Post;

import javax.servlet.http.HttpServletRequest;

/**
* @author 23200
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-12-26 19:16:39
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest, HttpServletRequest request);

}
