package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @Null(groups = OnCreate.class, message = "id не передается при создании")
    @NotNull(groups = OnUpdate.class, message = "id должен быть передан при обновлении")
    @Positive(groups = OnUpdate.class, message = "id должен быть положительным")
    private Integer id;
    @NotBlank(message = "Имя режиссера должно быть заполнено")
    private String name;
}
