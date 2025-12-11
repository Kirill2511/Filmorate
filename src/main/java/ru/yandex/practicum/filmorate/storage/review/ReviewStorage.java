package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Integer reviewId);

    Review findById(Integer reviewId);

    List<Review> findAll();

    List<Review> findByFilmId(Integer filmId, int count);

    void addLike(Integer reviewId, Integer userId);

    void addDislike(Integer reviewId, Integer userId);

    void removeLike(Integer reviewId, Integer userId);

    void removeDislike(Integer reviewId, Integer userId);

    Integer getUsefulRating(Integer reviewId);
}
