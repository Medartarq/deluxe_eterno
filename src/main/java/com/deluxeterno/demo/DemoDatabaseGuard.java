package com.deluxeterno.demo;

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
@Profile("demo")
public class DemoDatabaseGuard {
    @Bean
    static BeanPostProcessor verifyDemoDataSourceBeforeHibernate() {
        return new BeanPostProcessor() {
            @Override public Object postProcessAfterInitialization(Object bean, String name) {
                if (bean instanceof DataSource source) DemoDatabaseIdentity.verify(source);
                return bean;
            }
        };
    }
}
