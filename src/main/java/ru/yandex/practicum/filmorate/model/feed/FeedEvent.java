package ru.yandex.practicum.filmorate.model.feed;

import lombok.Data;

@Data
public class FeedEvent {
    private Integer id;
    private Integer userId;
    private Integer entityId;
    private EventType eventType;
    private Operation operation;
    private Long timestamp;
}
