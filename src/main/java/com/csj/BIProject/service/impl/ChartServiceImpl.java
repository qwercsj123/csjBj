package com.csj.BIProject.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csj.BIProject.model.entity.Chart;
import com.csj.BIProject.service.ChartService;
import com.csj.BIProject.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author 23200
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-12-26 19:16:39
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




