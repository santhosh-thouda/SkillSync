package com.capgemini.group.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String content;

    /**
     * Serialise as ISO-8601 string ("2025-05-01T10:30:00") so the Angular
     * DatePipe and new Date() can parse it directly without array-unwrapping.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;
}
