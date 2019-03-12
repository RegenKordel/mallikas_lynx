package eu.openreq.mallikas.test.repositories;

import static org.junit.Assert.assertTrue;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.MallikasApplication;
import eu.openreq.mallikas.controllers.MallikasController;

//!! These tests require for the QTWB project to exist in the database

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=MallikasApplication.class)
@SpringBootTest
public class RepositoryTest {
	
    @Autowired
    private MallikasController controller;


    @Before
    public void setUp() {
    	
    }
    
    @Test
    public void initializeTest() throws Exception {
    	Hibernate.initialize(controller);
    	
    	assertTrue(true);
    }
    
//    @Test
//    public void weirdDataButOk() throws Exception {
//    	Hibernate.initialize(controller);
//    	RequestParams params = new RequestParams();
//    	DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
//    	Date date = (Date)df.parse("2016/02/20");
//    	params.setCreated_at(date);
//    	params.setProjectId("QTWB");
//    	params.setType("BUG");
//    	List<String> reqIds = new ArrayList<String>();
//    	reqIds.add("QTBUG-56789");
//    	params.setRequirementIds(reqIds);
//    	ResponseEntity<?> response = controller.sendRequirementsByParamsToMilla(params);
//    	System.out.println(response);
//        assertTrue(true);
//        
//        
//    }
    
    
}