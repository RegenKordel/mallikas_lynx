package eu.openreq.mallikas.test.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.openreq.mallikas.MallikasApplication;
import eu.openreq.mallikas.controllers.MallikasController;
import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.RequestParams;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=MallikasApplication.class)
@SpringBootTest
public class MallikasControllerTest {
	
	@MockBean
	private RequirementRepository reqRepository;
	
	@MockBean
	private DependencyRepository depRepository;

	@MockBean
	private ProjectRepository projectRepository;
	
	@Autowired
	MallikasController controller;

	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	List<Project> testProjs;
	String testProjsJson;
	String testProJson;
	
	List<Requirement> testReqs;
	String testReqsJson;
	
	List<Dependency> testDeps;
	String testDepsJson;
	
	
	@Before
	public void setup() throws Exception {
		mapper = new ObjectMapper();
		
		Requirement testReq1 = new Requirement();
		testReq1.setId("asd");
		Requirement testReq2 = new Requirement();
		testReq1.setId("fgh");
		
		testReqs = Arrays.asList(testReq1, testReq2);
		testReqsJson = mapper.writeValueAsString(testReqs);
		
		Dependency testDep1 = new Dependency();
		testDep1.setId("asd");
		Dependency testDep2 = new Dependency();
		testDep2.setId("fgh");
		
		testDeps = Arrays.asList(testDep1, testDep2);
		testDepsJson = mapper.writeValueAsString(testDeps);
		
		Project testProj = new Project();
		testProj.setId("hjk");
		testProj.setSpecifiedRequirements(new HashSet<String>(Arrays.asList("asd", "fgh")));
		testProJson = mapper.writeValueAsString(testProj);
		testProjs = Arrays.asList(testProj);
		testProjsJson = mapper.writeValueAsString(testProjs);
		
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		Mockito.when(reqRepository.findById(Mockito.anyString())).thenReturn(null);
		
		Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(null);
		
		Mockito.when(projectRepository.findAll()).thenReturn(testProjs);
	}
	
	@Test
	public void getListOfProjectsTest() throws Exception {
		
		mockMvc.perform(get("/listOfProjects"))
				.andExpect(status().isOk());	
	}
	
	@Test
	public void requirementsByParamsTest() throws Exception {
		
		RequestParams params = new RequestParams();
		params.setProjectId("test");
		
	    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
	    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
	    String requestJson = ow.writeValueAsString(params);
	    
		mockMvc.perform(post("/requirementsByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk());

	}	
	
	@Test
	public void importRequirementsTest() throws Exception {		

		mockMvc.perform(post("/importRequirements")
				.content(testReqsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());	
	}
	
	@Test
	public void importDependenciesTest() throws Exception { 
		mockMvc.perform(post("/importDependencies")
				.content(testDepsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void importProjectsTest() throws Exception { 
		mockMvc.perform(post("/importProject")
				.content(testProJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
  
}
