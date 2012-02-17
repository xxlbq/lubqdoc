package jp.emcom.adv.fx.completor.biz.executor.impl;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.emcom.adv.fx.commons.bo.enums.CorrectionTypeEnum;
import jp.emcom.adv.fx.commons.bo.enums.OrderStatusEnum;
import jp.emcom.adv.fx.commons.msgsenders.AbstractMessageSender;
import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;
import jp.emcom.adv.fx.completor.biz.executor.impl.DefaultParWLExecutor.RebuildTask;
import jp.emcom.adv.fx.completor.service.WLRebuilderService;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.application.impl.AbstractLifecycle;
import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.ExecutionTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.PositionSourceFlagEnum;
import cn.bestwiz.jhf.core.bo.enums.PriHedgeDestTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.WLOrderStatusEnum;
import cn.bestwiz.jhf.core.bo.enums.WLStuffIdEnum;
import cn.bestwiz.jhf.core.bo.enums.WlOrderRouteEnum;
import cn.bestwiz.jhf.core.bo.exceptions.ServiceException;
import cn.bestwiz.jhf.core.dao.bean.info.JhfWlRateTemplate;
import cn.bestwiz.jhf.core.dao.bean.info.JhfWlRateTemplateId;
import cn.bestwiz.jhf.core.dao.bean.main.JhfApplicationDate;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrency;
import cn.bestwiz.jhf.core.dao.bean.main.JhfSysPositionInsert;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.idgenerate.IdGenerateFacade;
import cn.bestwiz.jhf.core.idgenerate.exception.IdGenerateException;
import cn.bestwiz.jhf.core.jms.bean.WLOrderRequestInfo;
import cn.bestwiz.jhf.core.jms.bean.WLOrderResponseInfo;
import cn.bestwiz.jhf.core.jms.bean.WLSpotRateInfo;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.CurrencyHelper;
import cn.bestwiz.jhf.core.util.DateHelper;
import cn.bestwiz.jhf.core.util.RateCalcHelpers;

