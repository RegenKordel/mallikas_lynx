package eu.openreq.mallikas.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.openreq.mallikas.models.json.Comment;
import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_status;
import eu.openreq.mallikas.models.json.Dependency_type;
import eu.openreq.mallikas.models.json.Person;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.RequestParams;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.RequirementPart;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.PersonRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@SpringBootApplication
@Controller
@RequestMapping("/")
public class MallikasController {

	@Autowired
	RequirementRepository requirementRepository;

	@Autowired
	DependencyRepository dependencyRepository;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	PersonRepository personRepository;

	/**
	 * Check which projects are already saved in the database
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get a list (map) of projects currently saved", notes = "Get a map with ids of all saved projects and their requirement counts.")
	@GetMapping(value = "listOfProjects")
	public ResponseEntity<?> getListOfProjects() {
		List<Project> projects = projectRepository.findAll();
		Map<String, Integer> projectIds = new HashMap<String, Integer>();
		if (projects.isEmpty() || projects == null) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		for (Project project : projects) {
			projectIds.put(project.getId(), project.getSpecifiedRequirements().size());
		}
		return new ResponseEntity<>(projectIds, HttpStatus.OK);
	}

	/**
	 * Import a Collection of Requirements from Milla and save to the
	 * RequirementRepository if the Requirement is not already in the database.
	 * 
	 * @param requirements
	 *            Collection of Requirements received from Milla
	 * @return ResponseEntity
	 */
	@ApiOperation(value = "Import a list of requirements", notes = "Import a list of new OpenReq JSON Requirements to the database. If a requirement exists, it is not updated or changed.")
	@PostMapping(value = "importRequirements")
	@ApiIgnore
	public ResponseEntity<?> importRequirements(@RequestBody Collection<Requirement> requirements) {
		System.out.println("Received requirements from Milla");
		List<Requirement> savedReqs = new ArrayList<>();
		for (Requirement req : requirements) {
			if (requirementRepository.findById(req.getId()) == null) {
				savedReqs.add(req);
			} else {
				System.out.println("Found a duplicate " + req.getId());
			}
		}
		requirementRepository.save(savedReqs);
		System.out.println("Requirements saved " + requirementRepository.count());
		savedReqs.clear();
		return new ResponseEntity<>("Requirements saved", HttpStatus.OK);
	}

	/**
	 * Import a Collection of Dependencies from Milla and save to the
	 * DependencyRepository
	 * 
	 * @param dependencies
	 *            Collection of Dependencies received from Milla
	 * @return ResponseEntity
	 */
	@ApiOperation(value = "Import a list of dependencies", notes = "Import a list of new OpenReq JSON Dependencies to the database. If a dependency exists, it is not updated or changed.")
	@PostMapping(value = "importDependencies")
	@ApiIgnore
	public ResponseEntity<?> importDependencies(@RequestBody Collection<Dependency> dependencies) {
		System.out.println("Received dependencies from Milla");
		List<Dependency> savedDependencies = new ArrayList<>();
		for (Dependency dependency : dependencies) {
			if (dependencyRepository.findById(dependency.getId()) == null) {
				savedDependencies.add(dependency);
			} else {
				System.out.println("Found a duplicate " + dependency.getId());
			}
		}
		dependencyRepository.save(savedDependencies);
		System.out.println("Dependencies saved " + dependencyRepository.count());
		savedDependencies.clear();
		return new ResponseEntity<>("Dependencies saved", HttpStatus.OK);
	}

	/**
	 * Import a Project from Milla and save to the ProjectRepository
	 * 
	 * @param project
	 *            Project received from Milla
	 * @return ResponseEntity
	 */
	@ApiOperation(value = "Import a project", notes = "Import an OpenReq JSON project to the database. If a project exists, it is updated.")
	@PostMapping(value = "importProject")
	public ResponseEntity<?> importProject(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project.getId());
		if (projectRepository.findOne(project.getId()) != null) {
			System.out.println("Found a duplicate " + project.getId());
		} 
		projectRepository.save(project);	
		
