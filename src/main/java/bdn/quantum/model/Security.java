package bdn.quantum.model;

//
// Security is a rough representation of SecurityEntity
//
public class Security {

	private Integer id;
	private Integer basketId;
	private String symbol;
	
	
	public Security() {
	}
	
	public Security(Integer id, Integer basketId, String symbol) {
		this.id = id;
		this.basketId = basketId;
		this.symbol = symbol;
	}
	
	public Security(SecurityEntity se) {
		this(se.getId(), se.getBasketId(), se.getSymbol());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBasketId() {
		return basketId;
	}

	public void setBasketId(Integer basketId) {
		this.basketId = basketId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


}
