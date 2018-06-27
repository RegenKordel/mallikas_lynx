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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import io.swagger.annotations.ApiOperation;

@SpringBootApplication
@Controller
//@RequestMapping("uh/mallikas/")
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
	@RequestMapping(value = "requirements", method = RequestMethod.POST)
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
	@RequestMapping(value = "dependencies", method = RequestMethod.POST)
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
	@RequestMapping(value = "project", method = RequestMethod.POST)
	public String importProjectFromMilla(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project);
		if (projectRepository.findOne(project.getId()) == null) {
			projectRepository.save(project);
		} else {
			System.out.println("Found a duplicate " + project.getId());
		}
		System.out.println("Project saved " + projectRepository.count());
		return "saved";
	}

	/**
	 * Receives an id of a Requirement (String) from Milla, and sends the
	 * Requirement object back to Milla, if it is in the database.
	 * 
	 * @param id
	 *            String received from Milla, id of a Requirement
	 * @return Requirement as a ResponseEntity, if it was found, else returns a new
	 *         ResponseEntity Not Found
	 */
	@RequestMapping(value = "mallikas/one", method = RequestMethod.POST)
	public ResponseEntity<?> sendOneRequirementToMilla(@RequestBody String id) {
		Requirement req = reqRepository.findById(id);
		System.out.println("Requested req is " + req.getId());
		List<Dependency> dependencies = dependencyRepository.findByFromId(id);
		if (req != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(req);
				String dependencyString = mapper.writeValueAsString(dependencies);
				String all = "{ \"requirements\":" + reqString + ", \"dependencies\":"+ dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

// Connection with Milla not working properly with this method at the moment 
//
//	/**
//	 * Sends all Requirements in the database as a String to Milla
//	 * 
//	 * @return all Requirements and their Dependencies as a String
//	 */
//	@RequestMapping(value = "mallikas/all", method = RequestMethod.GET)
//	public ResponseEntity<String> sendAllRequirementsToMilla() {
//		List<Requirement> allReqs = reqRepository.findAll();
//		List<Dependency> dependencies = dependencyRepository.findAll();
//		if (!allReqs.isEmpty()) {
//			try {
//				ObjectMapper mapper = new ObjectMapper();
//				String reqString = mapper.writeValueAsString(allReqs);
//				String dependencyString = mapper.writeValueAsString(dependencies);
//				String all = "{ \"requirements\":" + reqString + ", \"dependencies\":"+ dependencyString + "}";
//				return new ResponseEntity<String>(all, HttpStatus.OK);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return new ResponseEntity(HttpStatus.NOT_FOUND);
//	}

	/**
	 * Receives an id (String) of a Classifier (Component) from Milla and sends back
	 * to Milla Requirements belonging to that component
	 * 
	 * @return Requirements and Dependencies associated with them as a String, if
	 *         the List is not empty, else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "mallikas/classifiers", method = RequestMethod.POST)
	public ResponseEntity<String> sendRequirementsWithClassifierToMilla(@RequestBody String id) {
		List<Requirement> selectedReqs = reqRepository.findByClassifier(id);
		List<Dependency> allDependencies = new ArrayList();
		if (!selectedReqs.isEmpty()) {
			for (Requirement req : selectedReqs) {
				List<Dependency> dependencies = dependencyRepository.findByFromId(req.getId());
				if (!dependencies.isEmpty()) {
					allDependencies.addAll(dependencies);
				}
			}
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(selectedReqs);
				String dependencyString = mapper.writeValueAsString(allDependencies);
				String all = "{ \"requirements\":" + reqString + ", \"dependencies\":"+ dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
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
	@RequestMapping(value = "mallikas/reqs", method = RequestMethod.POST)
	public ResponseEntity<String> sendSelectedRequirementsToMilla(@RequestBody Collection<String> ids) {
		List<Requirement> selectedReqs = reqRepository.findByIdIn(ids);
		List<Dependency> dependencies = dependencyRepository.findByFromIdIn(ids);
		if (!selectedReqs.isEmpty()) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(selectedReqs);
				String dependencyString = mapper.writeValueAsString(dependencies);
				String all = "{ \"requirements\":" + reqString + ", \"dependencies\":"+ dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

	/**
	 * Receives an id of a Requirement (String) from Milla, and sends the
	 * Requirement and all Requirements that depend on it back to
	 * Milla.
	 * 
	 * @param id
	 *            String received from Milla, id of a Requirement
	 * @return Requirements and Dependencies as a ResponseEntity, if it was found,
	 *         else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "mallikas/dependent", method = RequestMethod.POST)
	public ResponseEntity<?> sendRequirementAndDependentReqsToMilla(@RequestBody String id) {
		Requirement req = reqRepository.findById(id);
		System.out.println("Requested req is " + req.getId());

		List<Dependency> dependenciesFrom = dependencyRepository.findByFromId(id);

		Set<String> reqIDs = new HashSet<>();
		if (!dependenciesFrom.isEmpty()) {
			for (Dependency dependency : dependenciesFrom) {
				String reqId = dependency.getToId();
				System.out.println("ToId " + reqId);
				if (!reqIDs.contains(reqId)) {
					reqIDs.add(reqId);
				}
			}
		}

		List<Requirement> dependentReqs = reqRepository.findByIdIn(reqIDs);

		if (req != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String reqString = mapper.writeValueAsString(req);
				String reqsString = mapper.writeValueAsString(dependentReqs);
				String dependencyFromString = mapper.writeValueAsString(dependenciesFrom);
				String all = "{\" requirement\":" +reqString + ", \"dependent_requirements\":" + reqsString + ", \"dependencies\":"+ dependencyFromString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}

}
