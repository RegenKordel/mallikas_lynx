package eu.openreq.mallikas.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import eu.openreq.mallikas.models.json.Comment;
import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_status;
import eu.openreq.mallikas.models.json.Dependency_type;
import eu.openreq.mallikas.models.json.Person;
import eu.openreq.mallikas.models.json.Project;
import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.RequirementPart;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.PersonRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;

@Service
public class UpdateService {

	@Autowired
	RequirementRepository requirementRepository;

	@Autowired
	DependencyRepository dependencyRepository;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	PersonRepository personRepository;
	
	public ResponseEntity<String> importRequirements(Collection<Requirement> requirements) {
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
	
	public ResponseEntity<String> importDependencies(@RequestBody Collection<Dependency> dependencies) {
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
	
	public ResponseEntity<String> importProject(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project.getId());
		if (projectRepository.findOne(project.getId()) != null) {
			System.out.println("Found a duplicate " + project.getId());
		} 
		projectRepository.save(project);	
		
		System.out.println("Project saved " + projectRepository.count());
		return new ResponseEntity<>("Project saved", HttpStatus.OK);
	}
	
	public ResponseEntity<String> updateDependencies(Collection<Dependency> dependencies, 
			boolean userInput, boolean isProposed) {
		try {
			if (userInput) {
				updateDependenciesWithUserInput(dependencies);
			} else if (isProposed) {
				saveProposedDependencies(dependencies);
			} else {
				dependencyRepository.save(dependencies);
			}

			System.out.println("Dependencies saved " + dependencyRepository.count());
			return new ResponseEntity<>("Dependencies saved in Mallikas", HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Mallikas update failed", HttpStatus.BAD_REQUEST);
		}
	}
	
	public ResponseEntity<String> updateRequirements(Collection<Requirement> requirements, 
			String projectId) {
		List<Requirement> savedRequirements = new ArrayList<>();
		List<Person> savedPersons = new ArrayList<>();		
		try {
			for (Requirement requirement : requirements) {		
				Set<RequirementPart> reqParts = requirement.getRequirementParts();
				
				if (reqParts!=null) {
					for (RequirementPart part : reqParts) {
						part.setRequirement(requirement);
					}
					requirement.setRequirementParts(reqParts);
				}
				Set<Comment> comments = requirement.getComments();
				
				if (comments!=null) {
					for (Comment comment : comments) {
						comment.setRequirement(requirement);
						Person person = comment.getCommentDoneBy();
						savedPersons.add(person);
					}
					requirement.setComments(comments);
				}
				
				if (requirementRepository.findById(requirement.getId()) == null) {
					savedRequirements.add(requirement);
				} else if (requirement.getModified_at() > requirementRepository.findById(requirement.getId())
						.getModified_at()) {
					savedRequirements.add(requirement);
				}
				requirement.setProjectId(projectId);
			}
			personRepository.save(savedPersons);
			requirementRepository.save(savedRequirements);
			System.out.println("Requirements saved " + requirementRepository.count());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
	}
	
	public ResponseEntity<String> updateProjectSpecifiedRequirements(Map<String, Collection<String>> 
			reqIds, String projectId) {
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
			System.out.println(e.getMessage());
		}

		return new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
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
				depId = dep.getFromid() + "_" + dep.getToid(); 
			}
			Dependency originalDependency = dependencyRepository.findById(depId);
			if (originalDependency!=null) {
				Set<String> descriptions = originalDependency.getDescription();
				if (dep.getDescription()!=null) {
					String newDescription = dep.getDescription().iterator().next();
					if (!descriptions.contains(newDescription)) {
						descriptions.add(newDescription);
						originalDependency.setDescription(descriptions);
						dependencyRepository.save(originalDependency);
					}
				}
			} else {
				dep.setId(depId);
				dependencyRepository.save(dep);
			}
		}
		
	}
	
	/**
	 * Updates dependency status and type as determined by user input
	 * 
	 * @param dependencies
	 */
	private void updateDependenciesWithUserInput(Collection<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			String depId = dep.getFromid() + "_" + dep.getToid();
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
	
	public ResponseEntity<String> deleteEverythingButRejectedDependencies() {
		projectRepository.deleteAll();
		requirementRepository.deleteAll();
		dependencyRepository.deleteAllNotRejected();

		return new ResponseEntity<>("Delete successful", HttpStatus.OK);
	}	
	
}
