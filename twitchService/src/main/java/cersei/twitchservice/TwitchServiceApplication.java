package cersei.twitchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TwitchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwitchServiceApplication.class, args);
    }

}
