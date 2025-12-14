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
    USERS ||--o{ FEED : "has_events"
    FILMS ||--o{ FILM_LIKES : "has_likes"
    FILMS ||--o{ REVIEWS : "has_reviews"
    FILMS }o--|| MPA_RATING : "has_mpa"
    FILMS ||--o{ FILM_GENRE : "has_genres"
    FILMS ||--o{ FILM_DIRECTORS : "has_directors"
    GENRES ||--o{ FILM_GENRE : "assigned_to"
    DIRECTORS ||--o{ FILM_DIRECTORS : "directed"
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

    DIRECTORS {
        int director_id PK
        string name
    }

    FILM_GENRE {
        int film_id PK,FK
        int genre_id PK,FK
    }

    FILM_DIRECTORS {
        int film_id PK,FK
        int director_id PK,FK
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
    }

    REVIEW_RATINGS {
        int review_id PK,FK
        int user_id PK,FK
        boolean is_like
    }

    FEED {
        int event_id PK
        int user_id FK
        int entity_id
        string event_type
        string operation
        timestamp created_at
    }
```

### Обозначения связей на диаграмме

- **"initiates"** — пользователь инициирует дружбу (отправляет запрос)
- **"receives"** — пользователь получает запрос на дружбу
- **"likes"** — пользователь ставит лайки фильмам
- **"writes"** — пользователь пишет отзывы на фильмы
- **"rates"** — пользователь оценивает отзывы (лайк/дизлайк)
- **"has_events"** — пользователь имеет события в ленте
- **"has_likes"** — фильм имеет лайки от пользователей
- **"has_reviews"** — фильм имеет отзывы
- **"has_ratings"** — отзыв имеет оценки полезности
- **"has_mpa"** — фильм имеет рейтинг MPA
- **"has_genres"** — фильм имеет жанры
- **"has_directors"** — фильм имеет режиссёров
- **"assigned_to"** — жанр назначен фильмам
- **"directed"** — режиссёр снял фильмы

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

### DIRECTORS

Справочник режиссёров фильмов.

### FILM_DIRECTORS

Связь многие-ко-многим между фильмами и режиссёрами (у фильма может быть несколько режиссёров, режиссёр может снять
несколько фильмов).

### FILM_LIKES

Лайки пользователей к фильмам.

### REVIEWS

Отзывы пользователей на фильмы. Каждый отзыв содержит:

- `review_id` — уникальный идентификатор
- `content` — текст отзыва
- `is_positive` — тип отзыва (положительный/отрицательный)
- `user_id` — автор отзыва
- `film_id` — фильм, к которому относится отзыв

Рейтинг полезности (`useful`) вычисляется динамически на основе данных из таблицы `REVIEW_RATINGS`.

### REVIEW_RATINGS

Оценки полезности отзывов. Пользователи могут ставить лайки или дизлайки отзывам:

- `is_like = TRUE` — лайк (увеличивает рейтинг на 1)
- `is_like = FALSE` — дизлайк (уменьшает рейтинг на 1)

### FEED

Лента событий пользователя. Хранит историю действий пользователей:

- `event_id` — уникальный идентификатор события
- `user_id` — пользователь, совершивший действие
- `entity_id` — идентификатор сущности (фильм, друг, отзыв)
- `event_type` — тип события (LIKE, FRIEND, REVIEW)
- `operation` — операция (ADD, REMOVE, UPDATE)
- `created_at` — время события

## SQL Запросы

Примеры SQL запросов для работы с базой данных доступны в файле [SQL.md](SQL.md)

## API

Подробная документация REST API доступна в файле [API.md](API.md)