package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение для ошибок валидации
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
