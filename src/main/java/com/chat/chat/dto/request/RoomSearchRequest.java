package com.chat.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * "유저"의 방을 불러오기 위해서는 2개의 인수가 인는 오버로드를 쓰고
 *  모든 방의 검색을 하기 위해서는 3개의 인수가 있는 오버로드를 씁니다
 */
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