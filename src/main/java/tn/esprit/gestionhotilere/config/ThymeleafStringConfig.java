package tn.esprit.gestionhotilere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafStringConfig {

    @Bean
    @Primary
    public SpringTemplateEngine stringTemplateEngine() {
        StringTemplateResolver str = new StringTemplateResolver();
        str.setTemplateMode(TemplateMode.HTML);
        str.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(str);
        engine.setEnableSpringELCompiler(true); // SpEL
        return engine;
    }
}
