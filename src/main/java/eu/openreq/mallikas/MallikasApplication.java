package eu.openreq.mallikas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//MR ADD:
import org.h2.server.web.WebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//MR ADD END

@SpringBootApplication
public class MallikasApplication {


//   @Configuration
//   public class WebConfiguration {
//       @Bean
//       ServletRegistrationBean h2servletRegistration(){
//           ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet());
//           registrationBean.addUrlMappings("/h2/*");
//           return registrationBean;
//       }
//   }
//    //MR ADD END
    
	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(MallikasApplication.class, args);

	}

}
