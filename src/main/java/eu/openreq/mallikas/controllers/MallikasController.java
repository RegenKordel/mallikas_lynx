package eu.openreq.mallikas.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_status;
import eu.openreq.mallikas.models.json.Dependency_type;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.RequestParams;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@SpringBootApplication
@Controller
@RequestMapping("/")
public class MallikasController {

	@Autowired
	RequirementRepository reqRepository;

	@Autowired
	DependencyRepository dependencyRepository;

	@Autowired
	ProjectRepository projectRepository;

	/**
	 * Check which projects are already saved in the database
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get a list of projects", notes = "Get a list of ids of all projects saved in the database.")
	@GetMapping(value = "listAllProjects")
	public ResponseEntity<?> getAListOfProjects() {
		List<Project> projects = projectRepository.findAll();
		List<String> projectIds = new ArrayList<String>();
		if (projects.isEmpty() || projects == null) {
			return new ResponseEntity<>("No projects in the database", HttpStatus.NOT_FOUND);
		}

		for (Project project : projects) {
			projectIds.add(project.getId());
		}
		return new ResponseEntity<>(projectIds, HttpStatus.FOUND);
	}

	/**
	 * Import a Collection of Requirements from Milla and save to the
	 * RequirementRepository if the Requirement is not already in the database.
	 * 
	 * @param requirements
	 *            Collection of Requirements received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a list of requirements", notes = "Import a list of new OpenReq JSON Requirements to the database. If a requirement exists, it is not updated or changed.")
	@PostMapping(value = "requirements")
	public String importRequirementsFromMilla(@RequestBody Collection<Requirement> requirements) {
		System.out.println("Received requirements from Milla");
		List<Requirement> savedReqs = new ArrayList<>();
		for (Requirement req : requirements) {
			if (reqRepository.findById(req.getId()) == null) {
				savedReqs.add(req);
			} else {
				System.out.println("Found a duplicate " + req.getId());
			}
		}
		reqRepository.save(savedReqs);
		System.out.println("Requirements saved " + reqRepository.count());
		savedReqs.clear();
		return "saved";
	}

	/**
	 * Import a Collection of Dependencies from Milla and save to the
	 * DependencyRepository
	 * 
	 * @param dependencies
	 *            Collection of Dependencies received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a list of dependencies", notes = "Import a list of new OpenReq JSON Dependencies to the database. If a dependency exists, it is not updated or changed.")
	@PostMapping(value = "dependencies")
	public String importDependenciesFromMilla(@RequestBody Collection<Dependency> dependencies) {
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
		return "saved";
	}

	/**
	 * Import a Project from Milla and save to the ProjectRepository
	 * 
	 * @param project
	 *            Project received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a project", notes = "Import an OpenReq JSON project to the database. If a project exists, it is updated.")
	@PostMapping(value = "project")
	public String importProjectFromMilla(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project.getId());
		if (projectRepository.findOne(project.getId()) != null) {
			System.out.println("Found a duplicate " + project.getId());
		} 
		projectRepository.save(project);	
		
		System.out.println("Project saved " + projectRepository.count());
		return "saved";
	}

	/**
	 * Update selected Dependencies
	 * 
	 * @param dependencies
	 *            Collection of Dependencies received from Milla
	 * @return String "Dependencies updated" if the update operation is successful
	 */
	@ApiOperation(value = "Update selected dependencies", notes = "Update existing and save new dependencies in the database.")
	@PostMapping(value = "updateDependencies")
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
	@ApiOperation(value = "Update selected requirements", notes = "Update existing and save new requirements to the database.")
	@PostMapping(value = "updateRequirements")
	public ResponseEntity<?> updateRequirements(@RequestBody Collection<Requirement> requirements) {
		// System.out.println("Received requirements to update " + requirements.size());
		List<Requirement> savedRequirements = new ArrayList<>();

		try {
			for (Requirement requirement : requirements) {
				if (reqRepository.findById(requirement.getId()) == null) {
					savedRequirements.add(requirement);
				} else if (requirement.getModified_at() > reqRepository.findById(requirement.getId()).getModified_at()) {
					savedRequirements.add(requirement);
				}
			}
			reqRepository.save(savedRequirements);
			System.out.println("Requirements saved " + reqRepository.count());
			savedRequirements.clear();
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
	}

	// Probably unnecessary
	/**
	 * Receives an id of a Requirement (String) from Milla, and sends the
	 * Requirement object back to Milla, if it is in the database.
	 * 
	 * @param id
	 *            String received from Milla, id of a Requirement
	 * @return Requirement as a ResponseEntity, if it was found, else returns a new
	 *         ResponseEntity Not Found
	 */
	@ApiOperation(value = "Get a requirement", notes = "Get a requirement saved in the database.")
	@PostMapping(value = "one")
	public ResponseEntity<String> sendOneRequirementToMilla(@RequestBody String id) {
		Requirement req = reqRepository.findById(id);
		List<Requirement> requirements = new ArrayList<>();

		if (req != null) {
			requirements.add(req);
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(requirements);
				String all = "{ \"requirements\":" + reqString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Should work (but the returned String might be too large to show in Swagger
	//
	/**
	 * Sends all Requirements in the database as a String to Milla
	 *
	 * @return all Requirements and their Dependencies as a String
	 */
	@ApiOperation(value = "Get a list of all requirements", notes = "Get a list of all requirements saved in the database.")
	@RequestMapping(value = "allRequirements", method = RequestMethod.GET)
	public ResponseEntity<String> sendAllRequirementsToMilla() {
		List<Requirement> allReqs = reqRepository.findAll();
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
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Should work (but the returned String might be too large to show in Swagger
	//
	/**
	 * Get all dependencies from the database as a JSON String
	 *
	 * @return
	 */
	@ApiOperation(value = "Get a list of all dependencies", notes = "Get a list of all dependencies saved in the database.")
	@RequestMapping(value = "allDependencies", method = RequestMethod.GET)
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
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// /**
	// * Receives an id (String) of a Classifier (Component) from Milla and sends
	// back
	// * to Milla Requirements belonging to that component
	// *
	// * @return Requirements and Dependencies associated with them as a String, if
	// * the List is not empty, else returns a new ResponseEntity Not Found
	// */
	// @PostMapping(value = "classifiers")
	// public ResponseEntity<String>
	// sendRequirementsWithClassifierToMilla(@RequestBody String id) {
	// List<Requirement> selectedReqs = reqRepository.findByClassifier(id);
	// List<Dependency> allDependencies = new ArrayList<Dependency>();
	// if (!selectedReqs.isEmpty() && selectedReqs != null) {
	// for (Requirement req : selectedReqs) {
	// List<Dependency> dependencies =
	// dependencyRepository.findByFromid(req.getId());
	// List<Dependency> dependenciesTo =
	// dependencyRepository.findByToid(req.getId());
	// // if (!dependencies.isEmpty()) {
	// allDependencies.addAll(dependencies);
	// allDependencies.addAll(dependenciesTo);
	// // }
	// }
	// try {
	// return new ResponseEntity<String>(createJsonString(null, null, selectedReqs,
	// allDependencies),
	// HttpStatus.OK);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// return new ResponseEntity(HttpStatus.NOT_FOUND);
	// }

	/**
	 * Receives a Collection of Requirement ids (String) from Milla and sends back
	 * to Milla a List of selected Requirements.
	 * 
	 * @return selected Requirements and Dependencies associated with them as a
	 *         String, if the List is not empty, else returns a new ResponseEntity
	 *         Not Found
	 */
	@ApiOperation(value = "Get a list of selected requirements", notes = "Get a list of selected requirements saved in the database.")
	@PostMapping(value = "selectedReqs")
	public ResponseEntity<String> sendSelectedRequirementsToMilla(@RequestBody Collection<String> ids) {
		List<Requirement> selectedReqs = reqRepository.findByIdIn(ids);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<Dependency> dependencies = dependencyRepository.findByIdIn(ids);
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	

	/**
	 * Receives a Collection of Requirement ids (String) from Milla and sends back
	 * to Milla a List of selected Requirements.
	 * 
	 * @return selected Requirements and Dependencies associated with them as a
	 *         String, if the List is not empty, else returns a new ResponseEntity
	 *         Not Found
	 */
	@PostMapping(value = "reqsSinceDate")
	public ResponseEntity<String> sendRequirementsSinceDateToMilla(@RequestBody Long created_at) {
		List<Requirement> selectedReqs = reqRepository.findCreatedSinceDate(created_at);
		System.out.print(selectedReqs);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<String> ids = new ArrayList<String>();
			for (Requirement req : selectedReqs) {
				ids.add(req.getId());
			}
			List<Dependency> dependencies = dependencyRepository.findByIdIn(ids);;
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// /**
	// * Receives a projectId from Milla and sends back all requirements and their
	// * dependencies in that project (single project version)
	// *
	// * @param projectId
	// * @return
	// */
	// @PostMapping(value = "projectRequirements")
	// public ResponseEntity<String> sendRequirementsInProjectToMilla(@RequestBody
	// String projectId) {
	// Project project = projectRepository.findById(projectId);
	// if (project != null) {
	// // System.out.println("Sending projects to Milla");
	// List<String> requirementIds = project.getSpecifiedRequirements();
	// List<Requirement> requirements = reqRepository.findByIdIn(requirementIds);
	// List<Dependency> dependencies =
	// dependencyRepository.findByFromidIn(requirementIds);
	// List<Dependency> dependenciesTo =
	// dependencyRepository.findByToidIn(requirementIds);
	// dependencies.addAll(dependenciesTo);
	// if (!requirementIds.isEmpty()) {
	// try {
	// return new ResponseEntity<String>(createJsonString(project, null,
	// requirements, dependencies),
	// HttpStatus.OK);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return new ResponseEntity(HttpStatus.NOT_FOUND);
	// }
	
	@PostMapping(value = "onlyDependenciesByParams") 
	public ResponseEntity<String> sendOnlyDependenciesByParamsToMilla(@RequestBody RequestParams params) {
		List<String> reqIds = params.getRequirementIds();
		
		List<Requirement> selectedReqs = reqRepository.findByIdIn(reqIds);
		
		if (!selectedReqs.isEmpty() && selectedReqs!=null) {
			Pageable pageLimit = new PageRequest(0, Integer.MAX_VALUE);
			
			if (params.getMaxDependencies()!=null && params.getMaxDependencies()>0) {
				pageLimit = new PageRequest(0, params.getMaxDependencies());
			}
			
			List<Dependency> dependencies = dependencyRepository.findByIdWithParams(reqIds, params.getScoreThreshold(), 
					params.getIncludeProposed(), params.getProposedOnly(), params.getIncludeRejected(), pageLimit);
			
			List<String> dependentReqIds = new ArrayList<String>();
			
			for (Dependency dep : dependencies) {
				if (!reqIds.contains(dep.getFromid())) {
					dependentReqIds.add(dep.getFromid());
				}
				if (!reqIds.contains(dep.getToid())) {
					dependentReqIds.add(dep.getToid());
				}
			}
			
			selectedReqs.addAll(reqRepository.findByIdIn(dependentReqIds));

			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies), HttpStatus.OK);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends requirements to Milla based on the parameters given (multiple parameters can be used simultaneously)
	 * @param params
	 * @return
	 */
	@PostMapping(value = "requirementsByParams")
	public ResponseEntity<String> sendRequirementsByParamsToMilla(@RequestBody RequestParams params) {
		
		List<Project> projects = null;
		List<String> reqIds = params.getRequirementIds();
		
		if (params.getProjectId() != null && projectRepository.findById(params.getProjectId())!=null) {
			Project project = projectRepository.findById(params.getProjectId());
			projects = new ArrayList<Project>();
			projects.add(project);
			List<String> projectReqIds = project.getSpecifiedRequirements();
			if (reqIds==null) {
				reqIds = projectReqIds;
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
		
		List<Requirement> selectedReqs = reqRepository.findByParams(reqIds, created, modified, type, status);
		
		if (params.getResolution()!=null) {
			List<Requirement> resolutionReqs = reqRepository.findByRequirementPart(params.getResolution());
			if (selectedReqs!=null) {
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
			
			dependencies = dependencyRepository.findByIdWithParams(ids, params.getScoreThreshold(),
					params.getIncludeProposed(), params.getProposedOnly(), params.getIncludeRejected(), pageLimit);
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
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Receives a projectId from Milla and sends back all requirements and their
	 * dependencies in that project (projects list version)
	 * 
	 * @param projectId
	 * @return
	 */
	@ApiOperation(value = "Get the requirements including dependencies of a project",
			notes = "Get the requirements including dependencies of a project saved in the database.")
	@PostMapping(value = "projectRequirements")
	public ResponseEntity<String> sendRequirementsInProjectToMilla(@RequestBody String projectId) {
		Project project = projectRepository.findById(projectId);

		if (project != null) {
			// System.out.println("Sending projects to Milla");
			List<Project> projects = new ArrayList<>();
			projects.add(project);
			List<String> requirementIds = project.getSpecifiedRequirements();
			List<Requirement> requirements = reqRepository.findByIdIn(requirementIds);
			List<Dependency> dependencies = dependencyRepository.findByIdIn(requirementIds);
			if (!requirementIds.isEmpty()) {
				try {
					return new ResponseEntity<String>(createUPCJsonString(projects, requirements, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Receives an id of a Requirement (String) from Milla, and sends the
	 * Requirement and all Requirements that depend on it back to Milla.
	 * 
	 * @param id
	 *            String received from Milla, id of a Requirement
	 * @return Requirements and Dependencies as a ResponseEntity, if it was found,
	 *         else returns a new ResponseEntity Not Found
	 */
	@ApiOperation(value = "Get the dependent requirements including dependencies of a requirement",
			notes = "Get the dependent requirements including dependencies of a requirement saved in the database.")
	@PostMapping(value = "dependents")
	public ResponseEntity<String> sendRequirementAndDependentReqsToMilla(@RequestBody String id) {
		Requirement requirement = reqRepository.findById(id);
		// System.out.println("Requested req is " + requirement);
		if (requirement != null) {

			List<Dependency> dependenciesFrom = dependencyRepository.findByFromid(id);
			List<Dependency> dependenciesTo = dependencyRepository.findByToid(id);
			dependenciesFrom.addAll(dependenciesTo);
			Set<String> requirementIDs = collectRequirementIDs(dependenciesFrom);
			requirementIDs.remove(requirement.getId());
			List<Requirement> dependentReqs = reqRepository.findByIdIn(requirementIDs);

			try {
				return new ResponseEntity<String>(createJsonString(null, requirement, dependentReqs, dependenciesFrom),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends a list of requirements (and dependencies) that have the same
	 * requirement resolution to Milla
	 * 
	 * @param resolutionValue
	 * @return
	 */
	@ApiOperation(value = "Get a list of requirements including  dependencies with the resolution",
			notes = "Get a list of requirements including  dependencies with the specific resolution saved in the database.")
	@PostMapping(value = "reqsWithResolution")
	public ResponseEntity<String> sendRequirementsWithResolutionToMilla(@RequestBody String resolutionValue) {
		List<Requirement> selectedReqs = reqRepository.findByRequirementPart(resolutionValue);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<String> reqIds = new ArrayList<>();
			for (Requirement req : selectedReqs) {
				reqIds.add(req.getId());
			}
			List<Dependency> dependencies = dependencyRepository.findByIdIn(reqIds);
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends a list of requirements (and dependencies) that have the same
	 * requirement type and/or status to Milla
	 * 
	 * @param whole
	 * @return
	 */
	@ApiOperation(value = "Get a list of requirements including  dependencies with the requirement type",
			notes = "Get a list of requirements including  dependencies with the specific requirement type saved in the database.")
	@PostMapping(value = "reqsWithType")
	public ResponseEntity<String> sendRequirementsWithTypeToMilla(@RequestBody String whole) {
		String[] parts = splitString(whole);
		List<Requirement> selectedReqs = null;
		try {

			selectedReqs = createRequirements(parts);

			if (selectedReqs == null) {
				return new ResponseEntity<>("Search failed", HttpStatus.NOT_FOUND);
			}
			if (!selectedReqs.isEmpty() || selectedReqs != null) {
				try {
					List<String> reqIds = new ArrayList<>();
					for (Requirement req : selectedReqs) {
						reqIds.add(req.getId());
					}
					List<Dependency> dependencies = dependencyRepository.findByIdIn(reqIds);
					return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IllegalArgumentException e) {
			new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends a list of requirements (and dependencies) that have the same dependency
	 * type to Milla
	 * 
	 * @param type
	 *            Dependency_type as a String
	 * @return
	 */
	@ApiOperation(value = "Get a list of requirements including  dependencies with the dependency type",
			notes = "Get a list of requirements including  dependencies with the specific dependency type saved in the database.")
	@PostMapping(value = "reqsWithDependencyType")
	public ResponseEntity<String> sendRequirementsWithDependencyTypeToMilla(@RequestBody String type) {
		List<Requirement> selectedReqs = null;
		try {
			List<Dependency> dependencies = dependencyRepository.findByType(Dependency_type.valueOf(type));
			Set<String> reqIds = new HashSet<>();
			for (Dependency dependency : dependencies) {
				if (!reqIds.contains(dependency.getFromid())) {
					reqIds.add(dependency.getFromid());
				}
				if (!reqIds.contains(dependency.getToid())) {
					reqIds.add(dependency.getToid());
				}
			}

			selectedReqs = reqRepository.findByIdIn(reqIds);

			if (selectedReqs == null) {
				return new ResponseEntity<>("Search failed", HttpStatus.NOT_FOUND);
			}
			if (!selectedReqs.isEmpty()) {
				try {
					return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IllegalArgumentException e) {
			new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Updates the speficiedRequirements list of a selected project with the ids of the latest updated requirements in that project.
	 * @param reqIds
	 * @return
	 */
	@ApiOperation(value = "Update a list of requirements",
			notes = "Update a list of requirements of the given project saved in the database.")
	@PostMapping(value = "updateProjectSpecifiedRequirements/")
	public ResponseEntity<String> updateProjectSpecifiedRequirements(@RequestBody Map<String, Collection<String>> reqIds, @RequestParam String projectId) {
		try {
			Project project = projectRepository.findById(projectId);
			
			project.getSpecifiedRequirements().addAll(reqIds.get(projectId));
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Creates a list of requirements based on the values in a String array (array
	 * contains values of Requirement_type and Requirement_status)
	 * 
	 * @param parts
	 * @return
	 */
	private List<Requirement> createRequirements(String[] parts) {
		List<Requirement> selectedReqs = null;
//		System.out.println("Parts 0 " + parts[0]);
//		System.out.println("Parts 1 " + parts[1]);
		if (!parts[0].equals("null") && !parts[1].equals("null")) {
			selectedReqs = reqRepository.findByTypeAndStatus(Requirement_type.valueOf(parts[0]),
					Requirement_status.valueOf(parts[1]));
		} else if (!parts[0].equals("null") && parts[1].equals("null")) {
			selectedReqs = reqRepository.findByType(Requirement_type.valueOf(parts[0]));
		} else if (parts[0].equals("null") && !parts[1].equals("null")) {
			selectedReqs = reqRepository.findByStatus(Requirement_status.valueOf(parts[1]));
		}
		return selectedReqs;
	}

	private String[] splitString(String word) {
		String[] parts = word.split("\\+");
		return parts;
	}

	/**
	 * Create a List containing Requirement IDs (String) that are extracted from a
	 * List of Dependencies
	 * 
	 * @param dependencies
	 * @return
	 */
	private Set<String> collectRequirementIDs(List<Dependency> dependencies) {
		Set<String> reqIDs = new HashSet<>();
		if (!dependencies.isEmpty()) {
			for (Dependency dependency : dependencies) {
				String reqToId = dependency.getToid();
				if (!reqIDs.contains(reqToId)) {
					reqIDs.add(reqToId);
				}
				String reqFromId = dependency.getFromid();
				if (!reqIDs.contains(reqFromId)) {
					reqIDs.add(reqFromId);
				}
			}
		}
		return reqIDs;
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
		Gson gson = new Gson();
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
	

//	private String createJsonStringReqIdsOnly(List<String> requirementIds, List<Dependency> dependencies)
//		throws JsonProcessingException {
//		Gson gson = new Gson();
//		String reqIdsString = gson.toJson(requirementIds);
//		String dependencyString = gson.toJson(dependencies);
//		String jsonString = "{\"requirementIds\":" + reqIdsString + ", \"dependencies\":" + dependencyString + "}";
//		
//		return jsonString;
//	}

	// /**
	// * Create a String containing (possibly) Project, Requirements and
	// Dependencies
	// * in JSON format
	// *
	// * @param project
	// * @param requirement
	// * @param requirements
	// * @param dependencies
	// * @return
	// * @throws JsonProcessingException
	// */
	// private String createJsonString(Project project, Requirement requirement,
	// List<Requirement> requirements,
	// List<Dependency> dependencies) throws JsonProcessingException {
	// ObjectMapper mapper = new ObjectMapper();
	// String dependencyString = mapper.writeValueAsString(dependencies);
	// String reqsString = mapper.writeValueAsString(requirements);
	// String jsonString;
	// if (requirement != null) {
	// String reqString = mapper.writeValueAsString(requirement);
	// jsonString = "{\"requirement\":" + reqString + ", \"requirements\":" +
	// reqsString + ", \"dependencies\":"
	// + dependencyString + "}";
	// } else if (project != null) {
	// String projectString = mapper.writeValueAsString(project);
	// jsonString = "{ \"project\":" + projectString + ", \"requirements\":" +
	// reqsString + ", \"dependencies\":"
	// + dependencyString + "}";
	// } else {
	// jsonString = "{ \"requirements\":" + reqsString + ", \"dependencies\":" +
	// dependencyString + "}";
	// }
	// return jsonString;
	// }
	
	/**
	 * Empties the database except for dependencies with the "rejected" status
	 * @param reqIds
	 * @return
	 */
	@ApiIgnore
	@DeleteMapping(value = "deleteEverythingButRejectedDependencies")
	public ResponseEntity<String> deleteEverythingButRejectedDependencies() {
		
		projectRepository.deleteAll();
		reqRepository.deleteAll();
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
		Gson gson = new Gson();
		String dependencyString = gson.toJson(dependencies);
		String reqsString = gson.toJson(requirements);
		String projectsString = gson.toJson(projects);
		String jsonString = "{ \"projects\":" + projectsString + ", \"requirements\":" + reqsString
				+ ", \"dependencies\":" + dependencyString + "}";
		return jsonString;
	}

	// /**
	// * Create a String containing Projects, Requirements and Dependencies
	// * in JSON format for UPC
	// *
	// * @param projects
	// * @param requirements
	// * @param dependencies
	// * @return
	// * @throws JsonProcessingException
	// */
	// private String createUPCJsonString(List<Project> projects, List<Requirement>
	// requirements,
	// List<Dependency> dependencies) throws JsonProcessingException {
	// ObjectMapper mapper = new ObjectMapper();
	// String dependencyString = mapper.writeValueAsString(dependencies);
	// String reqsString = mapper.writeValueAsString(requirements);
	// String projectsString = mapper.writeValueAsString(projects);
	// String jsonString = "{ \"projects\":" + projectsString + ",
	// \"requirements\":" + reqsString + ", \"dependencies\":"
	// + dependencyString + "}";
	// return jsonString;
	// }

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
				List<String> descriptions = originalDependency.getDescription();
				String newDescription = dep.getDescription().get(0);
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
}