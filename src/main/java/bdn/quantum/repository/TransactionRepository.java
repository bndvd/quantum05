package bdn.quantum.repository;

import java.util.Date;

import org.springframework.data.repository.CrudRepository;

import bdn.quantum.model.TranEntity;

public interface TransactionRepository extends CrudRepository<TranEntity, Integer> {

	Iterable<TranEntity> findBySecId(Integer secId);
	long countByTranDate(Date tranDate);
	
}
