package jp.emcom.adv.fx.completor.biz.executor.impl;

import java.math.BigDecimal;
import java.util.List;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.commons.bo.enums.OrderStatusEnum;
import jp.emcom.adv.fx.completor.service.WLNetterService;

import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.bo.enums.FormulaDirtyFlagEnum;
import cn.bestwiz.jhf.core.dao.LockHelper;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.service.ServiceFactory;

/**
 * 
 *
 */
public class WLNetter extends AbstractLifecycle {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(WLNetter.class);
	
	//
	private WLNetterService wlNetterService;

	/**
	 * 
	 */
	public WLNetter() {
	}

	@Override
	protected void doStart() throws Exception {
		// NOP
	}

	@Override
	protected void doStop(long timeout) throws Exception {
		// NOP
	}

	/**
	 * 
	 */
	public WLNetterService getWlNetterService() {
		return wlNetterService;
	}

	public void setWlNetterService(WLNetterService wlNetterService) {
		this.wlNetterService = wlNetterService;
	}
	
	/**
	 * 
	 */
	public boolean net(JhfWlOrder order) {
		boolean succeed = false;
		final long now = System.currentTimeMillis();
		try {
			//
			LOGGER.info("start to validate order, customerId: {}, currencyPair: {}, wlOrderId: {}, orderAmount: {}, completionAmount: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getOrderAmount(), order.getCompletionAmount()});
			final List<JhfAliveContract> contracts = this.wlNetterService.findNotSettledContracts(order.getCustomerId(), order.getCurrencyPair());
			if(!validate(order, contracts)) {
				return false;
			}
			
			//
			BigDecimal amount = order.getOrderAmount().subtract(order.getCompletionAmount());
			for(JhfAliveContract contract : contracts) {
				//
				if(amount.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}
				if(order.getSide().intValue() == contract.getSide().intValue()) {
					continue;
				}
				
				// Close
				if(amount.compareTo(contract.getAmountNoSettled()) > 0) {
					final BigDecimal netAmount = contract.getAmountNoSettled();
					amount = amount.subtract(netAmount);
					order.setCompletionStatus(CompletionStatusEnum.PROGRESSING.getValueAsBigDecimal());
					netClose(netAmount, order, contract);
				} else {
					final BigDecimal netAmount = amount;
					amount = amount.subtract(netAmount);
					order.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal());
					netClose(netAmount, order, contract);
					break;
				}
			}
			
			// Open
			if(amount.compareTo(BigDecimal.ZERO) > 0) {
				final BigDecimal netAmount = amount;
				order.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal());
				netOpen(netAmount, order);
			}
			
			//
			succeed = true;
			final long delay = System.currentTimeMillis() - now;
			LOGGER.info("netted an order, customerId: {}, currencyPair: {}, wlOrderId: {}, delay: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), delay});
		} catch(StaleObjectStateException e) {
			LOGGER.error("failed to net order[version mismatch], customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
			// NOP 
		} catch(Exception e) {
			LOGGER.error("failed to net order, customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
			order.setCompletionStatus(CompletionStatusEnum.FAILED.getValueAsBigDecimal());
			this.wlNetterService.updateQuietly(order);
		}
		
		//
		if(order.getOrderAmount().compareTo(order.getCompletionAmount()) != 0) {
			LOGGER.error("assertion failed, customerId: {}, currencyPair: {}, wlOrderId: {}, orderAmount: {}, completionAmount: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getOrderAmount(), order.getCompletionAmount()});
		}
		return succeed;
	}

	/**
	 * 
	 */
	private boolean validate(JhfWlOrder order, List<JhfAliveContract> contracts)
	throws Exception {
		//
		if(!order.getOrderStatus().equals(OrderStatusEnum.FILLED.getValue())) {
			LOGGER.error("invalid order status, customerId: {}, currencyPair: {}, wlOrderId: {}, orderStatus: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getOrderStatus()});
			return false;
		} 

		//
		final String frontDate = ServiceFactory.getCoreService().getFrontDate();
		if(!order.getOrderDate().equals(frontDate)) {
			LOGGER.error("invalid order date, customerId: {}, currencyPair: {}, wlOrderId: {}, orderDate: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getOrderDate()});
			return false;
		} 
		
		//
		if(order.getCompletionAmount() == null) {
			order.setCompletionAmount(BigDecimal.ZERO);
		}
		if(order.getCompletionAmount().compareTo(order.getOrderAmount()) >= 0) {
			LOGGER.error("invalid completion amount, customerId: {}, currencyPair: {}, wlOrderId: {}, orderAmount: {}, completionAmount: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getOrderAmount(), order.getCompletionAmount()});
			return false;
		}
		
		//
		BigDecimal side = null;
		for(JhfAliveContract contract : contracts) {
			if(side == null) {
				side = contract.getSide();
			} else if(side.compareTo(contract.getSide()) != 0) {
				LOGGER.error("not all contracts have the same side, customerId: {}, currencyPair: {}, wlOrderId: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId()});
				return false;
			}
		}
		return true;
	}
	
	private void netOpen(BigDecimal netAmount, JhfWlOrder order) 
	throws Exception {
		//
		LOGGER.info("start to net open, customerId: {}, currencyPair: {}, wlOrderId: {}, amount: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), netAmount});
		this.wlNetterService.netOpen(netAmount, order);
		
		//
		LOGGER.info("start to update formula, customerId: {}", order.getCustomerId());
		try {
			LockHelper.updateFormula(order.getCustomerId(),
					FormulaDirtyFlagEnum.ALIVE_ORDER_FLAG,
					FormulaDirtyFlagEnum.ALIVE_CONTRACT_FLAG);
		} catch(Exception e) {
			LOGGER.error("failed to update formula, customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
		}
	}
	
	private void netClose(BigDecimal netAmount, JhfWlOrder order, JhfAliveContract contract)
	throws Exception {
		//
		LOGGER.info("start to net close, customerId: {}, currencyPair: {}, wlOrderId: {}, contractId: {}, amount: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), contract.getContractId(), netAmount});
		this.wlNetterService.netClose(netAmount, order, contract);
		
		//
		LOGGER.info("start to update formula, customerId: {}", order.getCustomerId());
		try {
			LockHelper.updateFormula(order.getCustomerId(),
					FormulaDirtyFlagEnum.ALIVE_ORDER_FLAG,
					FormulaDirtyFlagEnum.ALIVE_CONTRACT_FLAG,
					FormulaDirtyFlagEnum.UNREALIZED_CASHFLOW_FLAG);			
		} catch(Exception e) {
			LOGGER.error("failed to update formula, customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
		}
	}
}
