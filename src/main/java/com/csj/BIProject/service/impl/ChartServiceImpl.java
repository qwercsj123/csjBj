package com.csj.BIProject.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csj.BIProject.constant.CommonConstant;
import com.csj.BIProject.model.dto.chart.ChartQueryRequest;
import com.csj.BIProject.model.dto.post.PostQueryRequest;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.model.entity.Post;
import com.csj.BIProject.service.ChartService;
import com.csj.BIProject.mapper.ChartMapper;
import com.csj.BIProject.service.UserService;
import com.csj.BIProject.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 23200
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-12-26 19:16:39
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Resource
    private UserService userService;

    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {

        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }


        String chartName = chartQueryRequest.getChartName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = userService.getLoginUser(request).getId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 拼接查询条件(模糊查询)
        if (StringUtils.isNotBlank(chartName)) {
            queryWrapper.and(qw -> qw.like("chartName", chartName).or().like("chartType",chartType));  //根据chartName或者图标类型来查询
        }
        queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);//根据goal来查询来查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId); //查询当前用户创建的表格

        queryWrapper.eq(ObjectUtils.isNotEmpty(chartType), "chartType", chartType);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}




