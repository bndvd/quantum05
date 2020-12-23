package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.PortfolioData;
import bdn.quantum.service.PortfolioService;
import bdn.quantum.util.ServiceError;

@RestController("portfolioController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class PortfolioController {
	
	@Autowired
	private PortfolioService portfolioService;
	
	@RequestMapping(value = "/portfolioData", method = RequestMethod.GET)
	public PortfolioData getPortfolio() {
		return portfolioService.getPortfolioData();
	}
	
	@RequestMapping(value = "/portfolioData", method = RequestMethod.POST)
	public PortfolioData insertPortfolioData(@RequestBody PortfolioData portfolioData) {
		return portfolioService.insertPortfolioData(portfolioData);
	}
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ServiceError> handle(RuntimeException exc) {
		ServiceError error = new ServiceError(HttpStatus.OK.value(), exc.getMessage());
		return new ResponseEntity<>(error, HttpStatus.OK);
	}


}
