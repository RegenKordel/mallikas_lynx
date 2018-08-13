package eu.openreq.mallikas.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import io.swagger.annotations.ApiOperation;

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
	 * Import a Collection of Requirements from Milla and save to the
	 * RequirementRepository if the Requirement is not already in the database.
	 * 
	 * @param requirements
	 *            Collection of Requirements received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a list of requirements", notes = "Import a list of issues as OpenReq JSON Requirements")
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
	@ApiOperation(value = "Import a list of dependencies", notes = "Import a list of Jira IssueLinks as OpenReq JSON Dependencies")
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
	@ApiOperation(value = "Import a project", notes = "Import an OpenReq Project")
	@PostMapping(value = "project")
	public String importProjectFromMilla(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project.getId());
		if (projectRepository.findOne(project.getId()) == null) {
			projectRepository.save(project);
		} else {
			updateProject(project);
			System.out.println("Found a duplicate " + project.getId());
		}
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
	@ApiOperation(value = "Update selected dependencies", notes = "Update and save dependencies to database")
	@PostMapping(value = "updateDependencies")
	public ResponseEntity<?> updateDependencies(@RequestBody Collection<Dependency> dependencies) {
		//System.out.println("Received dependencies to update");
		List<Dependency> savedDependencies = new ArrayList<>();
		try {
			for (Dependency dependency : dependencies) {
				if (dependencyRepository.findById(dependency.getId()) == null) {
					savedDependencies.add(dependency);
				} else {
					updateDependency(dependency);
				}
			}
			dependencyRepository.save(savedDependencies);
			savedDependencies.clear();
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
	@ApiOperation(value = "Update selected requirements", notes = "Update and save requirements to database")
	@PostMapping(value = "updateRequirements")
	public ResponseEntity<?> updateRequirements(@RequestBody Collection<Requirement> requirements) {
		System.out.println("Received requirements to update");
		List<Requirement> savedRequirements = new ArrayList<>();

		try {
			for (Requirement requirement : requirements) {
				if (reqRepository.findById(requirement.getId()) == null) {
					savedRequirements.add(requirement);
				} else {
					if (requirement.getModified_at() > reqRepository.findById(requirement.getId()).getModified_at()) {
						System.out.println("Update necessary");
						updateRequirement(requirement);
					}
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
//	/**
//	 * Receives an id of a Requirement (String) from Milla, and sends the
//	 * Requirement object back to Milla, if it is in the database.
//	 * 
//	 * @param id
//	 *            String received from Milla, id of a Requirement
//	 * @return Requirement as a ResponseEntity, if it was found, else returns a new
//	 *         ResponseEntity Not Found
//	 */
//	@PostMapping(value = "one")
//	public ResponseEntity<?> sendOneRequirementToMilla(@RequestBody String id) {
//		Requirement req = reqRepository.findById(id);
//	//	System.out.println("Requested req is " + req.getId());
//		if (req != null) {
//		List<Dependency> dependencies = dependencyRepository.findByFromId(id);
//			try {
//				ObjectMapper mapper = new ObjectMapper();
//				String reqString = mapper.writeValueAsString(req);
//				String dependencyString = mapper.writeValueAsString(dependencies);
//				String all = "{ \"requirement\":" + reqString + ", \"dependencies\":" + dependencyString + "}";
//				return new ResponseEntity<String>(all, HttpStatus.OK);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return new ResponseEntity(HttpStatus.NOT_FOUND);
//	}

	// Should work (but the returned String might be too large to show in Swagger
	//
	/**
	 * Sends all Requirements in the database as a String to Milla
	 *
	 * @return all Requirements and their Dependencies as a String
	 */
	@RequestMapping(value = "mallikas/all", method = RequestMethod.GET)
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
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Receives an id (String) of a Classifier (Component) from Milla and sends back
	 * to Milla Requirements belonging to that component
	 * 
	 * @return Requirements and Dependencies associated with them as a String, if
	 *         the List is not empty, else returns a new ResponseEntity Not Found
	 */
	@PostMapping(value = "classifiers")
	public ResponseEntity<String> sendRequirementsWithClassifierToMilla(@RequestBody String id) {
		List<Requirement> selectedReqs = reqRepository.findByClassifier(id);
		List<Dependency> allDependencies = new ArrayList<Dependency>();
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			for (Requirement req : selectedReqs) {
				List<Dependency> dependencies = dependencyRepository.findByFromId(req.getId());
				List<Dependency> dependenciesTo = dependencyRepository.findByToId(req.getId());
				// if (!dependencies.isEmpty()) {
				allDependencies.addAll(dependencies);
				allDependencies.addAll(dependenciesTo);
				// }
			}
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, allDependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Receives a Collection of Requirement ids (String) from Milla and sends back
	 * to Milla a List of selected Requirements.
	 * 
	 * @return selected Requirements and Dependencies associated with them as a
	 *         String, if the List is not empty, else returns a new ResponseEntity
	 *         Not Found
	 */
	@PostMapping(value = "selectedReqs")
	public ResponseEntity<String> sendSelectedRequirementsToMilla(@RequestBody Collection<String> ids) {
		List<Requirement> selectedReqs = reqRepository.findByIdIn(ids);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<Dependency> dependencies = dependencyRepository.findByFromIdIn(ids);
			List<Dependency> dependenciesTo = dependencyRepository.findByToIdIn(ids);
			dependencies.addAll(dependenciesTo);
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Receives a projectId from Milla and sends back all requirements and their
	 * dependencies in that project
	 * 
	 * @param projectId
	 * @return
	 */
	@PostMapping(value = "projectRequirements")
	public ResponseEntity<String> sendRequirementsInProjectToMilla(@RequestBody String projectId) {
		Project project = projectRepository.findById(projectId);
		if (project != null) {
			// System.out.println("Sending projects to Milla");
			List<String> requirementIds = project.getSpecifiedRequirements();
			List<Requirement> requirements = reqRepository.findByIdIn(requirementIds);
			List<Dependency> dependencies = dependencyRepository.findByFromIdIn(requirementIds);
			List<Dependency> dependenciesTo = dependencyRepository.findByToIdIn(requirementIds);
			dependencies.addAll(dependenciesTo);
			if (!requirementIds.isEmpty()) {
				try {
					return new ResponseEntity<String>(createJsonString(project, null, requirements, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
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
	@PostMapping(value = "dependents")
	public ResponseEntity<?> sendRequirementAndDependentReqsToMilla(@RequestBody String id) {
		Requirement requirement = reqRepository.findById(id);
		// System.out.println("Requested req is " + requirement.getId());
		if (requirement != null) {

			List<Dependency> dependenciesFrom = dependencyRepository.findByFromId(id);
			List<Dependency> dependenciesTo = dependencyRepository.findByToId(id);
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
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends a list of requirements (and dependencies) that have the same
	 * requirement resolution to Milla
	 * 
	 * @param resolutionValue
	 * @return
	 */
	@PostMapping(value = "reqsWithResolution")
	public ResponseEntity<String> sendRequirementsWithResolutionToMilla(@RequestBody String resolutionValue) {
		List<Requirement> selectedReqs = reqRepository.findByRequirementPart(resolutionValue);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<String> reqIds = new ArrayList<>();
			for (Requirement req : selectedReqs) {
				reqIds.add(req.getId());
			}
			List<Dependency> dependencies = dependencyRepository.findByFromIdIn(reqIds);
			List<Dependency> dependenciesTo = dependencyRepository.findByToIdIn(reqIds);
			dependencies.addAll(dependenciesTo);

			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Sends a list of requirements (and dependencies) that have the same
	 * requirement type and/or status to Milla
	 * 
	 * @param whole
	 * @return
	 */
	@PostMapping(value = "reqsWithType")
	public ResponseEntity<String> sendRequirementsWithTypeToMilla(@RequestBody String whole) {
		String[] parts = splitString(whole);
		List<Requirement> selectedReqs = null;
		try {

			selectedReqs = createRequirements(parts);

			if (selectedReqs == null) {
				return new ResponseEntity("Search failed", HttpStatus.NOT_FOUND);
			}
			if (!selectedReqs.isEmpty() || selectedReqs != null) {
				try {
					List<String> reqIds = new ArrayList<>();
					for (Requirement req : selectedReqs) {
						reqIds.add(req.getId());
					}
					List<Dependency> dependencies = dependencyRepository.findByFromIdIn(reqIds);
					List<Dependency> dependenciesTo = dependencyRepository.findByToIdIn(reqIds);
					dependencies.addAll(dependenciesTo);
					return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
							HttpStatus.OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IllegalArgumentException e) {
			new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity(HttpStatus.NOT_FOUND);
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
				String reqToId = dependency.getToId();
				if (!reqIDs.contains(reqToId)) {
					reqIDs.add(reqToId);
				}
				String reqFromId = dependency.getFromId();
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
		ObjectMapper mapper = new ObjectMapper();
		String dependencyString = mapper.writeValueAsString(dependencies);
		String reqsString = mapper.writeValueAsString(requirements);
		String jsonString;
		if (requirement != null) {
			String reqString = mapper.writeValueAsString(requirement);
			jsonString = "{\"requirement\":" + reqString + ", \"requirements\":" + reqsString + ", \"dependencies\":"
					+ dependencyString + "}";
		} else if (project != null) {
			String projectString = mapper.writeValueAsString(project);
			jsonString = "{ \"project\":" + projectString + ", \"requirements\":" + reqsString + ", \"dependencies\":"
					+ dependencyString + "}";
		} else {
			jsonString = "{ \"requirements\":" + reqsString + ", \"dependencies\":" + dependencyString + "}";
		}
		return jsonString;
	}

	/**
	 * Update a dependency with the information (mainly status) of the dependency
	 * received as a parameter
	 * 
	 * @param dependency
	 */
	private void updateDependency(Dependency dependency) {
		Dependency updatedDependency = dependencyRepository.findById(dependency.getId());
		updatedDependency.setCreated_at(dependency.getCreated_at());
		updatedDependency.setDependency_score(dependency.getDependency_score());
		updatedDependency.setDependency_type(dependency.getDependency_type());
		updatedDependency.setFromId(dependency.getFromId());
		updatedDependency.setToId(dependency.getToId());
		updatedDependency.setStatus(dependency.getStatus());
		dependencyRepository.save(updatedDependency);
	}

	private void updateRequirement(Requirement requirement) {
		Requirement updatedReq = reqRepository.findById(requirement.getId());
		updatedReq.setCreated_at(requirement.getCreated_at());
		updatedReq.setModified_at(requirement.getModified_at());
		updatedReq.setName(requirement.getName());
		updatedReq.setPriority(requirement.getPriority());
		updatedReq.setRequirement_type(requirement.getRequirement_type());
		updatedReq.setStatus(requirement.getStatus());
		updatedReq.setChildren(requirement.getChildren());
		updatedReq.setClassifierResults(requirement.getClassifierResults());
		updatedReq.setComments(requirement.getComments());
		updatedReq.setRequirementParts(requirement.getRequirementParts());
		updatedReq.setText(requirement.getText());
		reqRepository.save(updatedReq);
	}

	private void updateProject(Project project) {
		Project updatedProject = projectRepository.findById(project.getId());
		updatedProject.setCreated_at(project.getCreated_at());
		updatedProject.setModified_at(project.getModified_at()); // Should this be new Date.getTime()?
		updatedProject.setName(project.getName());
		updatedProject.setSpecifiedRequirements(project.getSpecifiedRequirements());
		projectRepository.save(updatedProject);
	}

}