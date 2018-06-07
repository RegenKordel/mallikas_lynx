package eu.openreq.mallikas.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.openreq.mallikas.models.entity.IssueObject;
import eu.openreq.mallikas.repositories.IssueRepository;
import io.swagger.annotations.ApiOperation;

@SpringBootApplication
@Controller
public class MallikasController {
	
	@Autowired
	IssueRepository issueRepository;
	
	@ApiOperation(value = "Return Hello Mallikas",
		    notes = "Just a simple test")
	@RequestMapping(value = "/test", method = RequestMethod.GET)
    public @ResponseBody String greeting() {
        return "Hello Mallikas";
    }
	
	
	/**
	 * Import issues from Milla
	 * 
	 */
	@ApiOperation(value = "Import a list of issues",
			notes = "Import a list of issues as IssueObjects")
	@RequestMapping(value = "/mallikas", method = RequestMethod.POST)
	public String importIssuesFromMilla(@RequestBody List<IssueObject> issues) {
		System.out.println("Received issues from Milla");
		List<IssueObject> allIssues = issues;
		List<IssueObject> savedIssues = new ArrayList<>();
		for(IssueObject issue : allIssues) {
			if(issueRepository.findByKey(issue.getKey())==null) {
				savedIssues.add(issue);
			}
			else {
				System.out.println("Found a duplicate " + issue.getKey());
			}
		}
		issueRepository.save(savedIssues);
		System.out.println("Issues saved " + issueRepository.count());
		allIssues.clear();
		savedIssues.clear();
		return "saved";
	}

}
