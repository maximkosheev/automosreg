package net.monsterdev.automosreg.services;

import java.util.List;
import net.monsterdev.automosreg.domain.User;

public interface UserService {
    /**
     * Возвращает список всех пользователей, зарегистрированных в системе
     * @return
     */
    List<User> findAll();

    /**
     * Возвращает кол-во пользователей, зарегистрированных в системе
     * Этот метод нужен для быстрого получениия кол-ва пользователей (без получения всего списка)
     * просто используется SQL-запрос SELECT COUNT(*) FROM...
     */
    Long getCount();

    /**
     * Регистрация нового пользователя в системе
     */
    User register(User newUser);

    /**
     * Устанавливает текущего пользователя
     */
    void setCurrentUser(User user);

    /**
     * Возвращает текущего пользователя
     */
    User getCurrentUser();

    /**
     * Выполняет обновление структур данных (список закупок, предложений, информации по ним), связанных с  текущим пользователем
     * @return возвращает "обновленного" пользователя
     */
    User update();
}
