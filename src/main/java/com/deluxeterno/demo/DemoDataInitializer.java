package com.deluxeterno.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {
    private final DemoDatasetService dataset;
    private final boolean reset, exit;
    private final String media;
    private final ConfigurableApplicationContext context;
    public DemoDataInitializer(DemoDatasetService dataset, @Value("${app.demo.reset-on-start:false}") boolean reset,
            @Value("${app.demo.exit-after-initialize:false}") boolean exit,
            @Value("${app.media.directory}") String media,ConfigurableApplicationContext context) {
        this.dataset=dataset;this.reset=reset;this.exit=exit;this.media=media;this.context=context;
    }
    @Override public void run(String... args) {
        if (!Path.of(media).toAbsolutePath().normalize().getFileName().toString().equals("uploads-demo"))
            throw new IllegalStateException("El entorno demo debe usar su carpeta uploads-demo independiente.");
        if (reset && !exit) throw new IllegalStateException("El restablecimiento solo se permite como operación local de una sola ejecución.");
        if (reset) dataset.reset(); else dataset.initialize();
        if (exit) System.exit(SpringApplication.exit(context));
    }
}
