package exchange.xyvox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XyvoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyvoxApplication.class, args);
    }

}
