package jp.emcom.adv.fx.completor.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jp.emcom.adv.fx.completor.service.dao.WLNetterDao;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.bo.bean.CashflowInfo;
import cn.bestwiz.jhf.core.bo.bean.FillingOrderInfo;
import cn.bestwiz.jhf.core.bo.bean.PositionInfo;
import cn.bestwiz.jhf.core.bo.contructor.CashflowFactory;
import cn.bestwiz.jhf.core.bo.contructor.ContractFactory;
import cn.bestwiz.jhf.core.bo.contructor.ExecutionFactory;
import cn.bestwiz.jhf.core.bo.contructor.OrderInfoFactory;
import cn.bestwiz.jhf.core.bo.contructor.PositionInfoFactory;
import cn.bestwiz.jhf.core.bo.enums.AutoTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.CashflowTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.ContractChangeReasonEnum;
import cn.bestwiz.jhf.core.bo.enums.CustTraderModeEnum;
import cn.bestwiz.jhf.core.bo.enums.ExecutionCommissionTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.SideEnum;
import cn.bestwiz.jhf.core.bo.enums.StayTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.SwapUnitEnum;
import cn.bestwiz.jhf.core.bo.enums.TradeTypeEnum;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveExecution;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrency;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrencyPair;
import cn.bestwiz.jhf.core.dao.bean.main.JhfGroupDefaultProduct;
import cn.bestwiz.jhf.core.dao.bean.main.JhfUnrealizedCashflow;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBindId;
import cn.bestwiz.jhf.core.dao.middle.TradeDao;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.idgenerate.IdGenerateFacade;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.CurrencyHelper;
import cn.bestwiz.jhf.core.util.DateHelper;
import cn.bestwiz.jhf.core.util.RateCalcHelpers;

/**
 *
 * 
 */
public class WLNetterService {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(WLNetterService.class);
	
	//
	private static final String COUNTERCCY_JPY = "JPY";
	
	//
	private TradeDao tradeDao;
	private WLNetterDao wlNetterDao;
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
	
	public void setTradeDao(TradeDao dao) {
		this.tradeDao = dao;
	}
	
	public void setWlNetterDao(WLNetterDao wlNetterDao) {
		this.wlNetterDao = wlNetterDao;
	}
	
