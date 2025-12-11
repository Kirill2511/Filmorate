package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.FeedEvent;
import ru.yandex.practicum.filmorate.model.feed.Operation;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private static final String BASE_SELECT = """
            SELECT
                f.event_id,
                f.user_id,
                f.entity_id,
                f.event_type,
                f.operation,
                f.created_at
            FROM feed f
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public FeedEvent create(FeedEvent event) {

        String sql = "INSERT INTO feed (user_id, entity_id, event_type, operation) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
            ps.setInt(1, event.getUserId());
            ps.setInt(2, event.getEntityId());
            ps.setString(3, event.getEventType().toString());
            ps.setString(4, event.getOperation().toString());
            return ps;
        }, keyHolder);

        event.setId(keyHolder.getKey().intValue());

        log.debug("Создан фильм с id: {}", event.getId());
        return findByEventId(event.getId());
    }

    @Override
    public FeedEvent findByEventId(Integer eventId) {
        String sql = BASE_SELECT + "\nWHERE event_id = ?";

        List<FeedEvent> events = jdbcTemplate.query(sql, eventRowMapper(), eventId);

        if (events.isEmpty()) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }

        return events.getFirst();
    }

    @Override
    public Collection<FeedEvent> findAllByUserId(Integer userId) {
        String sql = BASE_SELECT + "\nWHERE user_id = ?\nORDER BY f.created_at ASC";

        List<FeedEvent> events = jdbcTemplate.query(sql, eventRowMapper(), userId);

        if (events.isEmpty()) {
            throw new NotFoundException("События пользователя с id " + userId + " не найдены");
        }

        log.debug("Получен список всех событий пользователя {}, количество: {}", userId, events.size());

        return events;
    }

    private RowMapper<FeedEvent> eventRowMapper() {
        return (rs, rowNum) -> {
            FeedEvent event = new FeedEvent();
            event.setId(rs.getInt("event_id"));
            event.setUserId(rs.getInt("user_id"));
            event.setEntityId(rs.getInt("entity_id"));
            event.setEventType(EventType.valueOf(rs.getString("event_type")));
            event.setOperation(Operation.valueOf(rs.getString("operation")));
            event.setTimestamp(rs.getTimestamp("created_at").getTime());

            return event;
        };
    }
}
