package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.openreq.mallikas.models.json.Requirement;
import eu.openreq.mallikas.models.json.Requirement_status;
import eu.openreq.mallikas.models.json.Requirement_type;

public interface RequirementRepository extends JpaRepository<Requirement, String> {
	
	Requirement findById(String id);

	@Query("SELECT DISTINCT req FROM Requirement req, IN (req.classifierResults) AS c WHERE c.id = ?1")
	List<Requirement> findByClassifier(String id);
	
	List<Requirement> findByIdIn(Collection<String> ids);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.requirement_type = ?1")
	List<Requirement> findByType(Requirement_type type);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.status = ?1")
	List<Requirement> findByStatus(Requirement_status status);
	
	@Query("SELECT DISTINCT req FROM Requirement req WHERE req.requirement_type = ?1 AND req.status = ?2")
	List<Requirement> findByTypeAndStatus(Requirement_type type, Requirement_status status);
	
	@Query("SELECT DISTINCT req FROM Requirement req, IN (req.requirementParts) AS reqPart WHERE reqPart.text = ?1")
	List<Requirement> findByRequirementPart(String resolutionValue);
}
