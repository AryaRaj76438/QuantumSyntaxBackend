package com.quantumsyntax.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String excerpt;

    private String coverImageUrl;

    private List<String> tags;

    @NotBlank
    private String category;

    @NotBlank
    private String status;
}
