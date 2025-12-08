# java-filmorate

Приложение для оценки фильмов и управления списком друзей.

## Схема базы данных

```mermaid
erDiagram
    USERS ||--o{ FRIENDSHIP : "initiates"
    USERS ||--o{ FRIENDSHIP : "receives"
    USERS ||--o{ FILM_LIKES : "likes"
    USERS ||--o{ REVIEWS : "writes"
    USERS ||--o{ REVIEW_RATINGS : "rates"
    FILMS ||--o{ FILM_LIKES : "has_likes"
    FILMS ||--o{ REVIEWS : "has_reviews"
    FILMS }o--|| MPA_RATING : "has_mpa"
    FILMS ||--o{ FILM_GENRE : "has_genres"
    GENRES ||--o{ FILM_GENRE : "assigned_to"
    REVIEWS ||--o{ REVIEW_RATINGS : "has_ratings"

    USERS {
        int user_id PK
        string email UK
        string login UK
        string name
        date birthday
    }

    FRIENDSHIP {
        int user_id PK,FK
        int friend_id PK,FK
        string status "UNCONFIRMED or CONFIRMED"
    }

    FILMS {
        int film_id PK
        string name
        string description
        date release_date
        int duration
        int mpa_id FK
    }

    MPA_RATING {
        int mpa_id PK
        string name UK
        string description
    }

    GENRES {
        int genre_id PK
        string name UK
    }

    FILM_GENRE {
        int film_id PK,FK
        int genre_id PK,FK
    }

    FILM_LIKES {
        int film_id PK,FK
        int user_id PK,FK
    }

    REVIEWS {
        int review_id PK
        string content
        boolean is_positive
        int user_id FK
        int film_id FK
        int useful
    }

    REVIEW_RATINGS {
        int review_id PK,FK
        int user_id PK,FK
        boolean is_like
    }
```

### Обозначения связей на диаграмме

- **"initiates"** — пользователь инициирует дружбу (отправляет запрос)
- **"receives"** — пользователь получает запрос на дружбу
- **"likes"** — пользователь ставит лайки фильмам
- **"writes"** — пользователь пишет отзывы на фильмы
- **"rates"** — пользователь оценивает отзывы (лайк/дизлайк)
- **"has_likes"** — фильм имеет лайки от пользователей
- **"has_reviews"** — фильм имеет отзывы
- **"has_ratings"** — отзыв имеет оценки полезности
- **"has_mpa"** — фильм имеет рейтинг MPA
- **"has_genres"** — фильм имеет жанры
- **"assigned_to"** — жанр назначен фильмам

## Описание таблиц

### USERS

Хранит информацию о пользователях приложения.

### FRIENDSHIP

Связь между пользователями (дружба). Поддерживает два статуса:

- `UNCONFIRMED` — неподтверждённая (один пользователь отправил запрос)
- `CONFIRMED` — подтверждённая (второй пользователь принял запрос)

### FILMS

Основная информация о фильмах.

### MPA_RATING

Справочник рейтингов MPA (возрастные ограничения):

- G — нет ограничений
- PG — рекомендуется смотреть с родителями
- PG-13 — не желателен до 13 лет
- R — до 17 лет только с взрослым
- NC-17 — запрещён до 18 лет

### GENRES

Справочник жанров фильмов (Комедия, Драма, Мультфильм, Триллер, Документальный, Боевик).

### FILM_GENRE

Связь многие-ко-многим между фильмами и жанрами (у фильма может быть несколько жанров).

### FILM_LIKES

Лайки пользователей к фильмам.

### REVIEWS

Отзывы пользователей на фильмы. Каждый отзыв содержит:
- `review_id` — уникальный идентификатор
- `content` — текст отзыва
- `is_positive` — тип отзыва (положительный/отрицательный)
- `user_id` — автор отзыва
- `film_id` — фильм, к которому относится отзыв
- `useful` — рейтинг полезности (изменяется при добавлении лайков/дизлайков)

### REVIEW_RATINGS

Оценки полезности отзывов. Пользователи могут ставить лайки или дизлайки отзывам:
- `is_like = TRUE` — лайк (увеличивает рейтинг на 1)
- `is_like = FALSE` — дизлайк (уменьшает рейтинг на 1)

## SQL Запросы

Примеры SQL запросов для работы с базой данных доступны в файле [SQL.md](SQL.md)

## API

Подробная документация REST API доступна в файле [API.md](API.md)

## API

Подробная документация REST API доступна в файле [API.md](API.md).