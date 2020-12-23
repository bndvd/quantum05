package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.Asset;
import bdn.quantum.model.Position;
import bdn.quantum.model.Security;
import bdn.quantum.service.AssetService;
import bdn.quantum.util.ServiceError;

@RestController("assetController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class AssetController {

	@Autowired
	private AssetService assetService;
	
	@RequestMapping(value = "/securities", method = RequestMethod.GET)
	public Iterable<Security> getSecurities() {
		return assetService.getSecurities();
	}
	
	@RequestMapping(value = "/securities/{basketId}", method = RequestMethod.GET)
	public Iterable<Security> getSecurities(@PathVariable(value="basketId") Integer basketId) {
		return assetService.getSecuritiesInBasket(basketId);
	}
	
	@RequestMapping(value = "/security", method = RequestMethod.POST)
	public Security createSecurity(@RequestBody Security security) {
		return assetService.createSecurity(security);
	}
	
	@RequestMapping(value = "/asset/{basketId}", method = RequestMethod.GET)
	public Asset getAsset(@PathVariable(value="basketId") Integer basketId) {
		return assetService.getAsset(basketId);
	}
	
	@RequestMapping(value = "/assets", method = RequestMethod.GET)
	public Iterable<Asset> getAssets() {
		return assetService.getAssets();
	}
	
	@RequestMapping(value = "/asset", method = RequestMethod.POST)
	public Asset createAsset(@RequestBody Asset asset) {
		return assetService.createAsset(asset);
	}
	
	@RequestMapping(value = "/position/{secId}", method = RequestMethod.GET)
	public Position getPosition(@PathVariable(value="secId") Integer secId) {
		return assetService.getPosition(secId);
	}
	
	@RequestMapping(value = "/positions", method = RequestMethod.GET)
	public Iterable<Position> getPositions() {
		return assetService.getPositions();
	}
	
	@RequestMapping(value = "/positions/{basketId}", method = RequestMethod.GET)
	public Iterable<Position> getPositions(@PathVariable(value="basketId") Integer basketId) {
		return assetService.getPositions(basketId);
	}
	
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ServiceError> handle(RuntimeException exc) {
		ServiceError error = new ServiceError(HttpStatus.OK.value(), exc.getMessage());
		return new ResponseEntity<>(error, HttpStatus.OK);
	}

}
