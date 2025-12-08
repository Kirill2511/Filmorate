package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    /**
     * Создать отзыв
     */
    public Review createReview(Review review) {
        // Проверяем существование пользователя и фильма
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());

        Review createdReview = reviewStorage.create(review);
        log.info("Создан отзыв: id={}, filmId={}, userId={}", 
                createdReview.getReviewId(), createdReview.getFilmId(), createdReview.getUserId());
        return createdReview;
    }

    /**
     * Обновить отзыв
     */
    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("ID отзыва должен быть указан");
        }
        Review updatedReview = reviewStorage.update(review);
        log.info("Обновлён отзыв: id={}", updatedReview.getReviewId());
        return updatedReview;
    }

    /**
     * Удалить отзыв
     */
    public void deleteReview(Integer reviewId) {
        reviewStorage.delete(reviewId);
        log.info("Удалён отзыв: id={}", reviewId);
    }

    /**
     * Получить отзыв по ID
     */
    public Review getReviewById(Integer reviewId) {
        return reviewStorage.findById(reviewId);
    }

    /**
     * Получить отзывы по ID фильма или все отзывы
     * @param filmId ID фильма (если null, возвращает все отзывы)
     * @param count количество отзывов (по умолчанию 10)
     */
    public List<Review> getReviews(Integer filmId, Integer count) {
        int limit = (count != null && count > 0) ? count : 10;
        
        if (filmId != null) {
            // Проверяем существование фильма
            filmService.getFilmById(filmId);
        }
        
        return reviewStorage.findByFilmId(filmId, limit);
    }

    /**
     * Поставить лайк отзыву
     */
    public void addLike(Integer reviewId, Integer userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        reviewStorage.addLike(reviewId, userId);
        
        Integer useful = reviewStorage.getUsefulRating(reviewId);
        log.info("Пользователь {} поставил лайк отзыву {} (рейтинг: {})", 
                userId, reviewId, useful);
    }

    /**
     * Поставить дизлайк отзыву
     */
    public void addDislike(Integer reviewId, Integer userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        reviewStorage.addDislike(reviewId, userId);
        
        Integer useful = reviewStorage.getUsefulRating(reviewId);
        log.info("Пользователь {} поставил дизлайк отзыву {} (рейтинг: {})", 
                userId, reviewId, useful);
    }

    /**
     * Удалить лайк у отзыва
     */
    public void removeLike(Integer reviewId, Integer userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        reviewStorage.removeLike(reviewId, userId);
        
        Integer useful = reviewStorage.getUsefulRating(reviewId);
        log.info("Пользователь {} удалил лайк у отзыва {} (рейтинг: {})", 
                userId, reviewId, useful);
    }

    /**
     * Удалить дизлайк у отзыва
     */
    public void removeDislike(Integer reviewId, Integer userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        reviewStorage.removeDislike(reviewId, userId);
        
        Integer useful = reviewStorage.getUsefulRating(reviewId);
        log.info("Пользователь {} удалил дизлайк у отзыва {} (рейтинг: {})", 
                userId, reviewId, useful);
    }
}
