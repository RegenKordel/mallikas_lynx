package eu.openreq.mallikas.test.controllers;

import eu.closedreq.bridge.models.json.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openreq.mallikas.MallikasApplication;
import eu.openreq.mallikas.controllers.MallikasController;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.PersonRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import eu.openreq.mallikas.services.FilteringService;
import eu.openreq.mallikas.services.UpdateService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
	
	@MockBean
	private PersonRepository personRepository;
	
	@Autowired
	FilteringService filteringService;
	
	@Autowired
	UpdateService updateService;
	
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
	
	Map<String, Collection<String>> testReqIds;
	String testReqIdsJson;
	
	List<String> ids;
	
	@Before
	public void setup() throws Exception {
		mapper = new ObjectMapper();
		
		Requirement testReq1 = new Requirement();
		testReq1.setId("req1");
		
		RequirementPart part = new RequirementPart();
		part.setId("part1");
		part.setName("Resolution");
		part.setText("Done");
		testReq1.setRequirementParts(new HashSet<RequirementPart>(Arrays.asList(part)));
		
		Comment comment = new Comment();
		comment.setId("NoComment");
		testReq1.setComments(new HashSet<Comment>(Arrays.asList(comment)));
		
		Requirement testReq2 = new Requirement();
		testReq2.setId("req2");
		testReq2.setCreated_at(1000000);

		Requirement testReq3 = new Requirement();
		testReq3.setId("req3");
		testReq3.setRequirementParts(new HashSet<RequirementPart>(Arrays.asList(part)));
		
		testReqs = Arrays.asList(testReq1, testReq2, testReq3);
		testReqsJson = mapper.writeValueAsString(testReqs);
		
		Dependency testDep1 = new Dependency();
		testDep1.setId("req1_req2");
		testDep1.setFromid("req1");
		testDep1.setToid("req2");
		testDep1.setStatus(Dependency_status.ACCEPTED);
		testDep1.setDescription(Arrays.asList("Description here"));
		Dependency testDep2 = new Dependency();
		testDep2.setId("dep2");
		
		testDeps = Arrays.asList(testDep1, testDep2);
		testDepsJson = mapper.writeValueAsString(testDeps);
		
		Project testProj = new Project();
		testProj.setId("pro1");
		testProj.setSpecifiedRequirements(new HashSet<String>(Arrays.asList("req1", "req2, req3")));
		testProJson = mapper.writeValueAsString(testProj);
		testProjs = Arrays.asList(testProj);
		testProjsJson = mapper.writeValueAsString(testProjs);
		
		testReqIds = new HashMap<>();
		testReqIds.put("pro1", Arrays.asList("req1", "req2", "req3"));
		testReqIdsJson = mapper.writeValueAsString(testReqIds);
		
		ids = Arrays.asList("req1", "req2", "req3");		
		
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		Mockito.when(reqRepository.findById(Mockito.anyString())).thenReturn(testReq1);
		Mockito.when(reqRepository.findByProjectId(Mockito.anyString())).thenReturn(testReqs);
		Mockito.when(reqRepository.findAll()).thenReturn(testReqs);
		Mockito.when(reqRepository.findByRequirementPartText(Matchers.anyString())).thenReturn(Arrays.asList(testReq3));
		Mockito.when(reqRepository.findByIdIn(Matchers.any())).thenReturn(testReqs);
		Mockito.when(reqRepository.findByParams(Matchers.any(), Matchers.any(), Matchers.any(), 
				Matchers.any(), Matchers.any())).thenReturn(testReqs);
		
		Mockito.when(depRepository.findAll()).thenReturn(testDeps);
		Mockito.when(depRepository.findByRequirementIdIncludeProposed(Matchers.any())).thenReturn(testDeps);
		Mockito.when(depRepository.findByRequirementIdWithParams(Matchers.any(), Matchers.anyDouble(), 
				Matchers.any(), Matchers.any(), Matchers.any(), 
				org.mockito.Matchers.isA(Pageable.class))).thenReturn(testDeps);
		Mockito.when(depRepository.findById(Matchers.any())).thenReturn(testDep1);
		
		Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(testProj);
		Mockito.when(projectRepository.findAll()).thenReturn(testProjs);
		
		testDep1.setDescription(Arrays.asList("Another description"));
	}
	
	@Test
	public void getListOfProjectsTest() throws Exception {
		
		mockMvc.perform(get("/listOfProjects"))
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
	
	@Test
	public void updateRequirementsTest() throws Exception {		
		mockMvc.perform(post("/updateRequirements")
				.param("projectId", "pro1")
				.content(testReqsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());	
	}
	
	@Test
	public void updateDependenciesTest() throws Exception { 
		mockMvc.perform(post("/updateDependencies")
				.content(testDepsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void updateDependenciesUserInputTest() throws Exception { 
		mockMvc.perform(post("/updateDependencies?userInput=true")
				.content(testDepsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void updateDependenciesIsProposedTest() throws Exception { 
		mockMvc.perform(post("/updateDependencies?isProposed=true")
				.content(testDepsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}	
	
	@Test
	public void updateProjectSpecifiedRequirements() throws Exception {
		mockMvc.perform(post("/updateProjectSpecifiedRequirements")
				.param("projectId", "pro1")
				.content(testReqIdsJson)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	public void getAllRequirementsTest() throws Exception {
		mockMvc.perform(get("/allRequirements"))
				.andExpect(status().isOk());
	}	
	
	@Test
	public void getAllDependenciesTest() throws Exception {
		mockMvc.perform(get("/allDependencies"))
				.andExpect(status().isOk());
	}	
	
	 @Test
	 public void selectRequirementsTest() throws Exception {
		mockMvc.perform(post("/selectedReqs")
				.content(mapper.writeValueAsString(ids))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req1"));	
	}
	 
	@Test
	public void requirementsByParamsTest() throws Exception {
		
		RequestParams params = new RequestParams();
		params.setProposedOnly(false);
		params.setIncludeProposed(true);
		params.setIncludeRejected(false);
		params.setMaxDependencies(20);
		params.setProjectId("pro1");
		params.setStatus("fakeStatus");
		params.setType("noType");
		
	    String requestJson = mapper.writeValueAsString(params);
	    
		mockMvc.perform(post("/requirementsByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req1"));	

	}	
	
	@Test
	public void requirementsByParamsWithResolutionTest() throws Exception {
		
		RequestParams params = new RequestParams();
		params.setProjectId("pro1");
		params.setResolution("Done");
		
	    String requestJson = mapper.writeValueAsString(params);
	    
		mockMvc.perform(post("/requirementsByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req3"));	

	}	
	
	@Test
	public void requirementsByParamsWithIds() throws Exception {
		
		RequestParams params = new RequestParams();
		params.setRequirementIds(ids);
		params.setCreated_at(new Date(1000001));
		params.setModified_at(new Date(1000002));
		params.setStatus("PENDING");
		params.setType("PROSE");
		
	    String requestJson = mapper.writeValueAsString(params);
	    
		mockMvc.perform(post("/requirementsByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req1"));	

	}	
	
	@Test
	public void dependenciesByParamsTest() throws Exception {
		RequestParams params = new RequestParams();
		params.setProposedOnly(false);
		params.setIncludeProposed(true);
		params.setIncludeRejected(false);
		params.setMaxDependencies(20);
		params.setRequirementIds(ids);
		
	    String requestJson = mapper.writeValueAsString(params);
	    
		mockMvc.perform(post("/dependenciesByParams")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req1"));	
	}
	
	@Test
	public void projectRequirementsTest() throws Exception {
		mockMvc.perform(get("/projectRequirements")
				.param("projectId", "pro1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.requirements[0].id").value("req1"));
	}
  
	@Test
	public void deleteEverythingTest() throws Exception {
		mockMvc.perform(delete("/deleteEverything?keepRejected=true"))
				.andExpect(status().isOk());
	}
}
