-- =====================================================
-- Стартовые справочники. MERGE = идемпотентная вставка:
-- можно запускать при каждом старте приложения без ошибок
-- и дубликатов (KEY (id) — по какому полю проверять уникальность).
-- =====================================================

-- Рейтинги MPA
MERGE INTO MPA (id, name) KEY (id) VALUES (1, 'G');
MERGE INTO MPA (id, name) KEY (id) VALUES (2, 'PG');
MERGE INTO MPA (id, name) KEY (id) VALUES (3, 'PG-13');
MERGE INTO MPA (id, name) KEY (id) VALUES (4, 'R');
MERGE INTO MPA (id, name) KEY (id) VALUES (5, 'NC-17');

-- Жанры
MERGE INTO GENRES (id, name) KEY (id) VALUES (1, 'Комедия');
MERGE INTO GENRES (id, name) KEY (id) VALUES (2, 'Драма');
MERGE INTO GENRES (id, name) KEY (id) VALUES (3, 'Мультфильм');
MERGE INTO GENRES (id, name) KEY (id) VALUES (4, 'Триллер');
MERGE INTO GENRES (id, name) KEY (id) VALUES (5, 'Документальный');
MERGE INTO GENRES (id, name) KEY (id) VALUES (6, 'Боевик');