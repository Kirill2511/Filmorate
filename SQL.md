# SQL Запросы

## Фильмы

### Получение всех фильмов

```sql
SELECT f.*, mr.name as mpa_name
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
ORDER BY f.film_id;
```

### Получение фильма по ID с жанрами

```sql
SELECT f.*, mr.name as mpa_name, g.name as genre_name
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         LEFT JOIN film_genre fg ON f.film_id = fg.film_id
         LEFT JOIN genres g ON fg.genre_id = g.genre_id
WHERE f.film_id = ?;
```

### Топ N наиболее популярных фильмов (по количеству лайков)

```sql
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       mr.mpa_id,
       mr.name           as mpa_name,
       COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC
LIMIT ?;
```

### Топ популярных фильмов с фильтрацией по году и жанру

```sql
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       mr.mpa_id,
       mr.name           as mpa_name,
       COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         LEFT JOIN film_genre fg ON f.film_id = fg.film_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
  AND (? IS NULL OR fg.genre_id = ?)
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC
LIMIT ?;
```

### Добавление лайка к фильму

```sql
INSERT INTO film_likes (film_id, user_id)
VALUES (?, ?);
```

### Удаление лайка у фильма

```sql
DELETE
FROM film_likes
WHERE film_id = ?
  AND user_id = ?;
```

### Общие фильмы двух пользователей

```sql
SELECT f.*, mr.name as mpa_name, COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         JOIN film_likes fl1 ON f.film_id = fl1.film_id
         JOIN film_likes fl2 ON f.film_id = fl2.film_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE fl1.user_id = ?
  AND fl2.user_id = ?
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC;
```

### Поиск фильмов по названию

```sql
SELECT f.*, mr.name as mpa_name, COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE LOWER(f.name) LIKE LOWER(?)
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC;
```

### Поиск фильмов по режиссёру

```sql
SELECT f.*, mr.name as mpa_name, COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         JOIN film_directors fd ON f.film_id = fd.film_id
         JOIN directors d ON fd.director_id = d.director_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE LOWER(d.name) LIKE LOWER(?)
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC;
```

## Пользователи

### Получение всех пользователей

```sql
SELECT *
FROM users
ORDER BY user_id;
```

### Получение пользователя по ID

```sql
SELECT *
FROM users
WHERE user_id = ?;
```

### Создание пользователя

```sql
INSERT INTO users (email, login, name, birthday)
VALUES (?, ?, ?, ?);
```

### Обновление пользователя

```sql
UPDATE users
SET email    = ?,
    login    = ?,
    name     = ?,
    birthday = ?
WHERE user_id = ?;
```

## Дружба

### Получение списка друзей пользователя

```sql
SELECT u.*
FROM users u
         JOIN friendship f ON u.user_id = f.friend_id
WHERE f.user_id = ?;
```

### Получение списка общих друзей

```sql
SELECT u.*
FROM users u
         JOIN friendship f1 ON u.user_id = f1.friend_id
         JOIN friendship f2 ON u.user_id = f2.friend_id
WHERE f1.user_id = ?
  AND f2.user_id = ?;
```

### Добавление в друзья

```sql
INSERT INTO friendship (user_id, friend_id, status)
VALUES (?, ?, 'UNCONFIRMED');
```

### Подтверждение дружбы

```sql
UPDATE friendship
SET status = 'CONFIRMED'
WHERE user_id = ?
  AND friend_id = ?;
```

### Удаление из друзей

```sql
DELETE
FROM friendship
WHERE user_id = ?
  AND friend_id = ?;
```

## Жанры

### Получение всех жанров

```sql
SELECT *
FROM genres
ORDER BY genre_id;
```

### Получение жанра по ID

```sql
SELECT *
FROM genres
WHERE genre_id = ?;
```

### Получение жанров фильма

```sql
SELECT g.*
FROM genres g
         JOIN film_genre fg ON g.genre_id = fg.genre_id
WHERE fg.film_id = ?
ORDER BY g.genre_id;
```

## Рейтинги MPA

### Получение всех рейтингов MPA

```sql
SELECT *
FROM mpa_rating
ORDER BY mpa_id;
```

### Получение рейтинга MPA по ID

```sql
SELECT *
FROM mpa_rating
WHERE mpa_id = ?;
```

## Режиссёры

### Получение всех режиссёров

```sql
SELECT *
FROM directors
ORDER BY director_id;
```

### Получение режиссёра по ID

```sql
SELECT *
FROM directors
WHERE director_id = ?;
```

