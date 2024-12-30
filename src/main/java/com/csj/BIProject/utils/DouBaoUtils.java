package com.csj.BIProject.utils;

import cn.hutool.core.text.StrBuilder;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;

import java.util.ArrayList;
import java.util.List;


/**
 * 调用豆包的工具类
 */

public class DouBaoUtils {
    public static String GenerateByAi(String message) {


        StrBuilder strBuilder = new StrBuilder();
        String apiKey = "3b649479-251c-49c8-9857-2a5e86e6f27b";
        ArkService service = ArkService.builder().apiKey(apiKey).build();

        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM)
                .content("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                        "分析需求：\n" +
                        "{数据分析的需求或者目标}\n" +
                        "原始数据：\n" +
                        "{csv格式的原始数据，用,作为分隔符}\n" +
                        "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                        "【【【【【\n" +
                        "{生成前端 Echarts V5 的 option 配置对象的JSON数据，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                        "【【【【【\n" +
                        "{明确的数据分析结论、越详细越好，不要生成多余的注释}\n").build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER)
                        .content(message).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("ep-20241227202642-fkfs2")
                .messages(messages)
                .build();

        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(
                choice ->strBuilder.append(choice.getMessage().getContent()));

        // shutdown service
        service.shutdownExecutor();

        return strBuilder.toString();
    }

}