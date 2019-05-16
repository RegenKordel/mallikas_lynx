package eu.openreq.mallikas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.openreq.mallikas.models.json.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String>{

	Project findById(String id);
}
