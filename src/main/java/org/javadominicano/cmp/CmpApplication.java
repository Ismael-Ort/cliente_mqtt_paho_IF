package org.javadominicano.cmp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CmpApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CmpApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Solo arranca el suscriptor
        new Thread(() -> new Suscriptor().start()).start();
    }
}
