package ru.yandex.practicum.filmorate.exception;

/**
 * Исключение для случаев, когда запрашиваемый объект не найден
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
