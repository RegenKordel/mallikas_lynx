package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, String> {
	
	Requirement findById(String id);
	
	List<Requirement> findByIdIn(Collection<String> ids);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.projectId = ?1)")
	List<Requirement> findByProjectId(String projectId);

	@Query("SELECT part.requirement FROM RequirementPart part WHERE part.text = ?1")
	List<Requirement> findByRequirementPartText(String text);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE ((?1) is null OR req.id IN (?1)) AND (?2 is null OR "
			+ "req.created_at >= ?2) AND (?3 is null OR req.modified_at >= ?3) AND (?4 is null OR "
			+ "req.requirement_type = ?4) AND (?5 is null OR req.status = ?5)")
	List<Requirement> findByParams(Collection<String> ids, Long created_at, Long modified_at, Requirement_type type, 
			Requirement_status status);
}