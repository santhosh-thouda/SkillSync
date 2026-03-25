package com.capgemini.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent {
    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionTime;
    private String status;
}
