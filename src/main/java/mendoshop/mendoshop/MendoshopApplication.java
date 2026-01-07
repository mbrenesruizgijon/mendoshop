package mendoshop.mendoshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MendoshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(MendoshopApplication.class, args);
	}

}
