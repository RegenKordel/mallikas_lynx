package eu.openreq.mallikas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.openreq.mallikas.models.json.Project;

public interface ProjectRepository extends JpaRepository<Project, String>{

	Project findById(String id);
}
