package eu.openreq.mallikas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.openreq.mallikas.models.json.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, String>{

}
