package eu.openreq.mallikas.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.controllers.MallikasController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {
	
    @Autowired
    private MallikasController controller;

    @Test
    public void contexLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

}
