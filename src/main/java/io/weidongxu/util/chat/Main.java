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
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.ModelType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {

    private static final ClientLogger LOGGER = new ClientLogger(Main.class);

    private static final String MODEL_ID = "gpt-4";
    private static final int MODEL_TOKEN_LIMIT = 8193;
    private static final int RESPONSE_TOKEN_LIMIT = 1023;

    private static final Encoding ENCODING = Encodings.newDefaultEncodingRegistry().getEncodingForModel(ModelType.GPT_4);

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        initOpenAIClient();
        updatePrompt();

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

    private static void updatePrompt() {
        String quickStartUrl = "https://raw.githubusercontent.com/wiki/Azure/azure-sdk-for-java/TypeSpec-Java-Quickstart.md";

        String markdownStr = new HttpPipelineBuilder().build()
                .sendSync(new HttpRequest(HttpMethod.GET, quickStartUrl), Context.NONE)
                .getBodyAsString(StandardCharsets.UTF_8).block();

        conversation.clear();
        conversation.add(new ChatMessage(ChatRole.SYSTEM)
                .setContent(
                        "Please answer question based on the fact included in triple quotes below.\n" +
                        "\"\"\"" +
                        markdownStr +
                        "\"\"\""
                ));
    }

    private static List<ChatMessage> getConversation(String input) {
        conversation.add(new ChatMessage(ChatRole.USER).setContent(input));
        trimConversation();
        return conversation;
    }

    private static void addReply(String reply) {
        conversation.add(new ChatMessage(ChatRole.ASSISTANT).setContent(reply));
    }

    private static void trimConversation() {
        int remainingTokenCount = MODEL_TOKEN_LIMIT - RESPONSE_TOKEN_LIMIT;

        remainingTokenCount -= 100; // there seems certain discrepancy about token count between local and backend

        int systemTokenCount = conversation.stream()
                .filter(m -> m.getRole() == ChatRole.SYSTEM)
                .map(m -> ENCODING.countTokens(m.getContent()))
                .reduce(0, Integer::sum);
        LOGGER.verbose("system token count: {}", systemTokenCount);

        remainingTokenCount -= systemTokenCount;

        int index = conversation.size() - 1;
        for (; index > 0; --index) {
            ChatMessage message = conversation.get(index);
            if (message.getRole() != ChatRole.SYSTEM) {
                int tokens = ENCODING.countTokens(message.getContent());

                remainingTokenCount -= tokens;
                if (remainingTokenCount <= 0) {
                    break;
                }
            }
        }

        if (index > 0) {
            LOGGER.info("discard messages: {}", index);

            List<ChatMessage> trimmedConversation = new ArrayList<>();
            trimmedConversation.add(conversation.get(0));
            trimmedConversation.addAll(conversation.subList(index + 1, conversation.size()));

            conversation.clear();
            conversation.addAll(trimmedConversation);
        } else {
            LOGGER.verbose("remaining token count: {}", remainingTokenCount);
        }
    }

    private static OpenAIClient completionsClient;

    private static void initOpenAIClient() {
        completionsClient = new OpenAIClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("API_KEY")))
                .buildClient();
    }

    private static String chatComplete(List<ChatMessage> messages) {
        ChatCompletions completions = completionsClient.getChatCompletions(MODEL_ID,
                new ChatCompletionsOptions(messages)
                        .setMaxTokens(1024));
        return completions.getChoices().get(0).getMessage().getContent();
    }
}
