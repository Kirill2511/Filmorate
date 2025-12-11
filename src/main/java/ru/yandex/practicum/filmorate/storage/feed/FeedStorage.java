package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.feed.FeedEvent;

import java.util.Collection;

public interface FeedStorage {

    FeedEvent create(FeedEvent event);

    FeedEvent findByEventId(Integer eventId);

    Collection<FeedEvent> findAllByUserId(Integer userId);
}
