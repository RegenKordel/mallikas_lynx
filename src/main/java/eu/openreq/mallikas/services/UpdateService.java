package eu.openreq.mallikas.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.closedreq.bridge.models.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
		System.out.println(requirementRepository.count() + " requirements total");
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
		System.out.println(dependencyRepository.count() + " dependencies total");
		savedDependencies.clear();
		return new ResponseEntity<>("Dependencies saved", HttpStatus.OK);
	}
	
	public ResponseEntity<String> importProject(@RequestBody Project project) {
		System.out.println("Received a project from Milla " + project.getId());
		if (projectRepository.findOne(project.getId()) != null) {
			System.out.println("Found a duplicate " + project.getId());
		} 
		projectRepository.save(project);	
		
		System.out.println(projectRepository.count() + " projects total");
		return new ResponseEntity<>("Project saved", HttpStatus.OK);
	}
	
	public ResponseEntity<String> updateDependencies(Collection<Dependency> dependencies, 
			boolean userInput, boolean isProposed) {
		Long originalCount = dependencyRepository.count();
		
		try {
			if (userInput) {
				updateUserInputDependencies(dependencies);
			} else if (isProposed) {
				updateProposedDependencies(dependencies);
			} else {
				dependencyRepository.save(dependencies);
			}


			Long newCount = dependencyRepository.count() - originalCount;
			
			String saved = newCount + " dependencies added, " + dependencyRepository.count() + " dependencies total";
			System.out.println(saved);
			return new ResponseEntity<>(saved, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>("Updating dependencies failed", HttpStatus.BAD_REQUEST);
		}
	}
	
	public ResponseEntity<String> updateRequirements(Collection<Requirement> requirements, 
			String projectId) {
		List<Requirement> savedRequirements = new ArrayList<>();
		List<Person> savedPersons = new ArrayList<>();		
		Long originalCount = requirementRepository.count();
		
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
			
			Long newCount = requirementRepository.count() - originalCount;
			
			String saved = newCount + " requirements added, " + requirementRepository.count() + " requirements total";;
			System.out.println(saved);
			return new ResponseEntity<>(saved, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return new ResponseEntity<>("Updating requirements failed", HttpStatus.BAD_REQUEST);
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
	 * Save dependencies received from similarity detection services and such
	 * 
	 * @param dep
	 */
	private void saveDependency(Dependency dep) {
		String depId = dep.getFromid() + "_" + dep.getToid(); 
		String reverseId = dep.getToid() + "_" + dep.getFromid(); 
		if (dependencyRepository.findById(reverseId)!=null) {
			depId = reverseId;
		}
		dep.setId(depId);
		
		Dependency originalDep = dependencyRepository.findById(depId);
		if (originalDep != null) {
			if (originalDep.getStatus()==Dependency_status.PROPOSED 
					|| dep.getStatus()==Dependency_status.ACCEPTED 
					|| dep.getStatus()==Dependency_status.REJECTED) {
				dependencyRepository.save(dep);
			} 
		} else {
			dependencyRepository.save(dep);
		}
		
	}
	
	/**
	 * Updates dependency as determined by user input, no proposed dependencies
	 * 
	 * @param dependencies
	 */
	private void updateUserInputDependencies(Collection<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			if (dep.getStatus()!=Dependency_status.PROPOSED) {
				saveDependency(dep);			
			}
		}
	}
	
	/**
	 * Update only proposed dependencies
	 * 
	 * @param dependencies
	 */
	private void updateProposedDependencies(Collection<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			if (dep.getStatus()==Dependency_status.PROPOSED) {
				saveDependency(dep);			
			}
		}
	}
	
	public ResponseEntity<String> deleteEverything(Boolean keepRejected) {
		projectRepository.deleteAll();
		requirementRepository.deleteAll();
		if (keepRejected) {
			dependencyRepository.deleteAllNotRejected();
		} else {
			dependencyRepository.deleteAll();
		}
		personRepository.deleteAll();

		return new ResponseEntity<>("Delete successful", HttpStatus.OK);
	}	
	
}
