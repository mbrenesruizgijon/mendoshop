package mendoshop.mendoshop.Servicio;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class Webconfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String home = System.getProperty("user.home");
        Path imgDir = Paths.get(home, "mendoshop_uploads", "img");
        registry.addResourceHandler("/uploads/img/**")
                .addResourceLocations("file:" + imgDir.toAbsolutePath().toString() + "/");
    }
}

