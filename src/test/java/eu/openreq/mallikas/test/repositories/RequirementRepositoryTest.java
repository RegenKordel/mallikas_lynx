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
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RequirementRepositoryTest {

	@Autowired
	private RequirementRepository reqRepository;

	@Autowired
	private ProjectRepository projectRepository;

	private Project project;

	private Requirement req1;

	private Requirement req2;

	private Requirement req3;

	@Before
	public void setUp() throws IOException {
		initializeRequirements();
		initializeProjectAndRepository();
	}

	private void initializeProjectAndRepository() {
		project = new Project();
		project.setCreated_at(1);
		project.setId("PRO");

		Set<String> reqIds = new HashSet<String>();
		reqIds.add("RE1");
		reqIds.add("RE2");
		reqIds.add("RE3");

		project.setSpecifiedRequirements(reqIds);

		projectRepository.save(project);
	}

	private void initializeRequirements() {
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
	}

	@Test
	public void repositorySavesOneRequirement() {
		reqRepository.save(req1);
		Assert.assertNotNull(reqRepository.findOne("RE1"));
		Assert.assertNull(reqRepository.findOne("RE2"));
	}
	
	@Test
	public void findByIdWorks() {
		reqRepository.save(req1);
		Assert.assertNotNull(reqRepository.findById("RE1"));
		Assert.assertNull(reqRepository.findById("RE2"));
	}
}