### Создание режиссёра

```sql
INSERT INTO directors (name)
VALUES (?);
```

### Обновление режиссёра

```sql
UPDATE directors
SET name = ?
WHERE director_id = ?;
```

### Удаление режиссёра

```sql
DELETE
FROM directors
WHERE director_id = ?;
```

### Получение режиссёров фильма

```sql
SELECT d.*
FROM directors d
         JOIN film_directors fd ON d.director_id = fd.director_id
WHERE fd.film_id = ?
ORDER BY d.director_id;
```

### Фильмы режиссёра (сортировка по лайкам)

```sql
SELECT f.*, mr.name as mpa_name, COUNT(fl.user_id) as likes_count
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         JOIN film_directors fd ON f.film_id = fd.film_id
         LEFT JOIN film_likes fl ON f.film_id = fl.film_id
WHERE fd.director_id = ?
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, mr.mpa_id, mr.name
ORDER BY likes_count DESC;
```

### Фильмы режиссёра (сортировка по году)

```sql
SELECT f.*, mr.name as mpa_name
FROM films f
         JOIN mpa_rating mr ON f.mpa_id = mr.mpa_id
         JOIN film_directors fd ON f.film_id = fd.film_id
WHERE fd.director_id = ?
ORDER BY f.release_date;
```

## Лента событий

### Получение ленты пользователя

```sql
SELECT event_id, user_id, entity_id, event_type, operation, created_at
FROM feed
WHERE user_id = ?
ORDER BY created_at;
```

### Добавление события в ленту

```sql
INSERT INTO feed (user_id, entity_id, event_type, operation)
VALUES (?, ?, ?, ?);
```

## Отзывы

### Получение отзыва по ID

```sql
SELECT review_id, content, is_positive, user_id, film_id, useful
FROM reviews
WHERE review_id = ?;
```

### Получение всех отзывов (отсортированных по полезности)

```sql
SELECT review_id, content, is_positive, user_id, film_id, useful
FROM reviews
ORDER BY useful DESC;
```

### Получение отзывов для фильма

```sql
SELECT review_id, content, is_positive, user_id, film_id, useful
FROM reviews
WHERE film_id = ?
ORDER BY useful DESC
LIMIT ?;
```

### Создание отзыва

```sql
INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
VALUES (?, ?, ?, ?, 0);
```

### Обновление отзыва

```sql
UPDATE reviews
SET content     = ?,
    is_positive = ?
WHERE review_id = ?;
```

### Удаление отзыва

```sql
DELETE
FROM reviews
WHERE review_id = ?;
```

### Добавление лайка к отзыву

```sql
-- Удаляем предыдущую оценку пользователя, если была
DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?;

-- Добавляем лайк
INSERT INTO review_ratings (review_id, user_id, is_like) VALUES (?, ?, TRUE);

-- Обновляем рейтинг полезности
UPDATE reviews SET useful = (
    SELECT COALESCE(SUM(CASE WHEN is_like = TRUE THEN 1 ELSE -1 END), 0)
    FROM review_ratings WHERE review_id = ?
) WHERE review_id = ?;
```

### Добавление дизлайка к отзыву

```sql
-- Удаляем предыдущую оценку пользователя, если была
DELETE FROM review_ratings WHERE review_id = ? AND user_id = ?;

-- Добавляем дизлайк
INSERT INTO review_ratings (review_id, user_id, is_like) VALUES (?, ?, FALSE);

-- Обновляем рейтинг полезности
UPDATE reviews SET useful = (
    SELECT COALESCE(SUM(CASE WHEN is_like = TRUE THEN 1 ELSE -1 END), 0)
    FROM review_ratings WHERE review_id = ?
) WHERE review_id = ?;
```

### Удаление лайка с отзыва

```sql
DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND is_like = TRUE;

-- Обновляем рейтинг полезности
UPDATE reviews SET useful = (
    SELECT COALESCE(SUM(CASE WHEN is_like = TRUE THEN 1 ELSE -1 END), 0)
    FROM review_ratings WHERE review_id = ?
) WHERE review_id = ?;
```

### Удаление дизлайка с отзыва

```sql
DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND is_like = FALSE;

-- Обновляем рейтинг полезности
UPDATE reviews SET useful = (
    SELECT COALESCE(SUM(CASE WHEN is_like = TRUE THEN 1 ELSE -1 END), 0)
    FROM review_ratings WHERE review_id = ?
) WHERE review_id = ?;
```
