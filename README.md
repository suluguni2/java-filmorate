# java-filmorate

## Схема базы данных
![Схема БД](FilmorateApplication.svg)
### Примеры запросов

Топ-10 популярных фильмов:

SELECT f.id, f.name, COUNT(fl.users\_id) AS likes

FROM film f

LEFT JOIN films\_likes fl ON f.id = fl.film\_id

GROUP BY f.id, f.name

ORDER BY likes DESC

LIMIT 10;



Общие друзья двух пользователей:

SELECT u.\*

FROM users u

JOIN users\_friends f1 ON u.id = f1.friend\_id

JOIN users\_friends f2 ON u.id = f2.friend\_id

WHERE f1.users\_id = 1 AND f2.users\_id = 2;

