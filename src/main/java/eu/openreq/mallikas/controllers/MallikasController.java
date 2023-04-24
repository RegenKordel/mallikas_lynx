package eu.openreq.mallikas.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.closedreq.bridge.models.json.Dependency;
import eu.closedreq.bridge.models.json.Project;
import eu.closedreq.bridge.models.json.RequestParams;
import eu.closedreq.bridge.models.json.Requirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.openreq.mallikas.services.FilteringService;
import eu.openreq.mallikas.services.UpdateService;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@SpringBootApplication
@Controller
@RequestMapping("/")
public class MallikasController {
	
	@Autowired
	FilteringService filteringService;

	@Autowired
	UpdateService updateService;
	
	/**
	 * Check which projects are already saved in the database
	 * 
	 * @return
	 */
	@ApiOperation(value = "Get a list (map) of projects currently saved", notes = "Get a map with ids of all saved projects and their requirement counts.")
	@GetMapping(value = "listOfProjects")
	public ResponseEntity<Map<String, Integer>> getListOfProjects() {
		return filteringService.listOfProjects();
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
	public ResponseEntity<String> importRequirements(@RequestBody Collection<Requirement> requirements) {
		return updateService.importRequirements(requirements);
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
		return updateService.importDependencies(dependencies);
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
	@Transactional
	public ResponseEntity<String> importProject(@RequestBody Project project) {
		return updateService.importProject(project);
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
	public ResponseEntity<String> updateDependencies(@RequestBody Collection<Dependency> dependencies, 
			boolean userInput, boolean isProposed) {
		return updateService.updateDependencies(dependencies, userInput, isProposed);
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
	public ResponseEntity<String> updateRequirements(@RequestBody Collection<Requirement> requirements, 
			@RequestParam String projectId) {
		return updateService.updateRequirements(requirements, projectId);
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
		return updateService.updateProjectSpecifiedRequirements(reqIds, projectId);
	}

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
		return filteringService.allRequirements();
	}


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
		return filteringService.allDependencies();
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
		return filteringService.selectedRequirements(ids);
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
		return filteringService.requirementsByParams(params);
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
		return filteringService.dependenciesByParams(params);
	}
	
	/**
	 * Replaces the dependency from/to ids with correct ones if they were the wrong way around, also returns project ids
	 * @return
	 */
	@ApiIgnore
	@PostMapping(value = "correctIdsForDependencies") 
	public ResponseEntity<String> getCorrectIdsForDependencies(@RequestBody List<Dependency> dependencies) {
		return filteringService.correctIdsForDependencies(dependencies);
	}
	
	/**
	 * Finds the projects where the dependencies belong, returning a hashmap
	 * @return
	 */
	@ApiIgnore
	@PostMapping(value = "projectsForDependencies") 
	public ResponseEntity<String> getProjectsForDependencies(@RequestBody List<Dependency> dependencies) {
		return filteringService.projectsForDependencies(dependencies);
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
			@RequestParam(required = false) boolean includeProposed, @RequestParam(required = false) boolean requirementsOnly) {
		return filteringService.requirementsInProject(projectId, includeProposed, requirementsOnly);
	}

	
	
	/**
	 * Empties the database. Optionally keep dependencies with the status "Rejected"
	 * @param keepRejected
	 * @return
	 */
	@ApiIgnore
	@DeleteMapping(value = "deleteEverything")
	@Transactional
	public ResponseEntity<String> deleteEverything(@RequestParam Boolean keepRejected) {
		return updateService.deleteEverything(keepRejected);
	}	
	
	
}