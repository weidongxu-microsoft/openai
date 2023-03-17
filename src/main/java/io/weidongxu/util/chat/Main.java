// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.weidongxu.util.chat;

import com.azure.ai.openai.OpenAiClient;
import com.azure.ai.openai.OpenAiClientBuilder;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

import java.util.Collections;
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
                String conversation = getConversation(input);
                String reply = complete(conversation);
                addReply(reply);
                System.out.println("< " + reply);
            }
        }
    }

    private static final List<String> conversation = new CopyOnWriteArrayList<>();
    static {
        conversation.add("<|im_start|>system\nThe following is a conversation with an AI assistant. The assistant is helpful, creative, clever, and very friendly.\n<|im_end|>");
    }

    private static String getConversation(String input) {
        conversation.add("<|im_start|>user\n" + input + "\n<|im_end|>");
        return String.join("\n", conversation) + "\n<|im_start|>assistant\n";
    }

    private static void addReply(String reply) {
        conversation.add("<|im_start|>assistant\n" + reply + "\n<|im_end|>");
    }

    private static OpenAiClient completionsClient;

    private static String complete(String prompt) {
        if (completionsClient == null) {
            completionsClient = new OpenAiClientBuilder()
                    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
                    .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("API_KEY")))
                    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .buildClient();
        }

        Completions completions = completionsClient.getCompletions("gpt-35-turbo",
                new CompletionsOptions()
                        .setMaxTokens(512)
                        .setStop(Collections.singletonList("<|im_end|>"))
                        .setPrompt(Collections.singletonList(prompt)));
        return completions.getChoices().get(0).getText();
    }
}
