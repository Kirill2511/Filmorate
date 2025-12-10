package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    private Integer id;
    @NotBlank(message = "Имя режиссера должно быть заполнено")
    private String name;
}