	/**
	 * 
	 */
	public void updateQuietly(JhfWlOrder order) {
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			this.wlNetterDao.update(order);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			LOGGER.error("failed to update order: " + order, e);
		}
	}
	
	public List<JhfAliveContract> findNotSettledContracts(String customerId, String ccy)
	throws Exception {
		List<JhfAliveContract> list = null;
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			list = this.wlNetterDao.findNotSettledContracts(customerId, ccy);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			throw e;
		}
		return list;
	}
	
	/**
	 * 
	 */
	public void netOpen(BigDecimal amount, JhfWlOrder jwo)
	throws Exception {
		final Date now = new Date();
		try {
			//
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			
			// JhfAliveOrder
			final JhfGroupDefaultProduct jgdp = this.wlNetterDao.findGroupDefaultProduct(jwo.getCustomerId(), jwo.getCurrencyPair());
			final JhfAliveOrder jao = OrderInfoFactory.getInstance().converToJhfAliveOrder(amount, jgdp.getProductId(), jwo);
			jao.setTradeType(new BigDecimal(TradeTypeEnum.TRADE_OPEN_ENUM.getValue()));
			this.wlNetterDao.save(jao);
			if(isVerbose()) LOGGER.info("saved JhfAliveOrder, wlOrderId: {}, orderId: {}", jwo.getWlOrderId(), jao.getOrderId());
			
			// JhfAliveExecution
			final FillingOrderInfo orderInfo = OrderInfoFactory.getInstance().convertToFillOrderInfo(jao);
			final PositionInfo positionInfo = PositionInfoFactory.getInstance().createPositionInfo(
					orderInfo, jwo.getExecutionDate(), jwo.getExecutionPrice(), false, null,
					CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue(), StayTypeEnum.STAY_TYPE_1_ENUM.getValue());
			positionInfo.setGroupId(jgdp.getId().getGroupId());
			positionInfo.setSettleDate(jwo.getSettleDate());
			positionInfo.setOriginalSettleDate(jwo.getSettleDate());
			positionInfo.setCommissionTax(BigDecimal.ZERO);
			positionInfo.setOpenCommission(BigDecimal.ZERO);
			positionInfo.setSettleCommission(BigDecimal.ZERO);
			positionInfo.setExecutionCmsType(ExecutionCommissionTypeEnum.OPEN_COMMISSION_ENUM.getValue());
			final JhfAliveExecution jae = ExecutionFactory.getInstance().create(orderInfo, positionInfo, BigDecimal.ONE, AutoTypeEnum.AUTO_TYPE_ENUM.getName());
			jae.setExecutionDatetime(jwo.getExecutionTime());
			jae.setExecutionTime(DateHelper.formatHms(jwo.getExecutionTime()));
			this.wlNetterDao.save(jae);
			if(isVerbose()) LOGGER.info("saved JhfAliveExecution, wlOrderId: {}, executionId: {}", jwo.getWlOrderId(), jae.getExecutionId());
			
			// JhfAliveContract
			final JhfAliveContract jac = ContractFactory.getInstance().create(jae, positionInfo, jwo.getSettleDate(), orderInfo);
			jac.setChangeReason(new BigDecimal(ContractChangeReasonEnum.CONTRACT_REASON_OPEN_ENUM.getValue()));
			this.wlNetterDao.save(jac);
			if(isVerbose()) LOGGER.info("saved JhfAliveContract, wlOrderId: {}, contractId: {}", jwo.getWlOrderId(), jac.getContractId());
			
			// JfhWlOrder
			jwo.setCompletionTime(now);
			jwo.setCompletionTimeLong(now.getTime());
			jwo.setCompletionAmount(jwo.getCompletionAmount().add(amount));
			this.wlNetterDao.update(jwo);
			if(isVerbose()) LOGGER.info("updated JhfWlOrder, wlOrderId: {}, completionAmount: {}", jwo.getWlOrderId(), jwo.getCompletionAmount());
			
			// JhfWlOrderBind
			final JhfWlOrderBind jwob = new JhfWlOrderBind();
			jwob.setId(new JhfWlOrderBindId(jwo.getWlOrderId(), jao.getOrderId()));
			jwob.setAllocateAmount(jao.getOrderAmount());
			jwob.setExecutionId(jae.getExecutionId());
			jwob.setTradeType(jao.getTradeType());
			jwob.setInputStaffId(jao.getInputStaffId());
			jwob.setUpdateStaffId(jao.getInputStaffId());
			jwob.setActiveFlag(new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
			this.wlNetterDao.save(jwob);
			if(isVerbose()) LOGGER.info("saved JhfWlOrderBind, wlOrderId: {}, orderId: {}, allocateAmount: {}", new Object[]{jwob.getId().getWlOrderId(), jwob.getId().getOrderId(), jwob.getAllocateAmount()});
			
			//
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			LOGGER.error("failed to net open, amount: " + amount + ", wlOrderId: " + jwo.getWlOrderId(), e);
			throw e;
		} finally {
			final long delay = System.currentTimeMillis() - now.getTime();
			LOGGER.info("net open completed, wlOrderId: {}, amount: {}, delay: {}", new Object[]{jwo.getWlOrderId(), amount, delay});
		}
	}
	
	public void netClose(BigDecimal amount, JhfWlOrder jwo, JhfAliveContract contract) 
	throws Exception {
		final Date now = new Date();
		try {
			//
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			
			// JhfAliveOrder
			final JhfGroupDefaultProduct jgdp = this.wlNetterDao.findGroupDefaultProduct(jwo.getCustomerId(), jwo.getCurrencyPair());
			final JhfAliveOrder jao = OrderInfoFactory.getInstance().converToJhfAliveOrder(amount, jgdp.getProductId(), jwo);
			jao.setTradeType(contract.getSettledType());
			jao.setTopOrderId(contract.getOrderId());
			jao.setSettleContractId(contract.getContractId());
			this.wlNetterDao.save(jao);
			if(isVerbose()) LOGGER.info("saved JhfAliveOrder, wlOrderId: {}, orderId: {}", jwo.getWlOrderId(), jao.getOrderId());
			
			// JhfAliveExecution
			final FillingOrderInfo orderInfo = OrderInfoFactory.getInstance().convertToFillOrderInfo(jao);
			PositionInfo positionInfo = PositionInfoFactory.getInstance().createPositionInfo(
					orderInfo, jwo.getExecutionDate(), jwo.getExecutionPrice(), false, null,
					CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue(), StayTypeEnum.STAY_TYPE_1_ENUM.getValue());
			positionInfo.setGroupId(jgdp.getId().getGroupId());
			positionInfo.setSettleDate(jwo.getSettleDate());
			positionInfo.setOriginalSettleDate(jwo.getSettleDate());
			positionInfo.setCommissionTax(BigDecimal.ZERO);
			positionInfo.setOpenCommission(BigDecimal.ZERO);
			positionInfo.setSettleCommission(BigDecimal.ZERO);
			positionInfo.setExecutionCmsType(ExecutionCommissionTypeEnum.SETTLE_COMMISSION_ENUM.getValue());
			positionInfo = calcSwapProfitLoss(positionInfo, jao, contract);
			positionInfo = calcSpotProfitLoss(positionInfo, jao, contract);
			
			final BigDecimal convertRate;
			if(CurrencyHelper.getCounterCurrencyCode(jao.getCurrencyPair()).equals(COUNTERCCY_JPY)){
				convertRate = BigDecimal.ONE;
			}else{
				convertRate = (positionInfo.getSpot().compareTo(BigDecimal.ZERO) < 0) ? jwo.getConvertPriceBuy() : jwo.getConvertPriceSell();
			}
			
			final JhfAliveExecution jae = ExecutionFactory.getInstance().create(orderInfo, positionInfo, convertRate, AutoTypeEnum.AUTO_TYPE_ENUM.getName());
			jae.setExecutionDatetime(jwo.getExecutionTime());
			jae.setExecutionTime(DateHelper.formatHms(jwo.getExecutionTime()));
			this.wlNetterDao.save(jae);
			if(isVerbose()) LOGGER.info("saved JhfAliveExecution, wlOrderId: {}, executionId: {}, spotPl: {}, swapPl: {}", new Object[]{jwo.getWlOrderId(), jae.getExecutionId(), positionInfo.getSpot(), positionInfo.getSwap()});
			
			// JhfUnrealizedCashflow
			final List<JhfUnrealizedCashflow> jucfs = createCashflow(positionInfo, convertRate);
			for(JhfUnrealizedCashflow jucf : jucfs) {
				this.wlNetterDao.save(jucf);	
				if(isVerbose()) LOGGER.info("saved JhfUnrealizedCashflow, wlOrderId: {}, cashflowId: {}, cashflowType: {}", new Object[]{jwo.getWlOrderId(), jucf.getCashflowId(), jucf.getCashflowType(), jucf.getCashflowAmount()});
			}
			
			// Lock contract
			final long mark = System.currentTimeMillis();
			contract = tradeDao.getContractUseUpgradeLock(contract.getContractId());
			contract.setAmountSettling(jao.getOrderAmount());
			if(isVerbose()) LOGGER.info("locked JhfAliveContract, wlOrderId: {}, contractId: {}, delay: {}", new Object[]{jwo.getWlOrderId(), contract.getContractId(), (System.currentTimeMillis() - mark)});
			
			// JhfAliveContract
			final BigDecimal bdJpySpot = jucfs.get(0).getCashflowAmount();
	        final BigDecimal bdJpySwap = jucfs.size() > 1 ? jucfs.get(1).getCashflowAmount() : BigDecimal.ZERO;
			tradeDao.executeSettleOrderToContract(orderInfo, jae, contract, positionInfo, bdJpySpot, bdJpySwap, true);
			if(isVerbose()) LOGGER.info("updated JhfAliveContract, wlOrderId: {}, contractId: {}", jwo.getWlOrderId(), contract.getContractId());
			
			// JhfWlOrder
			jwo.setCompletionTime(now);
			jwo.setCompletionTimeLong(now.getTime());
			jwo.setCompletionAmount(jwo.getCompletionAmount().add(amount));
			this.wlNetterDao.update(jwo);
			if(isVerbose()) LOGGER.info("updated JhfWlOrder, wlOrderId: {}, completionAmount: {}", jwo.getWlOrderId(), jwo.getCompletionAmount());
			
			// JhfWlOrderBind
			final JhfWlOrderBind jwob = new JhfWlOrderBind();
			jwob.setId(new JhfWlOrderBindId(jwo.getWlOrderId(), jao.getOrderId()));
			jwob.setAllocateAmount(jao.getOrderAmount());
			jwob.setExecutionId(jae.getExecutionId());
			jwob.setTradeType(jao.getTradeType());
			jwob.setInputStaffId(jao.getInputStaffId());
			jwob.setUpdateStaffId(jao.getInputStaffId());
			jwob.setActiveFlag(new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
			this.wlNetterDao.save(jwob);
			if(isVerbose()) LOGGER.info("saved JhfWlOrderBind, wlOrderId: {}, orderId: {}, allocateAmount: {}", new Object[]{jwob.getId().getWlOrderId(), jwob.getId().getOrderId(), jwob.getAllocateAmount()});
			
			//
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			LOGGER.error("failed to net close, amount: " + amount + ", wlOrderId: " + jwo.getWlOrderId() + ", contractId: " + contract.getContractId(), e);
			throw e;
		} finally {
			final long delay = System.currentTimeMillis() - now.getTime();
			LOGGER.info("net close completed, wlOrderId: {}, contractId: {}, amount: {}, delay: {}", new Object[]{jwo.getWlOrderId(), contract.getContractId(), amount, delay});
		}
	}

	/**
	 * 
	 */
	private PositionInfo calcSwapProfitLoss(PositionInfo position, JhfAliveOrder jao, JhfAliveContract contract)
	throws Exception {
		//
		BigDecimal bdAmountOrg = contract.getAmount(); // 原来的数量
		BigDecimal bdAmountSettled = contract.getAmountSettled(); // 已决济的数量
		BigDecimal bdAmountNoSettled = bdAmountOrg.subtract(bdAmountSettled); // 未决济数量（包括决济注文中）
		BigDecimal bdAmount = jao.getOrderAmount(); // 此次决济的数量
		BigDecimal bdSwapTotal = contract.getSwapInterest(); // SWAPPOIT累计
		BigDecimal bdSwap = bdSwapTotal.multiply(bdAmount); // 计算本次决济时的swap值，swap计算公式为：(swap = swap利息*本次决济注文数/未决济数量)
		
		//
		final String counterCurrencyCode = CurrencyHelper.getCounterCurrencyCode(jao.getCurrencyPair());
		final JhfCurrency jc = ServiceFactory.getConfigService().getCurrency(counterCurrencyCode);
		bdSwap = bdSwap.divide(bdAmountNoSettled, jc.getCurrencyDecimal().intValue(), jc.getCurrencyRound().intValue());
		position.setSwap(bdSwap);
		return position;
	}
	
	private PositionInfo calcSpotProfitLoss(PositionInfo position, JhfAliveOrder jao, JhfAliveContract contract)
	throws Exception {
		//
		BigDecimal bdContractPrice = contract.getExecutionPrice(); // 建値(约定价格)
		BigDecimal bdExecutionPrice = position.getPrice(); // 约定价格(交易价格)
		BigDecimal bdAmount = jao.getOrderAmount(); // 此次决济的数量
		
		BigDecimal bdProfitLoss = null; // 损益
		if (jao.getSide().intValue() == SideEnum.SIDE_SELL.getValue()) { // 卖的决济注文(相返为买)
			bdProfitLoss = bdExecutionPrice.subtract(bdContractPrice);
		} else if (jao.getSide().intValue() == SideEnum.SIDE_BUY.getValue()) { // 买的决济注文(相返为买卖)
			bdProfitLoss = bdContractPrice.subtract(bdExecutionPrice);
		}
		bdProfitLoss = bdProfitLoss.multiply(bdAmount); // １通貨あたりの損益×数量
		
		//
		final String counterCurrencyCode = CurrencyHelper.getCounterCurrencyCode(jao.getCurrencyPair());
		final JhfCurrency jc = ServiceFactory.getConfigService().getCurrency(counterCurrencyCode);
		if (jc != null) {
			bdProfitLoss = bdProfitLoss.setScale(jc.getCurrencyDecimal().intValue(), jc.getCurrencyRound().intValue());
		}
		
		//
		position.setSpot(bdProfitLoss); // spot损益的设置
		return position;
	}
	
	private List<JhfUnrealizedCashflow> createCashflow(PositionInfo position, BigDecimal convertRate)
	throws Exception {
		//
		final List<JhfUnrealizedCashflow> r = new ArrayList<JhfUnrealizedCashflow>();
		final Timestamp toToday = DateHelper.getTodaysTimestamp();
		final String counterCurrencyCode = CurrencyHelper.getCounterCurrencyCode(position.getCurrencyPair());

		// Spot
		CashflowInfo cashflowInfo = new CashflowInfo();
		cashflowInfo.setValueDate(position.getOriginalSettleDate());
		cashflowInfo.setProductId(position.getProductId());
		cashflowInfo.setCustomerId(position.getCustomerId());
		cashflowInfo.setEventDate(position.getExecutionDate());
		cashflowInfo.setEventDateTime(toToday);
		cashflowInfo.setSourceId(position.getExecutionId());
		cashflowInfo.setCurrencyCode(COUNTERCCY_JPY);
		cashflowInfo.setAmountOriginal(position.getSpot());
		cashflowInfo.setBalance(null);
		if (StringUtils.isNotEmpty(position.getCashflowIdSpot())) {
			cashflowInfo.setCashflowId(position.getCashflowIdSpot());
		} else {
			cashflowInfo.setCashflowId(IdGenerateFacade.getCashflowId());
		}
		cashflowInfo.setCashflowType(CashflowTypeEnum.CASHFLOW_SPOT_ENUM.getValue());
		cashflowInfo.setRate(convertRate);
		cashflowInfo.setCurrencyCodeOriginal(counterCurrencyCode);
		cashflowInfo.setAmount(evalYen(COUNTERCCY_JPY, position.getSpot(), convertRate));
		r.add(CashflowFactory.getInstance().create(cashflowInfo, position));

		// Swap
		if (position.getSwap().compareTo(BigDecimal.ZERO) != 0) {
			final JhfCurrencyPair jcp = ServiceFactory.getConfigService().getJhfCurrencyPair(position.getCurrencyPair());
			cashflowInfo.setBalance(null);
			if (StringUtils.isNotEmpty(position.getCashflowIdSwap())) {
				cashflowInfo.setCashflowId(position.getCashflowIdSwap());
			} else {
				cashflowInfo.setCashflowId(IdGenerateFacade.getCashflowId());
			}
			cashflowInfo.setCashflowType(CashflowTypeEnum.CASHFLOW_SWAP_ENUM.getValue());
			cashflowInfo.setAmountOriginal(position.getSwap());
			if (jcp != null && jcp.getSwapUnit().intValue() == SwapUnitEnum.JPY_ENUM.getValue()) {
				cashflowInfo.setRate(new BigDecimal("1"));
				cashflowInfo.setCurrencyCodeOriginal(COUNTERCCY_JPY);
			} else {
				cashflowInfo.setRate(convertRate);
				cashflowInfo.setCurrencyCodeOriginal(counterCurrencyCode);
			}
			cashflowInfo.setAmount(evalYen(COUNTERCCY_JPY, position.getSwap(), cashflowInfo.getRate()));
			r.add(CashflowFactory.getInstance().create(cashflowInfo, position));
		}
		return r;
	}
	
	private BigDecimal evalYen(String sCurrency, BigDecimal bdVal, BigDecimal bdRate) throws Exception {
		BigDecimal bdAmount = bdVal;
		BigDecimal bdValue = bdAmount.multiply(bdRate);
		bdValue = RateCalcHelpers.caleCurrencyRound(sCurrency, bdValue) ;
		return bdValue;
	}

}
