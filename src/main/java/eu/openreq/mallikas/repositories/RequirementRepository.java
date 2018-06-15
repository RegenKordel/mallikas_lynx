package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.openreq.mallikas.models.json.Requirement;

public interface RequirementRepository extends JpaRepository<Requirement, String> {
	
	Requirement findById(String id);

	@Query("SELECT DISTINCT req FROM Requirement req, IN (req.classifierResults) AS c WHERE c.id = ?1")
	List<Requirement> findByClassifier(String id);
	
	List<Requirement> findByIdIn(Collection<String> ids);
}
