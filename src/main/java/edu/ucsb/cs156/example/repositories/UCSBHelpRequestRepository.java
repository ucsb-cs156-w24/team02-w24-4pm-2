package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBHelpRequest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UCSBHelpRequestRepository extends CrudRepository<UCSBHelpRequest, Long> {
}
