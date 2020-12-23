package bdn.quantum.service;

import java.util.List;

import bdn.quantum.model.Asset;
import bdn.quantum.model.BasketEntity;
import bdn.quantum.model.Position;
import bdn.quantum.model.Security;

public interface AssetService {

	public BasketEntity getBasket(Integer id);
	public Iterable<BasketEntity> getBaskets();
	public BasketEntity createBasket(BasketEntity basket);
	public Security getSecurity(Integer id);
	public Iterable<Security> getSecurities();
	public Iterable<Security> getSecuritiesInBasket(Integer basketId);
	public Security createSecurity(Security security);
	public Asset getAsset(Integer basketId);
	public Iterable<Asset> getAssets();
	public Asset createAsset(Asset asset);
	public Position getPosition(Integer secId);
	public List<Position> getPositions();
	public List<Position> getPositions(Integer basketId);
	public List<Position> getPositions(boolean includeTransactions);

}
