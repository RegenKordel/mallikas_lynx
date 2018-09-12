package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

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
}
