package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public Collection<FeedEvent> getFeed(Integer userId) {
        userStorage.findById(userId);
        return feedStorage.findAllByUserId(userId);
    }

    public FeedEvent createEvent(Integer entityId, Integer userId, EventType eventType, Operation operation) {
        FeedEvent event = new FeedEvent();
        event.setEntityId(entityId);
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);

        return feedStorage.create(event);
    }
}
