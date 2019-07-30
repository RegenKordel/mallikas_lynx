package eu.openreq.mallikas.test.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_status;
import eu.openreq.mallikas.models.json.Dependency_type;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.repositories.DependencyRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DependencyRepositoryTest {
	
	@Autowired
	DependencyRepository dependencyRepository;
	
	private Dependency dep1;
	
	private Project project;
	
	private Requirement req1;
	
	private Requirement req2;
	
	private Requirement req3;
	
    @Before
    public void setUp() throws IOException {
    	
    	req1 = new Requirement();
    	req1.setCreated_at(1);
    	req1.setId("RE1");
    	req1.setProjectId("PRO");
    	req1.setStatus(Requirement_status.SUBMITTED);
    	
    	req2 = new Requirement();
    	req3 = new Requirement();
    	 
    	project = new Project();
    	project.setCreated_at(1);
    	project.setId("PRO");
    	
    	Set <String> reqIds = new HashSet<String>();
    	reqIds.add("RE1");
    	
    	project.setSpecifiedRequirements(reqIds);
    	
		 dep1 = new Dependency();
		 dep1.setCreated_at(1);
		 dep1.setDependency_score(0.5);
		 dep1.setDependency_type(Dependency_type.REQUIRES);
		 dep1.setStatus(Dependency_status.ACCEPTED);
		 dep1.setFromid("RE1");
		 dep1.setToid("RE2");
		 dep1.setId("RE1_RE2_REQUIRES");
    }
	 @Test
	  public void repositorySavesOneDependency() {	 
		 dependencyRepository.save(dep1);
		 Assert.assertNotNull(dependencyRepository.findOne("RE1_RE2_REQUIRES"));
		 Assert.assertNull(dependencyRepository.findOne("RE3_RE2_REQUIRES"));
	  }
	
	 @Test
	  public void findByIdWorks() {	 
		 dependencyRepository.save(dep1);
		 Assert.assertNotNull(dependencyRepository.findById("RE1_RE2_REQUIRES"));
		 Assert.assertNull(dependencyRepository.findById("RE3_RE2_REQUIRES"));
	  }

}
