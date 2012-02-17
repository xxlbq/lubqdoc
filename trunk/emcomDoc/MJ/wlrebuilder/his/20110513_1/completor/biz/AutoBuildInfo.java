package jp.emcom.adv.fx.completor.biz;

import java.math.BigDecimal;

public class AutoBuildInfo {
	
	private String clientId;
	private String customerId;
	private BigDecimal positionAutoRebuildConstraint ;
	private BigDecimal positionAutoRebuildEndInterval ;
	private BigDecimal tradableEndInterval ;

	public AutoBuildInfo(String clientId,
			String customerId,
			BigDecimal positionAutoRebuildConstraint,
			BigDecimal positionAutoRebuildEndInterval,
			BigDecimal tradableEndInterval) {
		super();
		this.clientId = clientId;
		this.customerId = customerId;
		this.positionAutoRebuildConstraint = positionAutoRebuildConstraint;
		this.positionAutoRebuildEndInterval = positionAutoRebuildEndInterval;
		this.tradableEndInterval = tradableEndInterval;
	}
	
	public BigDecimal getTradableEndInterval() {
		return tradableEndInterval;
	}
	public void setTradableEndInterval(BigDecimal tradableEndInterval) {
		this.tradableEndInterval = tradableEndInterval;
	}
	public BigDecimal getPositionAutoRebuildEndInterval() {
		return positionAutoRebuildEndInterval;
	}
	public void setPositionAutoRebuildEndInterval(
			BigDecimal positionAutoRebuildEndInterval) {
		this.positionAutoRebuildEndInterval = positionAutoRebuildEndInterval;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getPositionAutoRebuildConstraint() {
		return positionAutoRebuildConstraint;
	}
	public void setPositionAutoRebuildConstraint(
			BigDecimal positionAutoRebuildConstraint) {
		this.positionAutoRebuildConstraint = positionAutoRebuildConstraint;
	}
	
	
}
