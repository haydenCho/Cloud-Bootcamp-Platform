package com.solcho.bootcamp.content.dto;

import com.solcho.bootcamp.content.entity.Content;

public record ContentResponse(
        Long id,
        String unitCode,
        String title,
        String body
) {
    public static ContentResponse of(String unitCode, Content content) {
        return new ContentResponse(content.getId(), unitCode, content.getTitle(), content.getBody());
    }
}
