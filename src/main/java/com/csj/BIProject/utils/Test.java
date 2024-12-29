package com.csj.BIProject.utils;

import org.apache.commons.lang3.StringUtils;

public class Test {


    public static void main(String[] args) {


        String string="从给出的数据来看，可以得出以下初步分析结果：\\n\\n- 用户数随着日期呈现递" +
                "增的趋势，从 10 号的 10 个用户，到 20 号增加到 20 个用户，再到 30 号进一步增加到 30 个用户。\\n- 这可" +
                "能表明在这段时间内，业务或产品在吸引新用户方面有一定的成效，用户增长较为稳定。\\n\\n然而，仅根据这三个数据点，分" +
                "析具有一定的局限性，还需要更多的数据来进一步深入分析，比如：\\n- 其他日期的数据，以确" +
                "定这种增长趋势是否持续或存在波动。\\n- 同期的相关业务指标数据，来综合评估用户数增长的原因和影响。";
        string=string.replace("\\n","");
        string=string.replace("- ","\n");
        System.out.println(string);
    }
}
