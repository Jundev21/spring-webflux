package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoomSearchRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String title;
    int page;
    int size;

    public RoomSearchRequest(String title, int page, int size) {
        this.title = title;
        this.page = page;
        this.size = size;
    }
    public RoomSearchRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }
}