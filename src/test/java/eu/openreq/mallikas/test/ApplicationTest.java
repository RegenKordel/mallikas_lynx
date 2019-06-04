package eu.openreq.mallikas.test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.MallikasApplication;
import eu.openreq.mallikas.controllers.MallikasController;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=MallikasApplication.class)
@SpringBootTest
@DataJpaTest
public class ApplicationTest {
	
    @Autowired
    private MallikasController controller;

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
    
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
 

}
