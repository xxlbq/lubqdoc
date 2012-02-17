package jp.emcom.adv.fx.completor.biz.executor.impl;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.commons.bo.enums.OrderStatusEnum;
import jp.emcom.adv.fx.completor.service.WLCorrectorService;

import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.bo.enums.FormulaDirtyFlagEnum;
import cn.bestwiz.jhf.core.dao.LockHelper;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.service.ServiceFactory;

/**
 * 
 *
 */
public class WLCorrector extends AbstractLifecycle {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(WLCorrector.class);
	
	//
	private WLCorrectorService wlCorrectorService;
	
	/**
	 * 
	 */
	public WLCorrector() {
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
	public WLCorrectorService getWlCorrectorService() {
		return wlCorrectorService;
	}

	public void setWlCorrectorService(WLCorrectorService wlCorrectorService) {
		this.wlCorrectorService = wlCorrectorService;
	}
	
	/**
	 * 
	 */
	public boolean correct(JhfWlOrder order) {
		//
		boolean succeed = false;
		final long now = System.currentTimeMillis();
		try {
			//
			LOGGER.info("start to validate order, customerId: {}, currencyPair: {}, wlOrderId: {}, executionPrice: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getExecutionPrice()});
			if(!validate(order)) {
				return false;
			}
			
			//
			LOGGER.info("start to correct order, customerId: {}, currencyPair: {}, wlOrderId: {}, executionPrice: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), order.getExecutionPrice()});
			this.wlCorrectorService.correct(order);
			
			//
			succeed = true;
			final long delay = System.currentTimeMillis() - now;
			LOGGER.info("corrected an order, customerId: {}, currencyPair: {}, wlOrderId: {}, delay: {}", new Object[]{order.getCustomerId(), order.getCurrencyPair(), order.getWlOrderId(), delay});
		} catch(StaleObjectStateException e) {
			LOGGER.warn("failed to correct order[version mismatch], customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
			// NOP 
		} catch(Exception e) {
			LOGGER.error("failed to correct order, customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
			order.setCompletionStatus(CompletionStatusEnum.CORRECTION_FAILED.getValueAsBigDecimal());
			this.wlCorrectorService.updateQuietly(order);
		} 
		
		//
		if(succeed) {
			try {
				LOGGER.info("start to update formula, customerId: {}", order.getCustomerId());
				LockHelper.updateFormula(order.getCustomerId(),
						FormulaDirtyFlagEnum.ALIVE_ORDER_FLAG,
						FormulaDirtyFlagEnum.ALIVE_CONTRACT_FLAG,
						FormulaDirtyFlagEnum.UNREALIZED_CASHFLOW_FLAG);
			} catch (Exception e) {
				LOGGER.error("failed to update formula, customerId: " + order.getCustomerId() + ", currencyPair: " + order.getCurrencyPair() + ", wlOrderId: " + order.getWlOrderId(), e);
			}
		}
		
		//
		return succeed;
	}
	
	/**
	 * 
	 */
	private boolean validate(JhfWlOrder order) throws Exception {
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
		return true;
	}
}
