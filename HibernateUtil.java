package com.example;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory factory =
        new Configuration()
            .configure("hibernate.cfg.xml")
            .buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return factory;
    }
}
