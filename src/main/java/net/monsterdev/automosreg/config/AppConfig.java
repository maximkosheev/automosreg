package net.monsterdev.automosreg.config;

import net.monsterdev.automosreg.services.UserService;
import net.monsterdev.automosreg.services.impl.UserServiceImpl;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Configuration
@ComponentScan({
        "net.monsterdev.automosreg.config",
        "net.monsterdev.automosreg.model",
        "net.monsterdev.automosreg.services",
        "net.monsterdev.automosreg.repository",
        "net.monsterdev.automosreg.utils",
        "net.monsterdev.automosreg.ui"})
@EnableTransactionManagement
public class AppConfig {
    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return Persistence.createEntityManagerFactory("net.monsterdev.automosreg.hibernate");
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory());
        return transactionManager;
    }

    @Bean
    public CookieStore cookieStore() {
        return new BasicCookieStore();
    }
}
