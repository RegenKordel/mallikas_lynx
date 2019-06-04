package eu.openreq.mallikas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc 
public class MallikasApplication {

// For enabling H2 console for testing purpose, uncomment the following
//   @Configuration
//   public class WebConfiguration {
//       @Bean
//       ServletRegistrationBean h2servletRegistration(){
//           ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet());
//           registrationBean.addUrlMappings("/h2/*");
//           return registrationBean;
//       }
//   }

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(MallikasApplication.class, args);

	}

}
