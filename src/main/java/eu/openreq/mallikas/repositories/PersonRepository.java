package eu.openreq.mallikas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.closedreq.bridge.models.json.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, String>{

}
