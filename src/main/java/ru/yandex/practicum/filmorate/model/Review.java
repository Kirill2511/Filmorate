package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    @Size(max = 1000, message = "Содержание отзыва не может превышать 1000 символов")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя должен быть указан")
    private Integer userId;

    @NotNull(message = "ID фильма должен быть указан")
    private Integer filmId;

    private Integer useful = 0; // Рейтинг полезности, по умолчанию 0
}
