package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;

public interface RequirementRepository extends JpaRepository<Requirement, String> {
	
	Requirement findById(String id);
	
	List<Requirement> findByIdIn(Collection<String> ids);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.requirement_type = ?1")
	List<Requirement> findByType(Requirement_type type);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.status = ?1")
	List<Requirement> findByStatus(Requirement_status status);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.created_at >= ?1")
	List<Requirement> findCreatedSinceDate(Long created_at);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.requirement_type = ?1 AND req.status = ?2")
	List<Requirement> findByTypeAndStatus(Requirement_type type, Requirement_status status);
	
	@Query("SELECT DISTINCT req FROM Requirement req, IN (req.requirementParts) AS reqPart WHERE reqPart.text = ?1")
	List<Requirement> findByRequirementPart(String resolutionValue);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE (?1 is null OR req.id in ?1) AND (?2 is null OR "
			+ "req.created_at >= ?2) AND (?3 is null OR req.modified_at >= ?3) AND (?4 is null OR "
			+ "req.requirement_type = ?4) AND (?5 is null OR req.status = ?5)")
	List<Requirement> findByParams(Collection<String> ids, Long created_at, Long modified_at, Requirement_type type, 
			Requirement_status status);
}
