package bdn.quantum.repository;

import org.springframework.data.repository.CrudRepository;

import bdn.quantum.model.SecurityEntity;

public interface SecurityRepository extends CrudRepository<SecurityEntity, Integer> {

	Iterable<SecurityEntity> findByBasketId(Integer basketId);
	
}
