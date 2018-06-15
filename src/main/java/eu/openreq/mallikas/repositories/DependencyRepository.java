package eu.openreq.mallikas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.openreq.mallikas.models.json.Dependency;

public interface DependencyRepository extends JpaRepository<Dependency, String> {

	Dependency findById(String id);
	Dependency findByFromId(String fromId);
	Dependency findByToId(String toId);
}
