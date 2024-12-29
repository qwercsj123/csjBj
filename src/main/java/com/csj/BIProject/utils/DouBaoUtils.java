package com.csj.BIProject.utils;

import cn.hutool.core.text.StrBuilder;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;

import java.util.ArrayList;
import java.util.List;

public class DouBaoUtils {
    public static String GenerateByAi(String message) {


        StrBuilder strBuilder = new StrBuilder();
        String apiKey = "3b649479-251c-49c8-9857-2a5e86e6f27b";
        ArkService service = ArkService.builder().apiKey(apiKey).build();

        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM)
                .content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build();
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