package com.csj.BIProject.Rabitmq;

public  class  BiConstant {

    public static  String EXCHANGE_NAME="BI_EXCHANGE"; //普通交换机

    public static final String QUEUE_NAME="BI_QUEUE"; //普通队列

    public static  String ROUTINGKEY="bi_routingKey"; //绑定的键

    public static  String Dlq_Routing_Key="dlq_routingKey"; //绑定的键

    public static final String DLQ_QUEUE = "dlq_queue";  //死信队列




}
