package bdn.quantum.model;

import java.math.BigDecimal;
import java.util.Date;

public interface AbstractTransaction {

	public Date getTranDate();
	public String getType();
	public BigDecimal getShares();
	public BigDecimal getPrice();
	public BigDecimal getPrincipalDelta();

}
