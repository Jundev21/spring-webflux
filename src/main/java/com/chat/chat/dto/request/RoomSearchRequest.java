package com.chat.chat.dto.request;

import lombok.Data;

@Data
public class RoomSearchRequest {
    String title;
    int page;
    int size;
}