public class WLRebuilder extends AbstractLifecycle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WLRebuilder.class);
	private WLRebuilderService service ;
	private AbstractMessageSender<WLOrderResponseInfo> orderResponseSender;
	
	public AbstractMessageSender<WLOrderResponseInfo> getOrderResponseSender() {
		return orderResponseSender;
	}
	public void setOrderResponseSender(
			AbstractMessageSender<WLOrderResponseInfo> orderResponseSender) {
		this.orderResponseSender = orderResponseSender;
	}
	public WLRebuilderService getService() {
		return service;
	}
	public void setService(WLRebuilderService service) {
		this.service = service;
	}
	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
	}
	//
	@Override
	protected void doStop(long timeout) throws Exception {
		// TODO Auto-generated method stub
	}
	
	//
	public void rebuild(RebuildTask rebuildTask){
		try {
			BigDecimal rebuildTotal = service.sumRebuildAmount(rebuildTask.getCustomerId(),rebuildTask.getCurrencyPair());
			
			if(rebuildTotal == null || BigDecimal.ZERO.compareTo(rebuildTotal) == 0){
				LOGGER.info("no position need rebuild , customerId : {}" ,new Object[]{rebuildTask.getCustomerId()});
				return ;
			}
			BigDecimal Amount = rebuildTotal.abs();
			BigDecimal rebuildSide =( rebuildTotal.compareTo(BigDecimal.ZERO) >= 0 ) ? new BigDecimal("1"):new BigDecimal("-1");
			//
			JhfWlOrder rebuildSettleOrder = toRebuildOrder( Amount, rebuildSide,rebuildTask);
			JhfSysPositionInsert jspiSettle = buildJhfSysPositionInsert(rebuildSettleOrder);
			
			JhfWlOrder rebuildOpenOrder = reverseNewOrder(rebuildSettleOrder);
			JhfSysPositionInsert jspiOpen = buildJhfSysPositionInsert(rebuildOpenOrder);
			
			service.posRebuild(rebuildSettleOrder,jspiSettle,rebuildOpenOrder ,jspiOpen);
			
		} catch (Exception e) {
			LOGGER.error("rebuild failed " + rebuildTask, e);
		} finally{
//			final WLOrderResponseInfo response = toOrderResponseInfo(rebuildOrder);
//			getOrderResponseSender().sendMessage(response);
//			LOGGER.info("order response was successfully sent, clientId: {}, wlOrderId: {}, orderStatus: {}, failReason: {}, executionPrice: {}, totalDelay: {}", new Object[]{order.getClientId(), order.getWlOrderId(), order.getOrderStatus(), order.getFailReason(), response.getExecutionPrice(), (System.currentTimeMillis() - timestamp)});
		
		}

	}
	private JhfWlOrder reverseNewOrder(JhfWlOrder rebuildSettleOrder) {
		JhfWlOrder open =  new JhfWlOrder();
		try {
//			BeanUtils.copyProperties(open, rebuildSettleOrder);
			open.setWlOrderId(IdGenerateFacade.getWLOrderId());
			open.setSide(rebuildSettleOrder.getSide().negate());

			
			open.setOrderAmount(rebuildSettleOrder.getOrderAmount());
			open.setClientOrderNo(IdGenerateFacade.getWLClientOrderNo());
			open.setCustomerId(rebuildSettleOrder.getCustomerId());
			open.setClientId(rebuildSettleOrder.getClientId());
			open.setCurrencyPair(rebuildSettleOrder.getCurrencyPair());
			open.setOrderPrice(rebuildSettleOrder.getOrderPrice());
			open.setOrderPriceId(rebuildSettleOrder.getOrderPriceId());
			open.setExecutionPrice(rebuildSettleOrder.getExecutionPrice());
			open.setOrderDate(rebuildSettleOrder.getOrderDate());
			
			open.setOrderStatus(rebuildSettleOrder.getOrderStatus());
			open.setSlippage(rebuildSettleOrder.getSlippage());
			open.setRejectReason(null);
			open.setOrderRequestTime(rebuildSettleOrder.getOrderRequestTime());
			open.setOrderRequestTimeLong(rebuildSettleOrder.getOrderRequestTimeLong());
			open.setOrderAcceptTime(rebuildSettleOrder.getOrderAcceptTime());
			open.setOrderAcceptTimeLong(rebuildSettleOrder.getOrderAcceptTimeLong());
			open.setExecutionType(rebuildSettleOrder.getExecutionType());
			//TODO:
			open.setOrderRoute(rebuildSettleOrder.getOrderRoute());
			open.setSettleDate(null);
			open.setExecutionDate(null);
			open.setExecutionTime(null);
			open.setExecutionTimeLong(null);
			
			open.setExecutionPriceId(null);
			open.setExecutionPriceType(null);
			open.setConvertPriceId(null);
			open.setConvertPriceBuy(null);
			open.setConvertPriceSell(null);
			open.setCorrectionType(CorrectionTypeEnum.NORMAL.getValueAsBigDecimal());
			open.setCorrectionTime(null);
			open.setCompletionStatus(null);
			open.setCompletionTime(null);
			open.setCompletionTimeLong(null);
			open.setCompletionAmount(null);
			open.setMemo(null);
			open.setFailReason(null);
			open.setRevisionNumber(1);
			open.setInputDate(rebuildSettleOrder.getInputDate());
			open.setInputStaffId(rebuildSettleOrder.getInputStaffId());
			open.setUpdateDate(rebuildSettleOrder.getUpdateDate());
			open.setUpdateStaffId(rebuildSettleOrder.getUpdateStaffId());
			open.setActiveFlag(new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));

			
			

		} catch (Exception e){
			e.printStackTrace();
		}
		
		return open;
	}
	/**
	 * 
	 */
	private JhfSysPositionInsert buildJhfSysPositionInsert(JhfWlOrder order) throws Exception {
		final JhfSysPositionInsert jspi = new JhfSysPositionInsert();
		jspi.setPositionSourceId(order.getWlOrderId());
		jspi.setPositionSourceFlag(new BigDecimal(PositionSourceFlagEnum.WHIELABEL_ORDER_ENUM.getValue()));
		jspi.setCurrencyPair(order.getCurrencyPair());
		jspi.setFrontDate(order.getOrderDate());
		jspi.setContractCurrencyCode(CurrencyHelper.getPositionCurrencyCode(order.getCurrencyPair()));
		jspi.setCounterCurrencyCode(CurrencyHelper.getCounterCurrencyCode(order.getCurrencyPair()));
		jspi.setDealerCode(PriHedgeDestTypeEnum.HEDGE_AUTO_ENUM.getName());
		jspi.setCustContractAmount(order.getOrderAmount().multiply(order.getSide()).negate());
		jspi.setCustCounterAmount(order.getOrderAmount().multiply(order.getExecutionPrice()).multiply(order.getSide()));
		jspi.setCommission(BigDecimal.ZERO);
		jspi.setInputStaffId(WLStuffIdEnum.WL.getValue());
		jspi.setUpdateStaffId(WLStuffIdEnum.WL.getValue());
		jspi.setActiveFlag(new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		return jspi;
	}
	
	
	
	private JhfWlOrder toRebuildOrder(BigDecimal amount,
			BigDecimal side, RebuildTask rebuildTask) throws Exception {
		
		final JhfWlOrder wlOrder = new JhfWlOrder();

		final Date now = new Date();
		final String staffId = ServiceFactory.getCoreService().getSystemStaffId();
		final Timestamp currentTime  = DateHelper.getSystemTimestamp();
		
		
		wlOrder.setWlOrderId(IdGenerateFacade.getWLOrderId());
		wlOrder.setOrderAmount(amount);
		wlOrder.setSide(side);
		wlOrder.setClientOrderNo(IdGenerateFacade.getWLClientOrderNo());
		wlOrder.setCustomerId(rebuildTask.getCustomerId());
		wlOrder.setClientId(rebuildTask.getClientId());
		wlOrder.setCurrencyPair(rebuildTask.getCurrencyPair());
		wlOrder.setOrderPrice(getMid(rebuildTask));
		wlOrder.setOrderPriceId(rebuildTask.getCloseRatePriceId());
		wlOrder.setExecutionPrice(wlOrder.getOrderPrice());
		wlOrder.setOrderDate(rebuildTask.getApplicationDate().getId().getFrontDate());
		
		
		wlOrder.setOrderStatus(OrderStatusEnum.NEW.getValue());
		wlOrder.setSlippage(BigDecimal.ZERO);
		wlOrder.setRejectReason(null);
		wlOrder.setOrderRequestTime(currentTime);
		wlOrder.setOrderRequestTimeLong(currentTime.getTime());
		wlOrder.setOrderAcceptTime(currentTime);
		wlOrder.setOrderAcceptTimeLong(currentTime.getTime());
		wlOrder.setExecutionType(new BigDecimal(ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()));
		//TODO:
		wlOrder.setOrderRoute(new BigDecimal(WlOrderRouteEnum.ROUTE_FIX_ENUM.getValue()));
		wlOrder.setSettleDate(null);
		wlOrder.setExecutionDate(null);
		wlOrder.setExecutionTime(null);
		wlOrder.setExecutionTimeLong(null);
//		wlOrder.setExecutionPrice(null);
		wlOrder.setExecutionPriceId(null);
		wlOrder.setExecutionPriceType(null);
		wlOrder.setConvertPriceId(null);
		wlOrder.setConvertPriceBuy(null);
		wlOrder.setConvertPriceSell(null);
		wlOrder.setCorrectionType(CorrectionTypeEnum.NORMAL.getValueAsBigDecimal());
		wlOrder.setCorrectionTime(null);
		wlOrder.setCompletionStatus(null);
		wlOrder.setCompletionTime(null);
		wlOrder.setCompletionTimeLong(null);
		wlOrder.setCompletionAmount(null);
		wlOrder.setMemo(null);
		wlOrder.setFailReason(null);
		wlOrder.setRevisionNumber(1);
		wlOrder.setInputDate(now);
		wlOrder.setInputStaffId(staffId);
		wlOrder.setUpdateDate(now);
		wlOrder.setUpdateStaffId(staffId);
		wlOrder.setActiveFlag(new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));

		return wlOrder;
	}


	private BigDecimal getMid(RebuildTask rebuildTask) throws Exception {
//		String sCurrencyCode = CurrencyHelper.getCounterCurrencyCode(rebuildTask.getCurrencyPair());
		// 取得货币的信息
//		m_log.info("get Currency Start:" + order.getOrderId() + "|" +  order.getContractId());
//		JhfCurrency  currency = ServiceFactory.getConfigService().getCurrency(sCurrencyCode);
//		BigDecimal mid = rebuildTask.getCloseRateBuy().add(rebuildTask.getCloseRateSell()).divide(new BigDecimal("2"));
//		mid.setScale(currency.getCurrencyDecimal().intValue(), currency.getCurrencyRound().intValue());
		
		return RateCalcHelpers.CalcMidRate(rebuildTask.getCurrencyPair(), rebuildTask.getCloseRateBuy(), rebuildTask.getCloseRateSell());
	}
	
	
	public long getRebuildTimeByClientId(AutoBuildInfo info, JhfApplicationDate date , long gap) {
//		return date.getFrontEndDatetime().getTime() - info.getTradableEndInterval().intValue() + gap;
		
		//TODO:
		return date.getFrontEndDatetime().getTime() - info.getTradableEndInterval().intValue() + gap;
	}
	
	


	public WLSpotRateInfo findCloseRate(AutoBuildInfo info, String currencyPair,JhfApplicationDate date) throws Exception {
		
		Map<String, Object> rate = service.findCloseRate(info,currencyPair,date);
		if(rate == null){
			return null;
		}
		WLSpotRateInfo rateInfo = new WLSpotRateInfo();
		rateInfo.setAskRate((BigDecimal)rate.get("ASK_RATE"));
		rateInfo.setAskRate((BigDecimal)rate.get("BID_RATE"));
		rateInfo.setWlRateId((String)rate.get("WL_RATE_ID"));
		return rateInfo;
	}

	public List<String> findUsingCurrencyPair(String clientId) throws Exception {
		return service.findUsingCurrencyPair(clientId);
	}
	
	public boolean hadRebuildeBefore(String customerId, String currencyPair, String frontDate) throws Exception {
		return service.hadRebuildeBefore(customerId,currencyPair,frontDate);
	}
	public boolean completorNotFinished(String customerId, String currencyPair, String frontDate) throws Exception {
		return service.completorNotFinishedYet(customerId,currencyPair,frontDate);
	}

	
}
