package bdn.quantum.repository;

import org.springframework.data.repository.CrudRepository;

import bdn.quantum.model.MarketStatusEntity;

public interface MarketStatusRepository extends CrudRepository<MarketStatusEntity, String> {

	Iterable<MarketStatusEntity> findAllByOrderByMktDateAsc();

}
