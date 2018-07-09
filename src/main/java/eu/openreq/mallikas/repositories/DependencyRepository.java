package eu.openreq.mallikas.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.openreq.mallikas.models.json.Dependency;

public interface DependencyRepository extends JpaRepository<Dependency, String> {

	Dependency findById(String id);
	List<Dependency> findByFromId(String fromId);
	List<Dependency> findByToId(String toId);
	
	List<Dependency> findByFromIdIn(Collection<String> ids);
	List<Dependency> findByIdIn(Collection<String> ids);
}
