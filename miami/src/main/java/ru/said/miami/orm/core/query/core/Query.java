package ru.said.miami.orm.core.query.core;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Общий интерфейс для всех типов запросов
 * @param <T> тип результата выполнения запроса
 */
public interface Query {

    /**
     * Выполняет сгенерированный запрос и возвращает результат выполнения
     * @return результат выполнения запроса
     * @throws SQLException если произошла ошибка выполнения запроса
     * @param connection
     */
    <T> T execute(Connection connection) throws SQLException;
}
