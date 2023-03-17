// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.ai.openai.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the token counts processed for a completions request. Counts consider all tokens across prompts,
 * choices, choice alternates, best_of generations, and other consumers.
 */
@Immutable
public final class CompletionsUsage {
    /*
     * Number of tokens received in the completion
     */
    @JsonProperty(value = "completion_tokens", required = true)
    private int completionTokens;

    /*
     * Number of tokens sent in the original request
     */
    @JsonProperty(value = "prompt_tokens", required = true)
    private int promptTokens;

    /*
     * Total number of tokens transacted in this request/response
     */
    @JsonProperty(value = "total_tokens", required = true)
    private int totalTokens;

    /**
     * Creates an instance of CompletionsUsage class.
     *
     * @param completionTokens the completionTokens value to set.
     * @param promptTokens the promptTokens value to set.
     * @param totalTokens the totalTokens value to set.
     */
    @JsonCreator
    private CompletionsUsage(
            @JsonProperty(value = "completion_tokens", required = true) int completionTokens,
            @JsonProperty(value = "prompt_tokens", required = true) int promptTokens,
            @JsonProperty(value = "total_tokens", required = true) int totalTokens) {
        this.completionTokens = completionTokens;
        this.promptTokens = promptTokens;
        this.totalTokens = totalTokens;
    }

    /**
     * Get the completionTokens property: Number of tokens received in the completion.
     *
     * @return the completionTokens value.
     */
    public int getCompletionTokens() {
        return this.completionTokens;
    }

    /**
     * Get the promptTokens property: Number of tokens sent in the original request.
     *
     * @return the promptTokens value.
     */
    public int getPromptTokens() {
        return this.promptTokens;
    }

    /**
     * Get the totalTokens property: Total number of tokens transacted in this request/response.
     *
     * @return the totalTokens value.
     */
    public int getTotalTokens() {
        return this.totalTokens;
    }
}