package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    /**
     * Обработка ошибок валидации Bean Validation (аннотации @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Получена ошибка валидации запроса");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.error("Ошибка валидации поля '{}': {}", fieldName, errorMessage);
        });
        log.debug("Всего ошибок валидации: {}", errors.size());
        return errors;
    }

    /**
     * Обработка ошибок валидации параметров запроса.
     * Например: @Positive, при попытке ввести текст, а не число - пользователь получит именно это сообщение.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleParamValidationExceptions(MethodArgumentTypeMismatchException ex) {
        String error = "Param " + ex.getPropertyName() + " violates validation rule";
        return Map.of("error", error);
    }

    /**
     * Обработка ошибок валидации параметров запроса.
     * Например: @Min, при попытке ввести число меньше заданного пользователь получить сообщение указанное в
     * аннотации. Или дефолтное.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleParamValidationExceptions(ConstraintViolationException ex) {
        return Map.of("error", ex.getMessage());
    }

    /**
     * Обработка кастомных исключений валидации - 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    /**
     * Обработка исключений "Не найдено" - 404 Not Found
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        log.warn("Объект не найден: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    /**
     * Обработка IllegalArgumentException - 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Некорректный аргумент: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(BadRequestException ex) {
        log.warn("Ошибка в параметрах запроса: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    /**
     * Обработка нарушения ограничений целостности БД (например, FK constraint)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        log.warn("Нарушение целостности данных: {}", message);

        // Определяем, какое поле вызвало ошибку
        if (message != null) {
            if (message.contains("MPA_ID") || message.contains("mpa_id")) {
                return Map.of("error", "Рейтинг MPA не найден");
            } else if (message.contains("GENRE_ID") || message.contains("genre_id")) {
                return Map.of("error", "Жанр не найден");
            }
        }

        return Map.of("error", "Указаны некорректные данные");
    }

    /**
     * Обработка всех остальных исключений - 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalServerError(Exception ex) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        return Map.of("error", "Произошла внутренняя ошибка сервера");
    }
}
