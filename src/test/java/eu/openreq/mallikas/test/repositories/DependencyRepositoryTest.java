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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_status;
import eu.openreq.mallikas.models.json.Dependency_type;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DependencyRepositoryTest {
	
	@Autowired
	private DependencyRepository dependencyRepository;

	@Autowired
	private ProjectRepository projectRepository;
	
	private Dependency dep1;
	
	private Dependency dep2;
	
	private Dependency dep3;
	
	private Project project;
	
    @Before
    public void setUp() throws IOException {    	
    	initializeDependencies();
    	initializeProjectAndRepository();		 
    }
    
	 private void initializeDependencies() {
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
		 dep2.setFromid("RE2");
		 dep2.setToid("RE3");
		 dep2.setId("RE2_RE3_DUPLICATES");
		 
		 dep3 = new Dependency();
		 dep3.setCreated_at(1);
		 dep3.setDependency_score(0.5);
		 dep3.setDependency_type(Dependency_type.DUPLICATES);
		 dep3.setStatus(Dependency_status.REJECTED);
		 dep3.setFromid("RE1");
		 dep3.setToid("RE3");
		 dep3.setId("RE1_RE3_DUPLICATES");
	 }
	 
	 private void initializeProjectAndRepository() {
	    	project = new Project();
	    	project.setCreated_at(1);
	    	project.setId("PRO");
	    	
	    	Set <String> reqIds = new HashSet<String>();
	    	reqIds.add("RE1");
	    	reqIds.add("RE2");
	    	reqIds.add("RE3");
	    	
	    	project.setSpecifiedRequirements(reqIds);
	    	
	    	projectRepository.save(project);
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
	 
	 @Test
	 public void findByRequirementIdIncludeProposedWorks() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 List<String> ids = new ArrayList<String>();
		 ids.add("RE1");
		 ids.add("RE2");
		 ids.add("RE3");
		 List<Dependency> dependencies = dependencyRepository.findByRequirementIdIncludeProposed(ids);
		 assertEquals(2, dependencies.size());
	 }
	 
	 @Test
	 public void findByRequirementIdExcludeProposedWorks() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 List<String> ids = new ArrayList<String>();
		 ids.add("RE1");
		 ids.add("RE2");
		 ids.add("RE3");
		 List<Dependency> dependencies = dependencyRepository.findByRequirementIdExcludeProposed(ids);
		 assertEquals(1, dependencies.size());
	 }
	 
	 @Test
	 public void deleteAllNotRejectedWorks() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);		
		 dependencyRepository.save(dep3);
		 assertEquals(3, dependencyRepository.count());
		 
		 dependencyRepository.deleteAllNotRejected();
		 
		 List<Dependency> dependencies = dependencyRepository.findAll();
		 assertEquals(1, dependencies.size());
	 } 
	 
	 @Test
	 public void findByRequirementIdWithParamsWorksWhenParamsNull() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 dependencyRepository.save(dep3);
		 
		 List<String> ids = new ArrayList<String>();
		 ids.add("RE1");
		 ids.add("RE2");
		 ids.add("RE3");

		 assertEquals(3, dependencyRepository.count());
		 
		 List<Dependency> dependencies = dependencyRepository.findByRequirementIdWithParams(ids, null, null, null, null, null);
		 assertEquals(2, dependencies.size());
	 } 
	 
	 @Test
	 public void findByRequirementIdWithParamsWorksWhenIncludeRejectedTrue() {
		 dependencyRepository.save(dep1);
		 dependencyRepository.save(dep2);
		 dependencyRepository.save(dep3);
		 
		 List<String> ids = new ArrayList<String>();
		 ids.add("RE1");
		 ids.add("RE2");
		 ids.add("RE3");
		
		 assertEquals(3, dependencyRepository.count());
		 
		 List<Dependency> dependencies = dependencyRepository.findByRequirementIdWithParams(ids, null, null, null, true, null);
		 assertEquals(3, dependencies.size());
	 }
	 
}
