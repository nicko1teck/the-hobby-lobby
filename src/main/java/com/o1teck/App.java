package com.o1teck;

import java.util.HashMap;
import java.util.Map;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import com.cloudinary.Cloudinary;

//  https://github.com/princesoni1989/Spring-Boot-Cloudinary/blob/master/src/main/java/com/cloudinary/upload/UploadApplication.java

//@EnableJpaRepositories
@EnableAsync
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@ComponentScan({"com.o1teck.*"})
//@EntityScan({"com.o1teck.*"})
public class App extends SpringBootServletInitializer implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);


	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(App.class);
    }
	
	
	
	@Bean
	public static Cloudinary getCloudinary(
			@Value("${cloudinary.cloud_name}") String cloudName,
			@Value("${cloudinary.api_key}") String apiKey,
			@Value("${cloudinary.api_secret}") String apiSecret) {
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", cloudName);
		config.put("api_key", apiKey);
		config.put("api_secret", apiSecret);
		return new Cloudinary(config);
	}
	
	
	

	//https://stackoverflow.com/questions/33459740/error-no-url-for-servletcontext-resource-when-running-spring-integrated-test
	
	@Bean
    public TilesConfigurer tilesConfigurer() {
		org.springframework.web.servlet.view.tiles3.TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("classpath:tiles.xml");
        //tilesConfigurer.setDefinitions(
          //new String[] { "tiles.xml" });
          //new String[] { "/WEB-INF/tiles.xml" });
        
        
        	
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        TilesViewResolver viewResolver = new TilesViewResolver();
        registry.viewResolver(viewResolver);
    }
    
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
        .addResourceLocations("/css/");
        registry.addResourceHandler("/img/**")
        .addResourceLocations("/img/");
        registry.addResourceHandler("/js/**")
        .addResourceLocations("/js/");
    }
	

	
	// BCrypt password encoding
	@Bean
	PasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	PolicyFactory getUserHtmlPolicy() {
		return new HtmlPolicyBuilder().allowCommonBlockElements().allowCommonInlineFormattingElements().toFactory();
	}

	

	// Custom error handling config
	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> errorHandler() {

		return new WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>() {

			@Override
			public void customize(ConfigurableServletWebServerFactory factory) {
				factory.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/403"));
				//factory.setContextPath("");
			}
			
		};
	}

	
}