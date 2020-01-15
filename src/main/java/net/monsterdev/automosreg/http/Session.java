package net.monsterdev.automosreg.http;

import java.util.Properties;

/**
 * Класс Session хранит параметры, актуальные для всех запросов в рамках сессии.
 * Один из таких параметров - ключ авторизации
 * @author madmax
 */
public final class Session {
    private static Session instance = null;

    private Properties properties;

    private Session() {
        properties = new Properties();
        instance = this;
    }

    public static Session getInstance() {
        if (instance == null)
            instance = new Session();
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
