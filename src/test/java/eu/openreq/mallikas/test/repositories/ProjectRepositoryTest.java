package eu.openreq.mallikas.test.repositories;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.repositories.ProjectRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProjectRepositoryTest {
	
	@Autowired
	private ProjectRepository projectRepository;
	
	private Project project1;
	
	private Project project2;

    @Before
    public void setUp() throws IOException {    	
    	initializeProjects();		 
    }
    
	private void initializeProjects() {
		project1 = new Project();
		project1.setCreated_at(1);
		project1.setId("PRO1");

		Set<String> reqIds = new HashSet<String>();
		reqIds.add("RE1");
		reqIds.add("RE2");
		reqIds.add("RE3");

		project1.setSpecifiedRequirements(reqIds);
		
		project2 = new Project();
		project2.setCreated_at(2);
		project2.setId("PRO2");

		reqIds = new HashSet<String>();
		reqIds.add("RE4");
		reqIds.add("RE5");
		reqIds.add("RE6");

		project2.setSpecifiedRequirements(reqIds);
	}
	
	 @Test
	  public void repositorySavesOneProject() {
		 projectRepository.save(project1);
		 Assert.assertNotNull(projectRepository.findOne("PRO1"));
		 Assert.assertNull(projectRepository.findOne("PRO2"));
	 }
	 
	 @Test
	  public void findByIdWorks() {
		 projectRepository.save(project1);
		 projectRepository.save(project2);
		 Assert.assertNotNull(projectRepository.findById("PRO1"));
		 Assert.assertNull(projectRepository.findById("PRO3"));
	 }
}
