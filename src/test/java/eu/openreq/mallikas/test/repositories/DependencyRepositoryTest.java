package eu.openreq.mallikas.test.repositories;

import static org.junit.Assert.assertEquals;

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
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DependencyRepositoryTest {
	
	@Autowired
	DependencyRepository dependencyRepository;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	RequirementRepository reqRepository;
	
	private Dependency dep1;
	
	private Dependency dep2;
	
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
    	req2.setCreated_at(1);
    	req2.setId("RE2");
    	req2.setProjectId("PRO");
    	req2.setStatus(Requirement_status.OPEN);
    	
    	req3 = new Requirement();
    	req3.setCreated_at(1);
    	req3.setId("RE3");
    	req3.setProjectId("PRO");
    	req3.setStatus(Requirement_status.OPEN);
    	
    	reqRepository.save(req1);
    	reqRepository.save(req2);
    	reqRepository.save(req3);
    	 
    	project = new Project();
    	project.setCreated_at(1);
    	project.setId("PRO");
    	
    	Set <String> reqIds = new HashSet<String>();
    	reqIds.add("RE1");
    	reqIds.add("RE2");
    	reqIds.add("RE3");
    	
    	project.setSpecifiedRequirements(reqIds);
    	
    	projectRepository.save(project);
    	
		 dep1 = new Dependency();
		 dep1.setCreated_at(1);
		 dep1.setDependency_score(0.5);
		 dep1.setDependency_type(Dependency_type.REQUIRES);
		 dep1.setStatus(Dependency_status.ACCEPTED);
		 dep1.setFromid("RE1");
		 dep1.setToid("RE2");
		 dep1.setId("RE1_RE2_REQUIRES");
		 
		 dep2 = new Dependency();
		 dep2.setCreated_at(1);
		 dep2.setDependency_score(0.5);
		 dep2.setDependency_type(Dependency_type.DUPLICATES);
		 dep2.setStatus(Dependency_status.PROPOSED);
		 dep2.setFromid("RE1");
		 dep2.setToid("RE3");
		 dep2.setId("RE1_RE2_DUPLICATES");
		 
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
	 
	 @Test
	 public void findByProjectIdIncludeProposedWorks() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 List<Dependency> dependencies = dependencyRepository.findByProjectIdIncludeProposed("PRO");
		 assertEquals(2, dependencies.size());
	 }
	 
	 @Test
	 public void findByProjectIdExcludeProposedWorks() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 List<Dependency> dependencies = dependencyRepository.findByProjectIdExcludeProposed("PRO");
		 assertEquals(1, dependencies.size());
	 }

}
