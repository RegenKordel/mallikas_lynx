package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.openreq.mallikas.models.json.Dependency;
import eu.openreq.mallikas.models.json.Dependency_type;

public interface DependencyRepository extends JpaRepository<Dependency, String> {

	Dependency findById(String id);
	List<Dependency> findByFromid(String fromId);
	List<Dependency> findByToid(String toId);
	
	List<Dependency> findByFromidIn(Collection<String> ids);
	List<Dependency> findByToidIn(Collection<String> ids);
	List<Dependency> findByIdIn(Collection<String> ids);
	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE dep.dependency_type = ?1")
	List<Dependency> findByType(Dependency_type type);
	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE ((dep.fromid IN (?1)) OR (dep.toid IN (?1))) "
			+ "AND (?2 is null OR dep.dependency_score >= ?2) "
			+ "AND ((?3 is null OR ?3 is TRUE) OR (?3 is FALSE AND dep.status != 0)) "
			+ "AND ((?4 is null or ?4 is FALSE) OR (?4 is TRUE AND dep.status = 0)) "
			+ "ORDER BY dep.dependency_score DESC")
	List<Dependency> findByIdsWithParams(Collection<String> ids, Double treshold, Boolean includeProposed, Boolean proposedOnly, 
			Pageable pageable);
	
}

