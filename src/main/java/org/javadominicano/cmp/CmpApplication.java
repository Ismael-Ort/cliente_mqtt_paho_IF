package org.javadominicano.cmp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication(scanBasePackages = {"org.javadominicano.cmp", "com.weathernet"})
@EnableScheduling
public class CmpApplication implements CommandLineRunner {

    private final Suscriptor suscriptor;

    public CmpApplication(Suscriptor suscriptor) {
        this.suscriptor = suscriptor;
    }

    public static void main(String[] args) {
        SpringApplication.run(CmpApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Solo arranca el suscriptor
        new Thread(() -> suscriptor.start()).start();
    }
}
