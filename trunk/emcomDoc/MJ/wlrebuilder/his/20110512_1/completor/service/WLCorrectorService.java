package jp.emcom.adv.fx.completor.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.completor.service.dao.WLCorrectorDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.bo.enums.CashflowTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.PositionSourceFlagEnum;
import cn.bestwiz.jhf.core.bo.enums.SideEnum;
import cn.bestwiz.jhf.core.bo.enums.TradeTypeEnum;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContractBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveExecution;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrency;
import cn.bestwiz.jhf.core.dao.bean.main.JhfHedgeCusttrade;
import cn.bestwiz.jhf.core.dao.bean.main.JhfSysPositionInsert;
import cn.bestwiz.jhf.core.dao.bean.main.JhfUnrealizedCashflow;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBind;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.CurrencyHelper;
import cn.bestwiz.jhf.core.util.RateCalcHelpers;

/**
 * 
 *
 */
public class WLCorrectorService {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(WLCorrectorService.class);
	
	//
	private static final String COUNTERCCY_JPY = "JPY";
	
	//
	private WLCorrectorDao wlCorrectorDao;
	private final AtomicBoolean verbose = new AtomicBoolean(true);

	/**
	 * 
	 */
	public boolean isVerbose() {
		return verbose.get();
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose.set(true);
	}
	
	public void setWlCorrectorDao(WLCorrectorDao wlCorrectorDao) {
		this.wlCorrectorDao = wlCorrectorDao;
	}
	
