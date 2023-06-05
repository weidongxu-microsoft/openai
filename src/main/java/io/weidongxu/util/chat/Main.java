// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.weidongxu.util.chat;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            System.out.flush();
            String input = sc.nextLine();
            if ("exit".equals(input)) {
                break;
            } else {
                List<ChatMessage> conversation = getConversation(input);
                String reply = chatComplete(conversation);
                addReply(reply);
                System.out.println("< " + reply);
            }
        }
    }

    private static final List<ChatMessage> conversation = new CopyOnWriteArrayList<>();
    static {
        conversation.add(new ChatMessage(ChatRole.SYSTEM)
                .setContent("The following is a conversation with an AI assistant. The assistant is helpful, creative, clever, and very friendly."));
    }

    private static List<ChatMessage> getConversation(String input) {
        conversation.add(new ChatMessage(ChatRole.USER).setContent(input));
        return conversation;
    }

    private static void addReply(String reply) {
        conversation.add(new ChatMessage(ChatRole.ASSISTANT).setContent(reply));
    }

    private static OpenAIClient completionsClient;

    private static String chatComplete(List<ChatMessage> messages) {
        if (completionsClient == null) {
            completionsClient = new OpenAIClientBuilder()
                    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                    .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("API_KEY")))
                    .buildClient();
        }

        String modelId = "gpt-4";
        ChatCompletions completions = completionsClient.getChatCompletions(modelId,
                new ChatCompletionsOptions(messages)
                        .setMaxTokens(1024));
        return completions.getChoices().get(0).getMessage().getContent();
    }
}
