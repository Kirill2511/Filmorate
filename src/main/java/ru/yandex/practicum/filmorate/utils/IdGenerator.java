package ru.yandex.practicum.filmorate.utils;

public class IdGenerator {
    private int nextId = 1;

    public int getNextId() {
        return nextId++;
    }
}
