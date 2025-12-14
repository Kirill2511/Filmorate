package ru.yandex.practicum.filmorate.controller.params;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.BadRequestException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Конвертер, который позволяет Spring автоматически преобразовывать
 * строковый параметр запроса в Set<SearchBy>.
 * Пример: "title,director" -> Set.of(SearchBy.TITLE, SearchBy.DIRECTOR)
 */
@Component
public class StringToSearchBySetConverter implements Converter<String, Set<SearchBy>> {
    @Override
    public Set<SearchBy> convert(String source) {
        try {
            return Arrays.stream(source.split(","))
                    .map(String::toUpperCase)
                    .map(String::trim)
                    .map(SearchBy::valueOf)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(SearchBy.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(", "));
            throw new BadRequestException("Неверный параметр запроса by. Допустимые значения: " + validValues);
        }
    }
}
