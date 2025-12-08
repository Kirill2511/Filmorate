package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, 0); // Изначально рейтинг = 0
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);

        log.debug("Создан отзыв с id: {}", review.getReviewId());
        return review;
    }

    @Override
    public Review update(Review review) {
        findById(review.getReviewId()); // Проверяем существование

        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        log.debug("Обновлён отзыв с id: {}", review.getReviewId());
        return findById(review.getReviewId());
    }

    @Override
    public void delete(Integer reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, reviewId);

        if (rowsAffected == 0) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }

        log.debug("Удалён отзыв с id: {}", reviewId);
    }

    @Override
    public Review findById(Integer reviewId) {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews WHERE review_id = ?";

        List<Review> reviews = jdbcTemplate.query(sql, reviewRowMapper(), reviewId);

        if (reviews.isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }

        log.debug("Получен отзыв с id: {}", reviewId);
        return reviews.getFirst();
    }

    @Override
    public List<Review> findAll() {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews ORDER BY useful DESC";

        List<Review> reviews = jdbcTemplate.query(sql, reviewRowMapper());

        log.debug("Получен список всех отзывов, количество: {}", reviews.size());
        return reviews;
    }

    @Override
    public List<Review> findByFilmId(Integer filmId, int count) {
        String sql;
        List<Review> reviews;

        if (filmId == null) {
            sql = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                    "FROM reviews ORDER BY useful DESC LIMIT ?";
            reviews = jdbcTemplate.query(sql, reviewRowMapper(), count);
        } else {
            sql = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                    "FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            reviews = jdbcTemplate.query(sql, reviewRowMapper(), filmId, count);
        }

        log.debug("Получен список отзывов для фильма {}, количество: {}", filmId, reviews.size());
        return reviews;
    }

    @Override
    public void addLike(Integer reviewId, Integer userId) {
        findById(reviewId); // Проверяем существование отзыва

        // Удаляем предыдущую оценку пользователя, если она была
        String deleteSql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        // Добавляем лайк
        String insertSql = "INSERT INTO review_ratings (review_id, user_id, is_like) VALUES (?, ?, TRUE)";
        jdbcTemplate.update(insertSql, reviewId, userId);

        // Обновляем рейтинг
        updateUsefulRating(reviewId);

        log.debug("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    @Override
    public void addDislike(Integer reviewId, Integer userId) {
        findById(reviewId); // Проверяем существование отзыва

        // Удаляем предыдущую оценку пользователя, если она была
        String deleteSql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteSql, reviewId, userId);

        // Добавляем дизлайк
        String insertSql = "INSERT INTO review_ratings (review_id, user_id, is_like) VALUES (?, ?, FALSE)";
        jdbcTemplate.update(insertSql, reviewId, userId);

        // Обновляем рейтинг
        updateUsefulRating(reviewId);

        log.debug("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    @Override
    public void removeLike(Integer reviewId, Integer userId) {
        removeRating(reviewId, userId, true);
        log.debug("Пользователь {} удалил лайк у отзыва {}", userId, reviewId);
    }

    @Override
    public void removeDislike(Integer reviewId, Integer userId) {
        removeRating(reviewId, userId, false);
        log.debug("Пользователь {} удалил дизлайк у отзыва {}", userId, reviewId);
    }

    @Override
    public Integer getUsefulRating(Integer reviewId) {
        String sql = "SELECT useful FROM reviews WHERE review_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
    }

    private void removeRating(Integer reviewId, Integer userId, Boolean isLike) {
        findById(reviewId); // Проверяем существование отзыва

        String sql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND is_like = ?";
        jdbcTemplate.update(sql, reviewId, userId, isLike);

        // Обновляем рейтинг
        updateUsefulRating(reviewId);
    }

    private void updateUsefulRating(Integer reviewId) {
        String sql = "UPDATE reviews SET useful = (" +
                "SELECT COALESCE(SUM(CASE WHEN is_like = TRUE THEN 1 ELSE -1 END), 0) " +
                "FROM review_ratings WHERE review_id = ?" +
                ") WHERE review_id = ?";

        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    private RowMapper<Review> reviewRowMapper() {
        return (rs, rowNum) -> {
            Review review = new Review();
            review.setReviewId(rs.getInt("review_id"));
            review.setContent(rs.getString("content"));
            review.setIsPositive(rs.getBoolean("is_positive"));
            review.setUserId(rs.getInt("user_id"));
            review.setFilmId(rs.getInt("film_id"));
            review.setUseful(rs.getInt("useful"));
            return review;
        };
    }
}