	/**
	 * 
	 */
	public void updateQuietly(JhfWlOrder order) {
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			this.wlCorrectorDao.update(order);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			LOGGER.error("failed to update order: " + order, e);
		}
	}

	/**
	 * 
	 */
	public void correct(JhfWlOrder jwo) throws Exception {
		final Date now = new Date();
		try {
			//
			LOGGER.info("start to correct, wlOrderId: {}", new Object[]{jwo.getWlOrderId()});
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			
			//
			final List<JhfWlOrderBind> wlOrderBinds = this.wlCorrectorDao.findWlOrderBinds(jwo.getWlOrderId());
			for(JhfWlOrderBind jwob : wlOrderBinds) {
				if(jwob.getTradeType().intValue() == TradeTypeEnum.TRADE_OPEN_ENUM.getValue()) {
					correctOpen(jwo, jwob);
				} else if(jwob.getTradeType().intValue() == TradeTypeEnum.TRADE_SETTLE_ENUM.getValue() || jwob.getTradeType().intValue() == TradeTypeEnum.TRADE_DAILY_SETTLE_ENUM.getValue()) {
					correctClose(jwo, jwob);
				} else {
					throw new Exception("invalid trade type, wlOrderId: " + jwob.getId().getWlOrderId() + ", orderId: " + jwob.getId().getOrderId());
				}
			}
			
			// JhfHedgeCusttrade
			final JhfHedgeCusttrade jhc = this.wlCorrectorDao.getHedgeCusttrade(jwo.getWlOrderId());
			if(jhc.getAmount().compareTo(jhc.getAmountNotHedged()) == 0 && jhc.getAmountHedging().compareTo(BigDecimal.ZERO) == 0) {
				jhc.setPrice(jwo.getExecutionPrice());
				jhc.setRevisionNumber(jhc.getRevisionNumber().add(BigDecimal.ONE));//xml not setup,so then
				this.wlCorrectorDao.update(jhc);
				if(isVerbose()) LOGGER.info("updated JhfHedgeCusttrade, wlOrderId: {}", jwo.getWlOrderId());
			}
			
			// JhfSysPositionInsert
			final JhfSysPositionInsert jspi = this.wlCorrectorDao.getSysPositionInsert(jwo.getWlOrderId(), PositionSourceFlagEnum.WHIELABEL_ORDER_ENUM);
			jspi.setCustCounterAmount(jwo.getOrderAmount().multiply(jwo.getExecutionPrice()).multiply(jwo.getSide()));
			this.wlCorrectorDao.update(jspi);
			if(isVerbose()) LOGGER.info("updated JhfSysPositionInsert, wlOrderId: {}", jwo.getWlOrderId());
			
			// JhfWlOrder
			jwo.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal());
			this.wlCorrectorDao.update(jwo);
			if(isVerbose()) LOGGER.info("updated JhfWlOrder, wlOrderId: {}", jwo.getWlOrderId());
			
			//
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			LOGGER.error("failed to correct, wlOrderId: " + jwo.getWlOrderId(), e);
			throw e;
		} finally {
			final long delay = System.currentTimeMillis() - now.getTime();
			LOGGER.info("correct completed, wlOrderId: {}, delay: {}", new Object[]{jwo.getWlOrderId(), delay});
		}
	}

	/**
	 * 
	 */
	private void correctOpen(JhfWlOrder jwo, JhfWlOrderBind jwob) throws Exception {
		//
		LOGGER.info("start to correct open, wlOrderId: {}, orderId: {}", new Object[]{jwob.getId().getWlOrderId(), jwob.getId().getOrderId()});
		
		// JhfAliveOrder
		final JhfAliveOrder jao = this.wlCorrectorDao.getAliveOrder(jwob.getId().getOrderId());
		jao.setExecutionPrice(jwo.getExecutionPrice());
		jao.setTradePrice(jwo.getExecutionPrice());
		this.wlCorrectorDao.update(jao);
		if(isVerbose()) LOGGER.info("updated JhfAliveOrder, wlOrderId: {}, orderId: {}", jwo.getWlOrderId(), jao.getOrderId());
		
		// JhfAliveExecution
		final JhfAliveExecution jae = this.wlCorrectorDao.findAliveExecutionByOrderId(jwob.getId().getOrderId());
		jae.setExecutionPrice(jwo.getExecutionPrice());
		jae.setTradePrice(jwo.getExecutionPrice());
		jae.setRevisionNumber(jae.getRevisionNumber().add(BigDecimal.ONE));//xml not setup,so then
		this.wlCorrectorDao.update(jae);
		if(isVerbose()) LOGGER.info("updated JhfAliveExecution, wlOrderId: {}, executionId: {}", jwo.getWlOrderId(), jae.getExecutionId());
		
		// JhfAliveContract
		final JhfAliveContract jac = this.wlCorrectorDao.getAliveContractByOrderId(jwob.getId().getOrderId());
		jac.setExecutionPrice(jwo.getExecutionPrice());
		jac.setTradePrice(jwo.getExecutionPrice());
		jac.setRevisionNumber(jac.getRevisionNumber().add(BigDecimal.ONE));//xml not setup,so then
		this.wlCorrectorDao.update(jac);
		if(isVerbose()) LOGGER.info("updated JhfAliveContract, wlOrderId: {}, contractId: {}", jwo.getWlOrderId(), jac.getContractId());
		
		//
		final List<JhfAliveContractBind> jacbs = this.wlCorrectorDao.findAliveContractBindsByContractId(jac.getContractId());
		for(JhfAliveContractBind jacb : jacbs) {
			//
			final JhfWlOrderBind jwobClose = this.wlCorrectorDao.findWlOrderBindByExecutionId(jacb.getId().getExecutionId());
			final JhfWlOrder jwoClose = this.wlCorrectorDao.findJhfWlOrder(jwobClose.getId().getWlOrderId());

			//
			final JhfAliveExecution execution = this.wlCorrectorDao.getAliveExecution(jacb.getId().getExecutionId());
			final BigDecimal spotPl = calcSpotProfitLoss(execution, jac);
			final BigDecimal convertRate = (spotPl.compareTo(BigDecimal.ZERO) < 0) ? jwoClose.getConvertPriceBuy() : jwoClose.getConvertPriceSell();
			final BigDecimal spotPlJpy = evalYen(COUNTERCCY_JPY, spotPl, convertRate);
			
			//
			execution.setConvertRate(convertRate);
			this.wlCorrectorDao.update(execution);
			if(isVerbose()) LOGGER.info("updated JhfAliveExecution, wlOrderId: {}, executionId: {}", jwo.getWlOrderId(), jae.getExecutionId());
			
			// JhfAliveContractBind
			jacb.setSpotPl(spotPlJpy);
			this.wlCorrectorDao.update(jacb);
			if(isVerbose()) LOGGER.info("updated JhfAliveContractBind, wlOrderId: {}, contractId: {}, executionId: {}, spotPlJpy: {}", new Object[]{jwo.getWlOrderId(), jacb.getId().getContractId(), jacb.getId().getExecutionId(), jacb.getSpotPl()});
			
			// JhfUnrealizedCashflow
			final JhfUnrealizedCashflow jucf = this.wlCorrectorDao.findUnrealizedCashflow(jacb.getId().getExecutionId(), CashflowTypeEnum.CASHFLOW_SPOT_ENUM);
			jucf.setRate(convertRate);
			jucf.setCashflowAmount(spotPlJpy);
			jucf.setCashflowAmountOriginal(spotPl);
			this.wlCorrectorDao.update(jucf);
			if(isVerbose()) LOGGER.info("updated JhfUnrealizedCashflow, wlOrderId: {}, cashflowId: {}, rate: {}, cashflowAmount: {}, cashflowAmountOriginal: {}", new Object[]{jwo.getWlOrderId(), jucf.getCashflowId(), jucf.getRate(), jucf.getCashflowAmount(), jucf.getCashflowAmountOriginal()});
		}
	}
	
	private void correctClose(JhfWlOrder jwo, JhfWlOrderBind jwob) throws Exception {
		//
		LOGGER.info("start to correct close, wlOrderId: {}, orderId: {}", new Object[]{jwob.getId().getWlOrderId(), jwob.getId().getOrderId()});
		
		// JhfAliveOrder
		final JhfAliveOrder jao = this.wlCorrectorDao.getAliveOrder(jwob.getId().getOrderId());
		jao.setExecutionPrice(jwo.getExecutionPrice());
		jao.setTradePrice(jwo.getExecutionPrice());
		this.wlCorrectorDao.update(jao);
		if(isVerbose()) LOGGER.info("updated JhfAliveOrder, wlOrderId: {}, orderId: {}", jwo.getWlOrderId(), jao.getOrderId());
		
		// JhfAliveExecution(1)
		final JhfAliveExecution jae = this.wlCorrectorDao.findAliveExecutionByOrderId(jwob.getId().getOrderId());
		jae.setExecutionPrice(jwo.getExecutionPrice());
		jae.setTradePrice(jwo.getExecutionPrice());
		jae.setRevisionNumber(jae.getRevisionNumber().add(BigDecimal.ONE));//xml not setup,so then
		
		//
		final JhfAliveContract contract = this.wlCorrectorDao.getAliveContractByContractId(jao.getSettleContractId());
		final BigDecimal spotPl = calcSpotProfitLoss(jae, contract);
		final BigDecimal convertRate = (spotPl.compareTo(BigDecimal.ZERO) < 0) ? jwo.getConvertPriceBuy() : jwo.getConvertPriceSell();
		final BigDecimal spotPlJpy = evalYen(COUNTERCCY_JPY, spotPl, convertRate);
		
		// JhfAliveExecution(2)
		jae.setConvertRate(convertRate);
		this.wlCorrectorDao.update(jae);
		if(isVerbose()) LOGGER.info("updated JhfAliveExecution, wlOrderId: {}, executionId: {}", jwo.getWlOrderId(), jae.getExecutionId());
		
		// JhfAliveContractBind
		final JhfAliveContractBind jacb = this.wlCorrectorDao.findAliveContractBindByExecutionId(jae.getExecutionId());
		jacb.setSpotPl(spotPlJpy);
		this.wlCorrectorDao.update(jacb);
		if(isVerbose()) LOGGER.info("updated JhfAliveContractBind, wlOrderId: {}, contractId: {}, executionId: {}, spotPlJpy: {}", new Object[]{jwo.getWlOrderId(), jacb.getId().getContractId(), jacb.getId().getExecutionId(), jacb.getSpotPl()});
		
		// JhfUnrealizedCashflow
		final JhfUnrealizedCashflow jucf = this.wlCorrectorDao.findUnrealizedCashflow(jae.getExecutionId(), CashflowTypeEnum.CASHFLOW_SPOT_ENUM);
		jucf.setRate(convertRate);
		jucf.setCashflowAmount(spotPlJpy);
		jucf.setCashflowAmountOriginal(spotPl);
		this.wlCorrectorDao.update(jucf);
		if(isVerbose()) LOGGER.info("updated JhfUnrealizedCashflow, wlOrderId: {}, cashflowId: {}, rate: {}, cashflowAmount: {}, cashflowAmountOriginal: {}", new Object[]{jwo.getWlOrderId(), jucf.getCashflowId(), jucf.getRate(), jucf.getCashflowAmount(), jucf.getCashflowAmountOriginal()});
	}
	
	private BigDecimal calcSpotProfitLoss(JhfAliveExecution jae, JhfAliveContract contract)
	throws Exception {
		//
		BigDecimal bdContractPrice = contract.getExecutionPrice(); // 建値(约定价格)
		BigDecimal bdExecutionPrice = jae.getExecutionPrice(); // 约定价格(交易价格)
		BigDecimal bdAmount = jae.getExecutionAmount(); // 数量
		
		//
		BigDecimal bdProfitLoss = null; // 损益
		if (jae.getSide().intValue() == SideEnum.SIDE_SELL.getValue()) { // 卖的决济注文(相返为买)
			bdProfitLoss = bdExecutionPrice.subtract(bdContractPrice);
		} else if (jae.getSide().intValue() == SideEnum.SIDE_BUY.getValue()) { // 买的决济注文(相返为买卖)
			bdProfitLoss = bdContractPrice.subtract(bdExecutionPrice);
		}
		bdProfitLoss = bdProfitLoss.multiply(bdAmount); // １通貨あたりの損益×数量

		//
		final String counterCurrencyCode = CurrencyHelper.getCounterCurrencyCode(jae.getCurrencyPair());
		final JhfCurrency jc = ServiceFactory.getConfigService().getCurrency(counterCurrencyCode);
		if (jc != null) {
			bdProfitLoss = bdProfitLoss.setScale(jc.getCurrencyDecimal().intValue(), jc.getCurrencyRound().intValue());
		}
		return bdProfitLoss;
	}
	
	private BigDecimal evalYen(String sCurrency, BigDecimal bdVal, BigDecimal bdRate) throws Exception {
		BigDecimal bdAmount = bdVal;
		BigDecimal bdValue = bdAmount.multiply(bdRate);
		bdValue = RateCalcHelpers.caleCurrencyRound(sCurrency, bdValue) ;
		return bdValue;
	}
}
