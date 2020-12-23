package bdn.quantum.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import bdn.quantum.model.MarketQuoteEntity;

public interface MarketQuoteRepository extends CrudRepository<MarketQuoteEntity, Long> {

	List<MarketQuoteEntity> findBySymbolOrderByMktDateAsc(String symbol);
	List<MarketQuoteEntity> findBySymbolAndMktDateIsGreaterThanOrderByMktDateAsc(String symbol, String mktDate);
	List<MarketQuoteEntity> findBySymbolAndMktDate(String symbol, String mktDate);
	Boolean existsBySymbolAndMktDate(String symbol, String mktDate);
	
}
