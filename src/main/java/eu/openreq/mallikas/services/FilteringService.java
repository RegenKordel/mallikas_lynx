package eu.openreq.mallikas.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.closedreq.bridge.models.json.*;
import eu.openreq.mallikas.repositories.DependencyRepository;
import eu.openreq.mallikas.repositories.PersonRepository;
import eu.openreq.mallikas.repositories.ProjectRepository;
import eu.openreq.mallikas.repositories.RequirementRepository;

@Service
public class FilteringService {
	
	@Autowired
	RequirementRepository requirementRepository;

	@Autowired
	DependencyRepository dependencyRepository;

	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	PersonRepository personRepository;
	
	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	
	public ResponseEntity<Map<String, Integer>> listOfProjects() {
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

	public ResponseEntity<String> allRequirements() {
		List<Requirement> allReqs = requirementRepository.findAll();
		List<Dependency> allDeps = dependencyRepository.findAll();
		if (!allReqs.isEmpty()) {
			try {
				String reqsString = gson.toJson(allReqs);
				String dependencyString = gson.toJson(allDeps);
				String all = "{ \"requirements\":" + reqsString + ", \"dependencies\":" + dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<String> allDependencies() {
		List<Dependency> dependencies = dependencyRepository.findAll();
		if (!dependencies.isEmpty()) {
			try {
				String dependencyString = gson.toJson(dependencies);
				String all = "{\"dependencies\":" + dependencyString + "}";
				return new ResponseEntity<String>(all, HttpStatus.OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<String> requirementsByParams(RequestParams params) {
		List<Project> projects = null;
		List<String> reqIds = new ArrayList<String>();
				
		if (params.getRequirementIds()!=null) {
			reqIds.addAll(params.getRequirementIds());
		}
		
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
		String type = params.getType();
		String status = params.getStatus();
		
		if (params.getCreated_at()!=null) {
			created = params.getCreated_at().getTime();
		}
		if (params.getModified_at()!=null) {
			modified = params.getModified_at().getTime();
		}
		
		List<Requirement> selectedReqs = new ArrayList<>();
		
		List<List<String>> splitReqIds = splitRequirementIds(reqIds);
		
		for (List<String> splitIds : splitReqIds) {
			selectedReqs.addAll(requirementRepository.findByParams(splitIds, created, modified, type, status));
		}
		
		if (params.getResolution()!=null) {
			List<Requirement> resolutionReqs = requirementRepository.findByRequirementPartText(params.getResolution());
			if (!selectedReqs.isEmpty()) {
				selectedReqs.retainAll(resolutionReqs);
			} else {
				selectedReqs = resolutionReqs;
			}
		}
		
		if (selectedReqs != null && !selectedReqs.isEmpty()) {
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
				dependencies.addAll(dependencyRepository.findByRequirementIdWithParams(splitIds, params.getScoreThreshold(),
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
				System.out.println(e.getMessage());
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<String> dependenciesByParams(RequestParams params) {
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
				dependencies.addAll(dependencyRepository.findByRequirementIdWithParams(splitIds, params.getScoreThreshold(),
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
				System.out.println(e.getMessage());
			}
		
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<String> requirementsInProject(String projectId, 
			boolean includeProposed, boolean requirementsOnly) {
		Project project = projectRepository.findById(projectId);

		if (project != null) {
			List<Project> projects = new ArrayList<>();
			projects.add(project);
			
			Set<String> requirementIds = project.getSpecifiedRequirements();
			List<Requirement> requirements = new ArrayList<Requirement>();
			List<Dependency> dependencies = new ArrayList<Dependency>();	
			
			if (requirementsOnly) {
				requirements = requirementRepository.findByProjectId(projectId);
			}
			else if (includeProposed) {
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
					System.out.println(e.getMessage());
				}
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	
	public ResponseEntity<String> selectedRequirements(List<String> ids) {
		List<Requirement> selectedReqs = requirementRepository.findByIdIn(ids);
		if (!selectedReqs.isEmpty() && selectedReqs != null) {
			List<Dependency> dependencies = new ArrayList<Dependency>();
			List<List<String>> splitReqIds = splitRequirementIds(ids);
			for (List<String> splitIds : splitReqIds) {
				dependencies.addAll(dependencyRepository.findByRequirementIdIncludeProposed(splitIds));
			}
			try {
				return new ResponseEntity<String>(createJsonString(null, null, selectedReqs, dependencies),
						HttpStatus.OK);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * Corrects the dependency from/to IDs if necessary
	 * @param dependencies
	 * @return
	 */
	public ResponseEntity<String> correctIdsForDependencies(List<Dependency> dependencies) {
		List<Dependency> correctDependencies = new ArrayList<Dependency>();
		for (Dependency dep : dependencies) {
			String reverse = dep.getToid() + "_" + dep.getFromid();
			if (dependencyRepository.findById(reverse)!=null) {
				String tempFrom = dep.getFromid();
				dep.setFromid(dep.getToid());
				dep.setToid(tempFrom);
			}
			dep.setId(dep.getFromid() + "_" + dep.getToid());
			correctDependencies.add(dep);
		}
		return new ResponseEntity<String>(gson.toJson(correctDependencies), HttpStatus.OK);
	}
	
	/**
	 * Returns the dependencies in a HashMap labeled under their respective projects
	 * @param dependencies
	 * @return
	 */
	public ResponseEntity<String> projectsForDependencies(List<Dependency> dependencies) {
		Map<String, List<Dependency>> depMap = new HashMap<>();
		for (Dependency dep : dependencies) {
			Requirement fromReq = requirementRepository.findById(dep.getFromid());
			Requirement toReq = requirementRepository.findById(dep.getToid());
			if (fromReq!=null) {
				depMap.computeIfAbsent(fromReq.getProjectId(), k -> new ArrayList<>()).add(dep);
			}
			if (toReq!=null) {
				depMap.computeIfAbsent(toReq.getProjectId(), k -> new ArrayList<>()).add(dep);
			}
		}
		return new ResponseEntity<String>(gson.toJson(depMap), HttpStatus.OK);
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
	public String createJsonString(Project project, Requirement requirement, List<Requirement> requirements,
			List<Dependency> dependencies) throws JsonProcessingException {
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
		String dependencyString = gson.toJson(dependencies);
		String reqsString = gson.toJson(requirements);
		String projectsString = gson.toJson(projects);
		String jsonString = "{ \"projects\":" + projectsString + ", \"requirements\":" + reqsString
				+ ", \"dependencies\":" + dependencyString + "}";
		return jsonString;
	}
	
	/**
	 * Splits the id lists to avoid the the Postgres query parameter amount limit
	 * 
	 * @param requirementIds
	 * @return
	 */
	public List<List<String>> splitRequirementIds(List<String> requirementIds) {
		if ((requirementIds==null) || (requirementIds.size() <= 10000)) {
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
