package bdn.quantum.repository;

import org.springframework.data.repository.CrudRepository;

import bdn.quantum.model.BasketEntity;

public interface BasketRepository extends CrudRepository<BasketEntity, Integer> {
	
}
