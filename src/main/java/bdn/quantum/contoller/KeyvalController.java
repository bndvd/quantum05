package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.KeyvalEntity;
import bdn.quantum.service.KeyvalService;
import bdn.quantum.service.MarketDataService;

@RestController("keyvalController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class KeyvalController {
	
	@Autowired
	KeyvalService keyvalService;
	@Autowired
	private QPlotController qPlotController;
	@Autowired
	private MarketDataService marketDataService;
	

	@RequestMapping(value = "/keyval", method = RequestMethod.GET)
	public Iterable<KeyvalEntity> getKeyvals() {
		return keyvalService.getKeyvals();
	}
	
	@RequestMapping(value = "/keyval/{key}", method = RequestMethod.GET)
	public KeyvalEntity getKeyval(@PathVariable(value="key") String key) {
		//System.out.println("KeyvalController.getKeyval: key=" + key);
		return keyvalService.getKeyval(key);
	}

	@RequestMapping(value = "/keyval", method = RequestMethod.POST)
	public KeyvalEntity createKeyval(@RequestBody KeyvalEntity ke) {
		//System.out.println("KeyvalEntityController.createKeyval: key=" + ke.getKey());
		return keyvalService.setKeyval(ke);
	}

	@RequestMapping(value = "/keyval/{key}", method = RequestMethod.DELETE)
	public void deleteKeyval(@PathVariable(value="key") String key) {
		//System.out.println("KeyvalEntityController.deleteKeyval: key=" + key);
		keyvalService.deleteKeyval(key);
	}
	
	public void keyvalChange() {
		qPlotController.plotDataChanged();
		marketDataService.configChanged();
	}
	
}
