package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import eu.openreq.mallikas.models.json.Dependency;

public interface DependencyRepository extends JpaRepository<Dependency, String> {

	Dependency findById(String id);
	
	List<Dependency> findByIdIn(Collection<String> ids);
	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE (dep.fromid IN (SELECT s FROM Project proj "
			+ "INNER JOIN proj.specifiedRequirements s WHERE proj.id = ?1) OR dep.toid IN "
			+ "(SELECT s FROM Project proj INNER JOIN proj.specifiedRequirements s "
			+ "WHERE proj.id = ?1)) AND (dep.status != 2)")
	List<Dependency> findByProjectIdIncludeProposed(String projectId);
	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE (dep.fromid IN (SELECT s FROM Project proj "
			+ "INNER JOIN proj.specifiedRequirements s WHERE proj.id = ?1) OR dep.toid IN "
			+ "(SELECT s FROM Project proj INNER JOIN proj.specifiedRequirements s "
			+ "WHERE proj.id = ?1)) AND (dep.status = 1)")
	List<Dependency> findByProjectIdExcludeProposed(String projectId);

	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE (dep.fromid IN (SELECT s FROM Project proj "
			+ "INNER JOIN proj.specifiedRequirements s WHERE proj.id = ?1) OR dep.toid IN "
			+ "(SELECT s FROM Project proj INNER JOIN proj.specifiedRequirements s "
			+ "WHERE proj.id = ?1)) AND (dep.status != 2)")
	List<Dependency> findByIdIncludeProposed(Collection<String> ids);
	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE (dep.fromid IN (SELECT s FROM Project proj "
			+ "INNER JOIN proj.specifiedRequirements s WHERE proj.id = ?1) OR dep.toid IN "
			+ "(SELECT s FROM Project proj INNER JOIN proj.specifiedRequirements s "
			+ "WHERE proj.id = ?1)) AND (dep.status = 1)")
	List<Dependency> findByIdExcludeProposed(Collection<String> ids);

	
	@Query("SELECT DISTINCT dep FROM Dependency dep WHERE ((dep.fromid IN (?1)) OR (dep.toid IN (?1))) "
			+ "AND (?2 is null OR dep.dependency_score >= ?2) "
			+ "AND ((?3 is null OR ?3 is TRUE) OR (?3 is FALSE AND dep.status != 0)) "
			+ "AND ((?4 is null or ?4 is FALSE) OR (?4 is TRUE AND dep.status = 0)) "
			+ "AND (((?5 is null or ?5 is FALSE) AND (dep.status != 2)) OR (?5 is TRUE))"
			+ "ORDER BY dep.dependency_score DESC")
	List<Dependency> findByIdWithParams(Collection<String> ids, Double scoreTreshold, Boolean includeProposed, Boolean proposedOnly, 
			Boolean includeRejected, Pageable pageable);
	
	@Modifying
	@Query("DELETE FROM Dependency WHERE status != 2")
	void deleteAllNotRejected();
	
	//SELECT DISTINCT dep FROM Dependency dep WHERE (dep.fromid IN (SELECT proj.specified_requirements FROM Project_specified_requirements proj WHERE proj.project_id = ?1) OR dep.toid IN (SELECT proj.specified_requirements FROM Project_specified_requirements proj WHERE proj.project_id = ?1)) AND (dep.status != 2);"
}

