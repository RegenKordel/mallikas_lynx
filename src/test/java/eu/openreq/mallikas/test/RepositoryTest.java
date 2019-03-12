package eu.openreq.mallikas.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.controllers.MallikasController;
import eu.openreq.mallikas.models.json.RequestParams;
import eu.openreq.mallikas.repositories.RequirementRepository;

//!! These tests require for the QTWB project to exist in the database

@RunWith(SpringRunner.class)
@SpringBootTest
public class RepositoryTest {
	
    @Autowired
    private MallikasController controller;


    @Before
    public void setUp() {
    	
    }
    
    @Test
    public void weirdDataButOk() throws Exception {
    	Hibernate.initialize(controller);
    	RequestParams params = new RequestParams();
    	DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
    	Date date = (Date)df.parse("2016/02/20");
    	params.setCreated_at(date);
    	params.setProjectId("QTWB");
    	params.setType("BUG");
    	List<String> reqIds = new ArrayList<String>();
    	reqIds.add("QTBUG-56789");
    	params.setRequirementIds(reqIds);
    	ResponseEntity<?> response = controller.sendRequirementsByParamsToMilla(params);
    	System.out.println(response);
        assertTrue(true);
        
        
    }
    
    
}