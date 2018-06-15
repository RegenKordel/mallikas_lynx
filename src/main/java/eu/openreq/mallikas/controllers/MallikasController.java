package eu.openreq.mallikas.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;
import io.swagger.annotations.ApiOperation;

@SpringBootApplication
@Controller
public class MallikasController {
	
	@Autowired
	RequirementRepository reqRepository;
	
	@Autowired
	DependencyRepository dependencyRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
//	@ApiOperation(value = "Return Hello Mallikas",
//		    notes = "Just a simple test")
//	@RequestMapping(value = "/test", method = RequestMethod.GET)
//    public @ResponseBody String greeting() {
//        return "Hello Mallikas";
//    }
	
	/**
	 * Import a Collection of Requirements from Milla and save to the RequirementRepository if the Requirement is not already in the database. 
	 * @param requirements Collection of Requirements received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a list of requirements",
			notes = "Import a list of issues as OpenReq JSON Requirements")
	@RequestMapping(value = "/mallikas", method = RequestMethod.POST)
	public String importRequirementsFromMilla(@RequestBody Collection<Requirement> requirements) {
		System.out.println("Received requirements from Milla");
		List<Requirement> savedReqs = new ArrayList<>();
		for(Requirement req : requirements) {
			if(reqRepository.findById(req.getId())==null) {
				savedReqs.add(req);
			}
			else {
				System.out.println("Found a duplicate " + req.getId());
			}
		}
		reqRepository.save(savedReqs);
		System.out.println("Requirements saved " + reqRepository.count());
		savedReqs.clear();
		return "saved";
	}
	
	/**
	 * Import a Collection of Dependencies from Milla and save to the DependencyRepository
	 * @param dependencies Collection of Dependencies received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a list of dependencies",
			notes = "Import a list of Jira IssueLinks as OpenReq JSON Dependencies")
	@RequestMapping(value = "/dependencies", method = RequestMethod.POST)
	public String importDependenciesFromMilla(@RequestBody Collection<Dependency> dependencies) {
		System.out.println("Received dependencies from Milla " + dependencies);
		List<Dependency> savedDependencies = new ArrayList<>();
		for(Dependency dependency : dependencies) {
			if(dependencyRepository.findById(dependency.getId())==null) {
				savedDependencies.add(dependency);
			}
			else {
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
	 * @param project Project received from Milla
	 * @return String "saved" if the import operation is successful
	 */
	@ApiOperation(value = "Import a project",
			notes = "Import an OpenReq Project")
	@RequestMapping(value = "/project", method = RequestMethod.POST)
	public String importProjectFromMilla(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project);
			if(projectRepository.findOne(project.getId())==null) {
				projectRepository.save(project);
			}
			else {
				System.out.println("Found a duplicate " + project.getId());
			}	
		System.out.println("Project saved " + projectRepository.count());
		return "saved";
	}
	
	/**
	 * Receives a key of a Requirement (String) from Milla, and sends the Requirement object back to Milla, if it is in the database. 
	 * @param key String received from Milla, key (identifier) of a Requirement
	 * @return Requirement as a ResponseEntity, if it was found, else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "/mallikas/{key}")
	public ResponseEntity<Requirement> sendOneRequirementToMilla(@PathVariable("key") String key) {
		System.out.println("Milla asked for a requirement");
			Requirement req = reqRepository.findById(key);
			System.out.println("Requested req is " + req.getId());
			if(req!=null) {
				return new ResponseEntity<Requirement>(req, HttpStatus.OK);
			}
		return new ResponseEntity(HttpStatus.NOT_FOUND);	
	}
	
	/**
	 * Sends all Requirements in the database as a List to Milla
	 * @return all Requirements (List<Requirement> as a ResponseEntity), if the List is not empty, else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "/mallikas/all")
	public ResponseEntity<List<Requirement>> sendAllRequirementsToMilla() {		
		System.out.println("Milla asked for all requirements");
		List<Requirement> reqs = reqRepository.findAll();
		if(!reqs.isEmpty()) {
			return new ResponseEntity<List<Requirement>>(reqs, HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);	
	}
	
	/**
	 * Sends all Requirements with a selected Classifier as a List to Milla. 
	 * @param id String received from Milla, contains the id of a Classifier
	 * @return Requirements that have the selected Classifier (List<Requirement> as a ResponseEntity), if the List is not empty, else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "/mallikas/classifiers/{id}")
	public ResponseEntity<List<Requirement>> sendRequirementsWithClassifierToMilla(@PathVariable("id") String id) {		
		System.out.println("Milla asked for all requirements in a component/classifier");
		System.out.println("ClassifierId is " + id);
		List<Requirement> reqs = reqRepository.findByClassifier(id);
		if(!reqs.isEmpty()) {
			return new ResponseEntity<List<Requirement>>(reqs, HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);	
	}
	
	/**
	 * Receives a Collection of Requirement ids (String) from Milla and sends back to Milla a List of selected Requirements. 
	 * @param ids Collection<String> received from Milla, ids of the selected Requirements
	 * @return selected Requirements (List<Requirement> as a ResponseEntity), if the List is not empty, else returns a new ResponseEntity Not Found
	 */
	@RequestMapping(value = "/mallikas/reqs/{ids}")
	public ResponseEntity<List<Requirement>> sendSelectedRequirementsToMilla(@PathVariable("ids") Collection<String> ids) {
		System.out.println("Milla asked for selected requirements again");
		System.out.println("ids is " +ids +" ids size is " + ids.size());
		List<Requirement> requestedReqs = reqRepository.findByIdIn(ids);
		System.out.println("requestedReqs " + requestedReqs.size());
		if(!requestedReqs.isEmpty()) {
			return new ResponseEntity<List<Requirement>>(requestedReqs, HttpStatus.OK);
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);	
	}

}
