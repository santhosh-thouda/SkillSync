package com.capgemini.group.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String status; // READ, DELIVERED

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
