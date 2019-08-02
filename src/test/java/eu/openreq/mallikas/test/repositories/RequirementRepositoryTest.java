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

import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import eu.openreq.mallikas.models.json.RequirementPart;

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
		req1.setModified_at(2);
		req1.setId("RE1");
		req1.setProjectId("PRO");
		req1.setStatus(Requirement_status.SUBMITTED);
		req1.setRequirement_type(Requirement_type.BUG);

		RequirementPart reqPart1 = new RequirementPart();
		reqPart1.setId(req1.getId() + "_RESOLUTION");
		reqPart1.setName("Resolution");
		reqPart1.setCreated_at(1);
		reqPart1.setText("TestResolution1");

		Set<RequirementPart> reqParts = new HashSet<RequirementPart>();
		reqParts.add(reqPart1);
		req1.setRequirementParts(reqParts);

		req2 = new Requirement();
		req2.setCreated_at(1);
		req2.setModified_at(1);
		req2.setId("RE2");
		req2.setProjectId("PRO");
		req2.setStatus(Requirement_status.OPEN);
		req2.setRequirement_type(Requirement_type.ISSUE);

		RequirementPart reqPart2 = new RequirementPart();
		reqPart2.setId(req2.getId() + "_RESOLUTION");
		reqPart2.setName("Resolution");
		reqPart2.setCreated_at(1);
		reqPart2.setText("TestResolution2");

		reqParts = new HashSet<RequirementPart>();
		reqParts.add(reqPart2);
		req2.setRequirementParts(reqParts);

		req3 = new Requirement();
		req3.setCreated_at(0);
		req3.setModified_at(3);
		req3.setId("RE3");
		req3.setProjectId("PRO");
		req3.setStatus(Requirement_status.OPEN);
		req3.setRequirement_type(Requirement_type.REQUIREMENT);
	}

	private void saveAllRequirements() {
		reqRepository.save(req1);
		reqRepository.save(req2);
		reqRepository.save(req3);
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

	@Test
	public void findByIdInWorks() {
		saveAllRequirements();

		List<String> reqIds = new ArrayList<String>();
		reqIds.add("RE1");
		reqIds.add("RE2");
		reqIds.add("RE4");

		List<Requirement> requirements = reqRepository.findByIdIn(reqIds);
		assertEquals(2, requirements.size());
	}

	@Test
	public void findByProjectIdWorks() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByProjectId("PRO");
		assertEquals(3, requirements.size());

		requirements = reqRepository.findByProjectId("PRE");
		assertEquals(0, requirements.size());
	}

	// @Test
	// public void findByRequirementPartWorks() {
	// saveAllRequirements();
	//
	// List<Requirement> requirements =
	// reqRepository.findByRequirementPart("TestResolution1");
	// assertEquals(1, requirements.size());
	//
	// requirements = reqRepository.findByRequirementPart("Wrong");
	// assertEquals(0, requirements.size());
	// }

	@Test
	public void findByParamsWorksWhenReqIdsGiven() {
		saveAllRequirements();

		List<String> reqIds = new ArrayList<String>();
		reqIds.add("RE1");
		reqIds.add("RE2");
		reqIds.add("RE4");

		List<Requirement> requirements = reqRepository.findByParams(reqIds, null, null, null, null);
		assertEquals(2, requirements.size());
	}

	@Test
	public void findByParamsWorksWhenCreatedAtGiven() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByParams(null, 1L, null, null, null);
		assertEquals(2, requirements.size());
	}

	@Test
	public void findByParamsWorksWhenModifiedAtGiven() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByParams(null, null, 3L, null, null);
		assertEquals(1, requirements.size());
	}

	@Test
	public void findByParamsWorksWhenRequirementTypeGiven() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByParams(null, null, null, Requirement_type.REQUIREMENT,
				null);
		assertEquals(1, requirements.size());
	}

	@Test
	public void findByParamsWorksWhenRequirementStatusGiven() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByParams(null, null, null, null, Requirement_status.OPEN);
		assertEquals(2, requirements.size());
	}

	@Test
	public void findByParamsWorksWhenAllParamsGiven() {
		saveAllRequirements();

		List<String> reqIds = new ArrayList<String>();
		reqIds.add("RE1");
		reqIds.add("RE2");
		reqIds.add("RE3");

		List<Requirement> requirements = reqRepository.findByParams(reqIds, 0L, 2L, Requirement_type.REQUIREMENT,
				Requirement_status.OPEN);
		assertEquals(1, requirements.size());
		assertEquals("RE3", requirements.get(0).getId());
	}

	@Test
	public void findByParamsWorksWhenNoParamsGiven() {
		saveAllRequirements();

		List<Requirement> requirements = reqRepository.findByParams(null, null, null, null, null);
		assertEquals(requirements.size(), reqRepository.count());
	}
}
