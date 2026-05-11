package com.capgemini.group.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String content;
}
