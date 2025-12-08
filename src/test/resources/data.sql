-- Заполнение справочника MPA рейтингов
MERGE INTO mpa_rating (mpa_id, name, description) 
VALUES (1, 'G', 'У фильма нет возрастных ограничений');

MERGE INTO mpa_rating (mpa_id, name, description) 
VALUES (2, 'PG', 'Детям рекомендуется смотреть фильм с родителями');

MERGE INTO mpa_rating (mpa_id, name, description) 
VALUES (3, 'PG-13', 'Детям до 13 лет просмотр не желателен');

MERGE INTO mpa_rating (mpa_id, name, description) 
VALUES (4, 'R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого');

MERGE INTO mpa_rating (mpa_id, name, description) 
VALUES (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

-- Заполнение справочника жанров
MERGE INTO genres (genre_id, name) VALUES (1, 'Комедия');
MERGE INTO genres (genre_id, name) VALUES (2, 'Драма');
MERGE INTO genres (genre_id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (genre_id, name) VALUES (4, 'Триллер');
MERGE INTO genres (genre_id, name) VALUES (5, 'Документальный');
MERGE INTO genres (genre_id, name) VALUES (6, 'Боевик');

-- Тестовые пользователи
INSERT INTO users (email, login, name, birthday) 
VALUES ('user1@mail.ru', 'user1', 'User One', '1990-01-01');

INSERT INTO users (email, login, name, birthday) 
VALUES ('user2@mail.ru', 'user2', 'User Two', '1995-05-15');

INSERT INTO users (email, login, name, birthday) 
VALUES ('user3@mail.ru', 'user3', 'User Three', '2000-12-31');

-- Тестовые фильмы
INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Test Film 1', 'Description for test film 1', '2000-01-01', 120, 1);

INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Test Film 2', 'Description for test film 2', '2010-06-15', 90, 2);

INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Test Film 3', 'Description for test film 3', '2020-12-25', 150, 3);

-- Жанры для фильмов
INSERT INTO film_genre (film_id, genre_id) VALUES (1, 1);
INSERT INTO film_genre (film_id, genre_id) VALUES (1, 2);
INSERT INTO film_genre (film_id, genre_id) VALUES (2, 3);
INSERT INTO film_genre (film_id, genre_id) VALUES (3, 4);
INSERT INTO film_genre (film_id, genre_id) VALUES (3, 6);

-- Лайки фильмов
INSERT INTO film_likes (film_id, user_id) VALUES (1, 1);
INSERT INTO film_likes (film_id, user_id) VALUES (1, 2);
INSERT INTO film_likes (film_id, user_id) VALUES (2, 1);

-- Дружба между пользователями
INSERT INTO friendship (user_id, friend_id, status) VALUES (1, 2, 'CONFIRMED');
INSERT INTO friendship (user_id, friend_id, status) VALUES (2, 1, 'CONFIRMED');
INSERT INTO friendship (user_id, friend_id, status) VALUES (1, 3, 'UNCONFIRMED');

-- Тестовые отзывы
INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
VALUES ('Great movie! Highly recommend.', TRUE, 1, 1, 0);

INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
VALUES ('Not my cup of tea. Too slow.', FALSE, 2, 1, 0);

INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
VALUES ('Amazing cinematography!', TRUE, 3, 2, 0);
