package bdn.quantum.service;

import bdn.quantum.model.PortfolioData;

public interface PortfolioService {
	
	public PortfolioData getPortfolioData();

	public PortfolioData insertPortfolioData(PortfolioData portfolioData);
	
}