		System.out.println("Project saved " + projectRepository.count());
		return new ResponseEntity<>("Project saved", HttpStatus.OK);
	}

	/**
	 * Update selected Dependencies
	 * 
	 * @param dependencies
	 *            Collection of Dependencies received from Milla
	 * @return String "Dependencies updated" if the update operation is successful
	 */
	@ApiOperation(value = "Post dependencies to be updated", notes = "Update existing and save new dependencies in the database.")
	@PostMapping(value = "updateDependencies")
	@Transactional
	public ResponseEntity<?> updateDependencies(@RequestBody Collection<Dependency> dependencies, 
			@RequestParam(required = false) boolean userInput, @RequestParam(required = false) boolean isProposed) {
		try {
			if (userInput) {
				updateDependenciesWithUserInput(dependencies);
			} else if (isProposed) {
				saveProposedDependencies(dependencies);
			} else {
				dependencyRepository.save(dependencies);
			}

			System.out.println("Dependencies saved " + dependencyRepository.count());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Update selected Requirements
	 * 
	 * @param requirements
	 *            Collection of Requirements received from Milla
	 * @return String "Requirements updated" if the update operation is successful
	 */
	@ApiOperation(value = "Post requirements to be updated", notes = "Update existing and save new requirements to the database.")
	@PostMapping(value = "updateRequirements")
	@Transactional
	public ResponseEntity<?> updateRequirements(@RequestBody Collection<Requirement> requirements, 
			@RequestParam String projectId) {
		List<Requirement> savedRequirements = new ArrayList<>();
		List<Comment> savedComments = new ArrayList<>();
		//List<RequirementPart> savedReqParts = new ArrayList<>();
		List<Person> savedPersons = new ArrayList<>();
		
		try {
			for (Requirement requirement : requirements) {
				
				Set<RequirementPart> reqParts = requirement.getRequirementParts();
				for (RequirementPart part : reqParts) {
					part.setRequirement(requirement);
					//savedReqParts.add(part);
				}
				requirement.setRequirementParts(reqParts);
				
				Set<Comment> comments = requirement.getComments();
				for (Comment comment : comments) {
					comment.setRequirement(requirement);
					Person person = comment.getCommentDoneBy();
					savedPersons.add(person);
					//savedComments.add(comment);
				}
				requirement.setComments(comments);
				
				if (requirementRepository.findById(requirement.getId()) == null) {
					savedRequirements.add(requirement);
				} else if (requirement.getModified_at() > requirementRepository.findById(requirement.getId()).getModified_at()) {
					savedRequirements.add(requirement);
				}
				requirement.setProjectId(projectId);
			}
			personRepository.save(savedPersons);
			requirementRepository.save(savedRequirements);
			System.out.println("Requirements saved " + requirementRepository.count());
			savedRequirements.clear();
			savedComments.clear();
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Updates the speficiedRequirements list of a selected project with the ids of the latest updated requirements in that project.
	 * @param reqIds
	 * @return
	 */
	@ApiOperation(value = "Post project specified requirements to be updated",
			notes = "Update the specified requirements of a project in the database.")
	@PostMapping(value = "updateProjectSpecifiedRequirements")
	public ResponseEntity<String> updateProjectSpecifiedRequirements(@RequestBody Map<String, Collection<String>> reqIds, @RequestParam String projectId) {
		try {
			Project project = projectRepository.findById(projectId);
			Set<String> projectReqs = project.getSpecifiedRequirements();
			for (String reqId : reqIds.get(projectId)) {
				if (!projectReqs.contains(reqId)) {
					projectReqs.add(reqId);
				}
			}
			project.setSpecifiedRequirements(projectReqs);
			projectRepository.save(project);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
	}

	// Should work (but the returned String might be too large to show in Swagger
	//
	/**
	 * Sends all Requirements in the database as a String to Milla
	 *
	 * @return all Requirements and their Dependencies as a String
	 */
	@ApiOperation(value = "Get a list of all requirements", notes = "Returns a list with every single requirement saved in the database!")
	@GetMapping(value = "allRequirements")
	@ApiIgnore
	@Transactional(readOnly = true)
	public ResponseEntity<String> getAllRequirements() {
		List<Requirement> allReqs = requirementRepository.findAll();
		List<Dependency> dependencies = dependencyRepository.findAll();
		if (!allReqs.isEmpty()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(allReqs);
				String dependencyString = mapper.writeValueAsString(dependencies);
				String all = "{ \"requirements\":" + reqString + ", \"dependencies\":" + dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// Should work (but the returned String might be too large to show in Swagger
	//
	/**
	 * Get all dependencies from the database as a JSON String
	 *
	 * @return
	 */
	@ApiOperation(value = "Get a list of all dependencies", notes = "Returns a list with every single dependency saved in the database!")
	@GetMapping(value = "allDependencies")
	@ApiIgnore
	@Transactional(readOnly = true)
	public ResponseEntity<String> getAllDependencies() {
		List<Dependency> dependencies = dependencyRepository.findAll();
		if (!dependencies.isEmpty()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String dependencyString = mapper.writeValueAsString(dependencies);
				dependencies.clear();
				String all = "{\"dependencies\":" + dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Receives a Collection of Requirement ids (String) from Milla and sends back
	 * to Milla a List of selected Requirements.
	 * 
	 * @return selected Requirements and Dependencies associated with them as a
	 *         String, if the List is not empty, else returns a new ResponseEntity
	 *         Not Found
	 */
	@ApiOperation(value = "Get requirements by ids posted", notes = "Fetches a list of requirements based on the ids provided.")
	@PostMapping(value = "selectedReqs")
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSelectedRequirements(@RequestBody List<String> ids) {
		List<Requirement> selectedReqs = requirementRepository.findByIdIn(ids);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<Dependency> dependencies = new ArrayList<Dependency>();
			List<List<String>> splitReqIds = splitRequirementIds(ids);
			for (List<String> splitIds : splitReqIds) {
			dependencies.addAll(dependencyRepository.findByIdIncludeProposed(splitIds));
			}
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * Fetches dependencies by given parameters along with their dependent requirements
	 * @param params
	 * @return
	 */
	@ApiOperation(value = "Get dependencies by params posted", notes = "Fetches dependencies and their dependent requirements by parameters provided.")
	@PostMapping(value = "dependenciesByParams") 
	@Transactional(readOnly = true)
	public ResponseEntity<String> getDependenciesByParams(@RequestBody RequestParams params) {
		List<String> reqIds = params.getRequirementIds();
		
		List<Requirement> selectedReqs = new ArrayList<>();
		
		List<List<String>> splitReqIds = splitRequirementIds(reqIds);

		for (List<String> splitIds : splitReqIds) {
			selectedReqs.addAll(requirementRepository.findByIdIn(splitIds));
		}
		
		if (!selectedReqs.isEmpty() && selectedReqs!=null) {
			Pageable pageLimit = new PageRequest(0, Integer.MAX_VALUE);
			
			if (params.getMaxDependencies()!=null && params.getMaxDependencies()>0) {
				pageLimit = new PageRequest(0, params.getMaxDependencies());
			}
			
			List<Dependency> dependencies = new ArrayList<Dependency>();
			
			for (List<String> splitIds : splitReqIds) {
				dependencies.addAll(dependencyRepository.findByIdWithParams(splitIds, params.getScoreThreshold(),
						params.getIncludeProposed(), params.getProposedOnly(), params.getIncludeRejected(), pageLimit));
			}
			
			List<String> dependentReqIds = new ArrayList<String>();
			
			for (Dependency dep : dependencies) {
				if (!reqIds.contains(dep.getFromid())) {
					dependentReqIds.add(dep.getFromid());
				}
				if (!reqIds.contains(dep.getToid())) {
					dependentReqIds.add(dep.getToid());
				}
			}
			
			selectedReqs.addAll(requirementRepository.findByIdIn(dependentReqIds));
			
			

			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies), HttpStatus.OK);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Sends requirements to Milla based on the parameters given (multiple parameters can be used simultaneously) along with their dependencies
	 * @param params
	 * @return
	 */
	@ApiOperation(value = "Get requirements by params posted", notes = "Fetches requirements and their dependencies by parameters provided.")
	@PostMapping(value = "requirementsByParams")
	@Transactional(readOnly = true)
	public ResponseEntity<String> getRequirementsByParams(@RequestBody RequestParams params) {
		
		List<Project> projects = null;
		List<String> reqIds = new ArrayList<String>();
		reqIds.addAll(params.getRequirementIds());
		
		if (params.getProjectId() != null && projectRepository.findById(params.getProjectId())!=null) {
			Project project = projectRepository.findById(params.getProjectId());
			projects = new ArrayList<Project>();
			projects.add(project);
			Set<String> projectReqIds = project.getSpecifiedRequirements();
			if (reqIds.isEmpty()) {
				reqIds.addAll(projectReqIds);
			} else {
				reqIds.retainAll(projectReqIds);
			}
		}
		
		Long created = null;
		Long modified = null;
		Requirement_type type = null;
		Requirement_status status = null;
		
		if (params.getCreated_at()!=null) {
			created = params.getCreated_at().getTime();
		}
		if (params.getModified_at()!=null) {
			modified = params.getModified_at().getTime();
		}
		if (params.getType()!=null) {
			type = Requirement_type.valueOf(params.getType());
		}
		if (params.getStatus()!=null) {
			status = Requirement_status.valueOf(params.getStatus());
		}
		
		List<Requirement> selectedReqs = new ArrayList<>();
		
		List<List<String>> splitReqIds = splitRequirementIds(reqIds);
		
		for (List<String> splitIds : splitReqIds) {
			selectedReqs.addAll(requirementRepository.findByParams(splitIds, created, modified, type, status));
		}
		
		if (params.getResolution()!=null) {
			List<Requirement> resolutionReqs = requirementRepository.findByRequirementPart(params.getResolution());
			if (!selectedReqs.isEmpty()) {
				selectedReqs.retainAll(resolutionReqs);
			} else {			
				selectedReqs = resolutionReqs;
			}
		}
		
		if (!selectedReqs.isEmpty() && selectedReqs!=null) {
			List<String> ids = new ArrayList<String>();
			for (Requirement req : selectedReqs) {
				ids.add(req.getId());
			}
			List<Dependency> dependencies = new ArrayList<Dependency>();
			
			Pageable pageLimit = new PageRequest(0, Integer.MAX_VALUE);
			
			if (params.getMaxDependencies()!=null && params.getMaxDependencies()>0) {
				pageLimit = new PageRequest(0, params.getMaxDependencies());
			}
			
			splitReqIds = splitRequirementIds(ids);
			
			for (List<String> splitIds : splitReqIds) {
				dependencies.addAll(dependencyRepository.findByIdWithParams(splitIds, params.getScoreThreshold(),
						params.getIncludeProposed(), params.getProposedOnly(), params.getIncludeRejected(), pageLimit));
			}
			
			try {
				if (projects==null) {
					return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
							HttpStatus.OK); 
				}
				return new ResponseEntity<String>(createUPCJsonString(projects, selectedReqs, dependencies),
						HttpStatus.OK);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * Receives a projectId from Milla and sends back all requirements and their
	 * dependencies in that project (projects list version)
	 * 
	 * @param projectId
	 * @return
	 */
	@ApiOperation(value = "Get all requirements and dependencies of a project",
			notes = "Get all requirements and dependencies of a project saved in the database, excluding rejected dependencies."
					+ " Has an option whether to include proposed dependencies.")
	@GetMapping(value = "projectRequirements")
	@Transactional(readOnly = true)
	public ResponseEntity<String> getRequirementsInProject(@RequestParam String projectId, 
			@RequestParam(required = false) boolean includeProposed) {
		Project project = projectRepository.findById(projectId);

		if (project != null) {
			List<Project> projects = new ArrayList<>();
			projects.add(project);
			
			Set<String> requirementIds = project.getSpecifiedRequirements();
			List<Requirement> requirements = new ArrayList<Requirement>();
			List<Dependency> dependencies = new ArrayList<Dependency>();	
			
			if (includeProposed) {
				requirements = requirementRepository.findByProjectId(projectId);
				dependencies.addAll(dependencyRepository.findByProjectIdIncludeProposed(projectId));			
			} else {
				requirements = requirementRepository.findByProjectId(projectId);
				dependencies = dependencyRepository.findByProjectIdExcludeProposed(projectId);	
			}
			if (!requirementIds.isEmpty()) {
				try {
					return new ResponseEntity<String>(createUPCJsonString(projects, requirements, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					requirements.clear();
					dependencies.clear();
					e.printStackTrace();
				}
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Create a String containing (possibly) Project, Requirements and Dependencies
	 * in JSON format
	 * 
	 * @param project
	 * @param requirement
	 * @param requirements
	 * @param dependencies
	 * @return
	 * @throws JsonProcessingException
	 */
	private String createJsonString(Project project, Requirement requirement, List<Requirement> requirements,
			List<Dependency> dependencies) throws JsonProcessingException {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();	
		String dependencyString = gson.toJson(dependencies);
		String reqsString = gson.toJson(requirements);
		String jsonString = "{";
		if (requirement != null) {
			String reqString = gson.toJson(requirement);
			jsonString += "\"requirement\":" + reqString + ", ";
		} else if (project != null) {
			String projectString = gson.toJson(project);
			jsonString += "\"project\":" + projectString + ", ";
		} 
		
		jsonString += "\"requirements\":" + reqsString + ", \"dependencies\":" + dependencyString + "}";
		
		return jsonString;
	}
	
	/**
	 * Empties the database except for dependencies with the status "rejected"
	 * @return
	 */
	@ApiIgnore
	@DeleteMapping(value = "deleteAllButRejectedDependencies")
	@Transactional
	public ResponseEntity<String> deleteAllButRejectedDependencies() {
		projectRepository.deleteAll();
		requirementRepository.deleteAll();
		dependencyRepository.deleteAllNotRejected();

		return new ResponseEntity<>("Delete successful", HttpStatus.OK);
	}

	/**
	 * Create a String containing Projects, Requirements and Dependencies in JSON
	 * format for UPC
	 * 
	 * @param projects
	 * @param requirements
	 * @param dependencies
	 * @return
	 * @throws JsonProcessingException
	 */
	private String createUPCJsonString(List<Project> projects, List<Requirement> requirements,
			List<Dependency> dependencies) throws JsonProcessingException {
		Gson gson =  new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();	
		String dependencyString = gson.toJson(dependencies);
		String reqsString = gson.toJson(requirements);
		String projectsString = gson.toJson(projects);
		String jsonString = "{ \"projects\":" + projectsString + ", \"requirements\":" + reqsString
				+ ", \"dependencies\":" + dependencyString + "}";
		return jsonString;
	}

	/**
	 * Updates dependency status and type as determined by user input
	 * 
	 * @param dependencies
	 */
	private void updateDependenciesWithUserInput(Collection<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			String depId = dep.getFromid() + "_" + dep.getToid() + "_SIMILAR";
			Dependency originalDependency = dependencyRepository.findById(depId);
			if (originalDependency!=null && ((dep.getStatus()==Dependency_status.ACCEPTED && 
					dep.getDependency_type()!=Dependency_type.SIMILAR) || 
					(dep.getStatus()!=Dependency_status.ACCEPTED))) {

				originalDependency.setStatus(dep.getStatus());
				originalDependency.setDependency_type(dep.getDependency_type());
				dependencyRepository.save(originalDependency);
			}
		}
	}
	
	/**
	 * Save proposed dependencies received from similarity detection services and such
	 * 
	 * @param dependencies
	 */
	private void saveProposedDependencies(Collection<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			String depId = dep.getId();
			if (depId==null) {
				depId = dep.getFromid() + "_" + dep.getToid() + "_SIMILAR"; 
			}
			Dependency originalDependency = dependencyRepository.findById(depId);
			if (originalDependency!=null) {
				Set<String> descriptions = originalDependency.getDescription();
				String newDescription = dep.getDescription().iterator().next();
				if (!descriptions.contains(newDescription)) {
					descriptions.add(newDescription);
					originalDependency.setDescription(descriptions);
					dependencyRepository.save(originalDependency);
				}
			} else {
				dep.setId(depId);
				dependencyRepository.save(dep);
			}
		}
		
	}
	
	/**
	 * Splits the id lists to avoid the the Postgres query parameter amount limit
	 * 
	 * @param requirementIds
	 * @return
	 */
	private List<List<String>> splitRequirementIds(List<String> requirementIds) {
		if (requirementIds.size()<=10000) {
			return Arrays.asList(requirementIds);
		}
		List<List<String>> splitLists = new ArrayList<>();
		for (int i = 0; i < requirementIds.size(); i = i + 15000) {
			List<String> splitList = requirementIds.subList(i, i + 15000 > requirementIds.size() ? requirementIds.size() : i + 15000);
			splitLists.add(splitList);
		}
		return splitLists;
	}
	
}