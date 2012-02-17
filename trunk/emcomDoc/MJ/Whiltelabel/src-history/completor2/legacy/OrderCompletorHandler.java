package jp.emcom.adv.fx.completor.legacy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.completor.service.WLCompletorService;
import jp.emcom.adv.fx.completor.service.WLNetterService;
import jp.emcom.adv.fx.completor.service.dao.WLNetterDao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.bo.bean.CashflowInfo;
import cn.bestwiz.jhf.core.bo.bean.FillingOrderInfo;
import cn.bestwiz.jhf.core.bo.bean.OrderInfo;
import cn.bestwiz.jhf.core.bo.bean.PositionInfo;
import cn.bestwiz.jhf.core.bo.contructor.CashflowFactory;
import cn.bestwiz.jhf.core.bo.contructor.ContractFactory;
import cn.bestwiz.jhf.core.bo.contructor.ExecutionFactory;
import cn.bestwiz.jhf.core.bo.contructor.OrderInfoFactory;
import cn.bestwiz.jhf.core.bo.contructor.PositionInfoFactory;
import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.CashflowTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.ChangeReasonEnum;
import cn.bestwiz.jhf.core.bo.enums.ContractChangeReasonEnum;
import cn.bestwiz.jhf.core.bo.enums.CustTraderModeEnum;
import cn.bestwiz.jhf.core.bo.enums.SideEnum;
import cn.bestwiz.jhf.core.bo.enums.StayTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.SwapUnitEnum;
import cn.bestwiz.jhf.core.bo.enums.TradeTypeEnum;
import cn.bestwiz.jhf.core.bo.exceptions.DaoException;
import cn.bestwiz.jhf.core.bo.exceptions.ServiceException;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContractBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveExecution;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrency;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCurrencyPair;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBindId;
import cn.bestwiz.jhf.core.dao.middle.CashflowDao;
import cn.bestwiz.jhf.core.dao.middle.MiddleDAOFactory;
import cn.bestwiz.jhf.core.dao.middle.TradeDao;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.idgenerate.IdGenerateFacade;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.ratecache.RateCacheFactory;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.CoreException;
import cn.bestwiz.jhf.core.service.exception.TradeException;
import cn.bestwiz.jhf.core.util.CurrencyHelper;
import cn.bestwiz.jhf.core.util.DateHelper;
import cn.bestwiz.jhf.core.util.RateCalcHelpers;

public class OrderCompletorHandler implements BusinessHandler{
	
	private static final Logger log = LoggerFactory.getLogger(OrderCompletorHandler.class);
	
	private SchedulerExecutor scheduler;
	
	private static Map<String, BlockingQueue<CompletorOrderInfo>>  ququeMap = 
		new HashMap<String, BlockingQueue<CompletorOrderInfo>>();
	
//	private static ConcurrentMap<String ,Object> ignMap = 
//		new ConcurrentHashMap<String, Object>();
	
	private static Map<String ,Object> ignMap = 
		new HashMap<String, Object>();
	
	private final static String COUNTERCCY_JPY = "JPY";
	private List<String> ccysList = null;
	
	private WLCompletorService ws;
	private WLNetterService ns;
	private TradeDao tradeDao ;
	private WLCompletorDao dao;
	private WLNetterDao netterDao;
	private CashflowDao cashflowDao = null;



	public static Map<String, BlockingQueue<CompletorOrderInfo>> getQuqueMap() {
		return ququeMap;
	}
	
	
	public void setDao(WLCompletorDao dao) {
		this.dao = dao;
	}



	public void setTradeDao(TradeDao tradeDao) {
		this.tradeDao = tradeDao;
	}



	public void setCashflowDao(CashflowDao cashflowDao) {
		this.cashflowDao = cashflowDao;
	}

	public void setWs(WLCompletorService ws) {
		this.ws = ws;
	}

	public void setCcysList(List<String> ccysList) {
		this.ccysList = ccysList;
	}

	
	
    // 初始化成员变量
	public static Map<String, Object> getIgnMap() {
		return ignMap;
	}

	public void businessHandle() {
		log.info("Completor Running .");
		
		prepareQueue();
		
		try {
			scheduler.schedule();
			
		} catch (Exception e) {
			log.error("fire error now  .",e);
		}
	}

	private void prepareQueue() {
		log.info("Prepare Completor Queue ...");
		for (String ccy : ccysList) {
			ququeMap.put(ccy,new LinkedBlockingQueue<CompletorOrderInfo>());
		}
		
		Set<Entry<String, BlockingQueue<CompletorOrderInfo>>> entrySet =ququeMap.entrySet();
		for (Entry<String, BlockingQueue<CompletorOrderInfo>> entry : entrySet) {
			
			final String ccy = entry.getKey();
			final BlockingQueue<CompletorOrderInfo> cq = entry.getValue();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						while(true){
							log.info("ccy:{} is takeing ",ccy);
							CompletorOrderInfo order = cq.take();
							complete(order);
						}
					} catch (InterruptedException e) {
						log.error("",e);
					}
					log.warn(ccy+" Completor Thread Interrupted " );
				}
		
			}).start();
		}
		
	}

	private void complete(CompletorOrderInfo info) {
		log.info("order:{} is takeing ",info);
		
		if(ignMap.containsKey(info.getKey()) ){
			info.getLatch().countDown();
			log.warn("Ignore Order  {}",info);
			return ;
		}
		
		if(isAdminUpdate(info)){
			
			log.info("AdminUpdate Order  order={}",info.getJhfWlOrder());
			try{
				adminUpdate(info.getJhfWlOrder());
			}catch(Exception ex){
				log.error("Admin Update Error ",info.getJhfWlOrder(),ex);
				ignMap.put(info.getKey(), info.getKey());
			}finally{
				info.getLatch().countDown();
			}
			
			return ;
		}
		
		try{
			nettingOrder(info);
		}catch(Exception ex){
			log.error("net order Error ",info.getJhfWlOrder(),ex);
			ignMap.put(info.getKey(), info.getKey());
		}finally{
			info.getLatch().countDown();
		}
		
		
	}
	
	private void adminUpdate(JhfWlOrder jhfWlOrder) throws Exception {
		List<JhfAliveOrder> aliveOrderList = dao.queryAliveOrder(jhfWlOrder);
		
		try {
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			for (JhfAliveOrder aliveOrder : aliveOrderList) {
				
				if(aliveOrder.getTradeType().intValue() == TradeTypeEnum.TRADE_OPEN_ENUM.getValue()){
					updateOpen(aliveOrder,jhfWlOrder);
				}else{
					updateSettle(aliveOrder,jhfWlOrder);
				}
			}
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			log.error("save open order error !",e);
			throw e;
		}

	}


	private void updateOpen(JhfAliveOrder aliveOrder, JhfWlOrder jhfWlOrder) throws Exception {
		//
//		1.	更新JHF_ALIVE_ORDER
		updateAliveOrder(aliveOrder, jhfWlOrder);
//		2.	更新JHF_ALIVE_EXECUTION
		JhfAliveExecution execution = dao.queryExecution(aliveOrder);
		updateAliveExecution(execution,jhfWlOrder);
//		3.	更新JHF_ALIVE_CONTRACT
		JhfAliveContract contract = dao.queryContract(aliveOrder);
		updateAliveContract(contract,aliveOrder);
//		4.	更新JHF_HEDGE_CUSTTRADE
		updateHedgeCusttrade(execution,jhfWlOrder);
//		5.	更新JHF_SYSPOSITION_INSERT
		updateSysPosition(execution,jhfWlOrder);
//		6.	对该新规注文对应的决济注文进行决济订正处理
//			更新JHF_CONTRACT_BIND
//			更新JHF_UNREALIZED_CASHFLOW
		
		List<JhfAliveContractBind> contractBinds = 
			queryContractBind(contract.getContractId());
		if(CollectionUtils.isEmpty(contractBinds)){
			return;
		}
		
		for (JhfAliveContractBind jhfAliveContractBind : contractBinds) {
			//决计注文的execution
			JhfAliveExecution execu = dao.queryExecutionById(jhfAliveContractBind.getId().getExecutionId());
			
			BigDecimal newSpotPl = calcSpotProfitLoss(contract.getCurrencyPair(), contract.getExecutionPrice(),
					execu.getExecutionPrice(),execu.getExecutionAmount(),execu.getSide());
			BigDecimal newSpotPlJpyAmount = evalYen(COUNTERCCY_JPY, newSpotPl,
					jhfWlOrder.getSide().intValue() == SideEnum.SIDE_BUY.getValue() 
					? jhfWlOrder.getConvertPriceBuy() : jhfWlOrder.getConvertPriceSell());
			
//			6.	更新JHF_UNREALIZED_CASHFLOW
			updateCashflow(execu,newSpotPl,newSpotPlJpyAmount);
			//		3.	更新JHF_CONTRACT_BIND
			updateContractBind(jhfAliveContractBind,newSpotPl);
		}
		
		
		
	}


	private void updateContractBind(JhfAliveContractBind jhfAliveContractBind,
			BigDecimal newSpotPl) throws DaoException {
		jhfAliveContractBind.setSpotPl(newSpotPl);
		dao.save(jhfAliveContractBind);
	}


	private List<JhfAliveContractBind> queryContractBind(String contractId) {
		// TODO Auto-generated method stub
		return dao.queryContractBind(contractId);
	}


	private void updateAliveContract(JhfAliveContract contract,
			JhfAliveOrder aliveOrder) throws DaoException {
		contract.setExecutionPrice(aliveOrder.getExecutionPrice());
		contract.setTradePrice(aliveOrder.getExecutionPrice());
		contract.setRevisionNumber(contract.getRevisionNumber().add(BigDecimal.ONE));
		dao.update(contract);
		
	}


	private void updateSettle(JhfAliveOrder aliveOrder, JhfWlOrder jhfWlOrder) 
	throws Exception {

		//		1.	更新JHF_ALIVE_ORDER
		updateAliveOrder(aliveOrder,jhfWlOrder);
		
		
//		//		2.	更新JHF_ALIVE_EXECUTION
//		List<JhfAliveExecution> executions = dao.queryExecutions(aliveOrder);
//		updateAliveExecution(executions,jhfWlOrder);
//		//		4.	更新JHF_HEDGE_CUSTTRADE
//		
//		updateHedgeCusttrade(executions,jhfWlOrder);
//		//		5.	更新JHF_SYSPOSITION_INSERT 
//		updateSysPosition(executions,jhfWlOrder);
		
		
		//		2.	更新JHF_ALIVE_EXECUTION
		JhfAliveExecution execution = dao.queryExecution(aliveOrder);
		updateAliveExecution(execution,jhfWlOrder);
		//		4.	更新JHF_HEDGE_CUSTTRADE
		
		updateHedgeCusttrade(execution,jhfWlOrder);
		//		5.	更新JHF_SYSPOSITION_INSERT 
		updateSysPosition(execution,jhfWlOrder);
		
		
		//		6.	更新JHF_UNREALIZED_CASHFLOW
		JhfAliveContract contract = dao.queryContract(aliveOrder);
		BigDecimal newSpotPl = calcSpotProfitLoss(contract.getCurrencyPair(),contract.getExecutionPrice(),
				execution.getExecutionPrice(),execution.getExecutionAmount(),execution.getSide());
		BigDecimal newSpotPlJpyAmount = evalYen(COUNTERCCY_JPY, newSpotPl,
				jhfWlOrder.getSide().intValue() == SideEnum.SIDE_BUY.getValue() 
				? jhfWlOrder.getConvertPriceBuy() : jhfWlOrder.getConvertPriceSell());
		updateCashflow(execution,newSpotPl,newSpotPlJpyAmount);
		//		3.	更新JHF_CONTRACT_BIND
		JhfAliveContractBind contractBind = 
			dao.querySingleContractBind(contract.getContractId());
		updateContractBind(contractBind,newSpotPl);
		
	}

	
	private void updateCashflow(JhfAliveExecution execution, BigDecimal newSpotPl, BigDecimal newSpotPlJpyAmount) throws CoreException {

//		BigDecimal newSpotPl = calcSpotProfitLoss(execution, jhfWlOrder, contract);
		
//		cashflowInfo.setSourceId(pdt.getExecutionId());
//		
//		cashflowInfo.setAmountOriginal(pdt.getSpot());
//		// cash flow金额
//		BigDecimal bdJpyAmount = evalYen(COUNTERCCY_JPY, pdt.getSpot(),bdRate);
//		cashflowInfo.setAmount(bdJpyAmount);
		dao.updateURCashflowAmount(execution,newSpotPl,newSpotPlJpyAmount);
	}
		
	private BigDecimal calcSpotProfitLoss(String ccy,BigDecimal contractPrice,BigDecimal executionSettlePrice,
				BigDecimal orderAmount,BigDecimal  side) throws ServiceException {
		

		// 取得货币的信息
//		log.info("get Currency Start:" + order.getOrderId() + "|" +  order.getContractId());
		JhfCurrency currency = ServiceFactory.getConfigService().getCurrency(ccy);
//		log.info("get Currency End:" + order.getOrderId() + "|" +  order.getContractId());

		BigDecimal bdProfitLoss = null; // 损益
		
		if (side.intValue() == SideEnum.SIDE_SELL.getValue()) {        // 卖的决济注文(相返为买)
			bdProfitLoss = executionSettlePrice.subtract(contractPrice);
		} else if (side.intValue() == SideEnum.SIDE_BUY.getValue()) { // 买的决济注文(相返为买卖)
			bdProfitLoss = contractPrice.subtract(executionSettlePrice);
		}
		
		bdProfitLoss = bdProfitLoss.multiply(orderAmount);  // １通貨あたりの損益×数量
		if (currency != null) {
			bdProfitLoss = bdProfitLoss.setScale(currency
					.getCurrencyDecimal().intValue(), 
							currency.getCurrencyRound().intValue()); // 小数点以下桁数設定
		}
		
		return bdProfitLoss;
	}




	private void updateSysPosition(JhfAliveExecution execution,
			JhfWlOrder jhfWlOrder) throws CoreException {
		dao.updateSysPositionPrice(execution,jhfWlOrder);
		
	}


	private void updateHedgeCusttrade(JhfAliveExecution execution,
			JhfWlOrder jhfWlOrder) throws CoreException {
		dao.updateHedgeCusttradePrice(execution,jhfWlOrder);
		
	}


//	private void updateContractBind(JhfAliveOrder aliveOrder,
//			JhfWlOrder jhfWlOrder) {
//		// TODO Auto-generated method stub
//		dao.updateContractBindSpotPl();
//	}


	private void updateAliveExecution(JhfAliveExecution execution,
			JhfWlOrder jhfWlOrder) throws DaoException {
		execution.setExecutionPrice(jhfWlOrder.getExecutionPrice());
		execution.setRevisionNumber(execution.getRevisionNumber().add(BigDecimal.ONE) );
		dao.update(execution);
		
	}


	private void updateAliveOrder(JhfAliveOrder aliveOrder,
			JhfWlOrder jhfWlOrder) throws DaoException {
		/** 更新updateDate，RevisionNumber **/
		
//		aliveOrder.setChangeReason(new BigDecimal(ChangeReasonEnum.ORDER_REASON_REVERSE_ENUM.getValue()) );
		aliveOrder.setChangeReason(new BigDecimal( 99 ) );
		aliveOrder.setRevisionNumber(aliveOrder.getRevisionNumber() + 1);
		
		aliveOrder.setTradePrice(jhfWlOrder.getExecutionPrice());
		aliveOrder.setExecutionPrice(jhfWlOrder.getExecutionPrice());
		dao.save(aliveOrder);
		
	}


	private boolean isAdminUpdate(CompletorOrderInfo info) {
		if(info.getJhfWlOrder().getCompletionStatus().intValue() == 
			CompletionStatusEnum.CORRECTED.getValue()){
			return true;
		}
		return false;
	}
	
	private void nettingOrder(CompletorOrderInfo order) throws Exception {

		List<JhfAliveContract> posList = queryPosList(
				order.getJhfWlOrder().getCustomerId(),
				order.getJhfWlOrder().getCurrencyPair(),
				order.getJhfWlOrder().getSide().negate());
		
		if( ! CollectionUtils.isEmpty(posList)){ 
			net(order, posList);
		}else{
			open(order);
		}

	}


	private List<JhfAliveContract> queryPosList(String customerId,String ccy, BigDecimal side) throws Exception {
		return ns.findNotSettledContracts(customerId, ccy);
	}

	
	private void open(CompletorOrderInfo order) throws Exception {
		try {
			JhfWlOrder jhfWlOrder = order.getJhfWlOrder();
			BigDecimal amount = jhfWlOrder.getOrderAmount().subtract( jhfWlOrder.getCompletionAmount() );
			jhfWlOrder.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal()); /* FINISHED已处理 */
			jhfWlOrder.setCompletionAmount(jhfWlOrder.getOrderAmount());
			
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			updateJhfWLOrder(jhfWlOrder);
			open(order.getJhfWlOrder(),amount);
			
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		} catch (Exception e) {
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
//			ignMap.putIfAbsent(order.getJhfWlOrder().getClientId() + order.getJhfWlOrder().getCurrencyPair(),new Object());
			ignMap.put(order.getKey(),order.getKey());
			log.error("save open order error !",e);
			throw e;
		}
		
	}

	private void net(CompletorOrderInfo orderInfo,List<JhfAliveContract> posList) throws Exception{
		
		JhfWlOrder jhfWlOrder = orderInfo.getJhfWlOrder();
		log.info("Start Net .order={}",jhfWlOrder);

		log.info("[net] JhfWlOrder OrderAmount:{} ,completionAmount:{}",jhfWlOrder.getOrderAmount(),jhfWlOrder.getCompletionAmount());
		BigDecimal amount = jhfWlOrder.getOrderAmount().subtract( jhfWlOrder.getCompletionAmount() );	
		log.info("Net Amount : {}",amount);
		
		int size = 	posList.size();			
		for (int i = 0; i < size ; i++) {
			try {
				if(amount.compareTo(BigDecimal.ZERO) <= 0 ){
					log.info("No More Amount Left To Completor ,Order={}",jhfWlOrder);
					return;
				}
				if(ignMap.containsKey(orderInfo.getKey())){
					break;
				}
				
				DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
				
				JhfAliveContract contract = posList.get(i);
				BigDecimal netAmount = BigDecimal.ZERO;

				if (amount.compareTo(contract.getAmountNoSettled()) > 0 ) {																																																		
					netAmount = contract.getAmountNoSettled();
					jhfWlOrder.setCompletionStatus(CompletionStatusEnum.PROGRESSING.getValueAsBigDecimal()) ;  /* PROGRESSING处理中 */																																																		
					jhfWlOrder.setCompletionAmount(jhfWlOrder.getCompletionAmount().add(netAmount));																																																		
					updateJhfWLOrder(jhfWlOrder);
					
//					contract.setAmountSettled(contract.getAmountSettled().add(netAmount));
//					contract.setAmountNoSettled(BigDecimal.ZERO);
					settle(contract,jhfWlOrder,netAmount);
					
					amount = amount.subtract(contract.getAmountNoSettled());
					
				} else {	
					netAmount = amount ;
					jhfWlOrder.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal());  /* FINISHED已处理*/																																																		
					jhfWlOrder.setCompletionAmount(jhfWlOrder.getCompletionAmount().add(netAmount));																																																		
					updateJhfWLOrder(jhfWlOrder);
					
//					contract.setAmountSettled(contract.getAmountSettled().add(netAmount));
//					contract.setAmountNoSettled(contract.getAmountNoSettled().subtract(netAmount));
					settle(contract,jhfWlOrder,netAmount);																																																		
					amount = BigDecimal.ZERO;																																																																																								
				}
				
				if (amount.compareTo(BigDecimal.ZERO) > 0) {
					open(jhfWlOrder,amount);
					jhfWlOrder.setCompletionStatus(CompletionStatusEnum.FINISHED.getValueAsBigDecimal()); /* FINISHED已处理 */
					jhfWlOrder.setCompletionAmount(jhfWlOrder.getCompletionAmount().add(amount));
					updateJhfWLOrder(jhfWlOrder);
				}
				
				DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
				
			} catch (Exception e) {
				
				DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
				log.error(" ", e);
				ignMap.put(orderInfo.getKey(),orderInfo.getKey());
//				break;
				throw e;
			}
		}
				
	}


	private JhfWlOrderBind createJhfWlOrderBind(JhfAliveOrder jhfAliveOrder, JhfWlOrder jhfWlOrder, JhfAliveExecution excutionBean) {
		JhfWlOrderBind jwb = new JhfWlOrderBind();
		JhfWlOrderBindId id = new JhfWlOrderBindId();
		
		id.setOrderId(jhfAliveOrder.getOrderId());;
		id.setWlOrderId(jhfWlOrder.getWlOrderId());
		
		jwb.setId(id);
		jwb.setActiveFlag(new BigDecimal( BoolEnum.BOOL_YES_ENUM.getValue()));
		jwb.setAllocateAmount(jhfAliveOrder.getOrderAmount());
//		jwb.setAllocateAmount(excutionBean.getExecutionAmount());   ?????????????
		jwb.setExecutionId(excutionBean.getExecutionId());
		jwb.setTradeType(jhfAliveOrder.getTradeType());
		jwb.setInputStaffId(jhfAliveOrder.getInputStaffId());
		jwb.setUpdateStaffId(jhfAliveOrder.getInputStaffId());
//		jwb.setInputDate(Date);
//		jwb.setUpdateDate(Date);
		return jwb;
	}

	private cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder createJhfAliveOrder(
			JhfWlOrder jhfWlOrder, BigDecimal amount) throws Exception {
		String productId = ns.findProductId(jhfWlOrder.getCustomerId(),jhfWlOrder.getCurrencyPair());
		log.info(" product Id :{}",productId);
		return OrderInfoFactory.getInstance().converToJhfAliveOrder(amount,productId,jhfWlOrder);
	}

	/**
	 * 
	 * 1.	插入JHF_ALIVE_ORDER
	 * 2.	插入JHF_ALIVE_EXECUTION
	 * 3.	插入JHF_ALIVE_CONTRACT
	 * 4.	插入JHF_WL_ORDER_BIND
	 * 
	 * @param jhfWlOrder
	 * @param amount 
	 * @throws Exception
	 */
	private void open(JhfWlOrder jhfWlOrder, BigDecimal amount) throws Exception {
		//插入JHF_ALIVE_ORDER
//		log.info("JhfWlOrder OrderAmount:{} ,completionAmount:{}",jhfWlOrder.getOrderAmount(),jhfWlOrder.getCompletionAmount());
		JhfAliveOrder jhfAliveOrder = createJhfAliveOrder(jhfWlOrder,amount);
//		log.info("JhfAliveOrder OrderAmount:{}",jhfAliveOrder.getOrderAmount());
		jhfAliveOrder.setChangeReason(new BigDecimal(ChangeReasonEnum.ORDER_REASON_BUILD_ENUM.getValue()));
		netterDao.save(jhfAliveOrder);
		log.info("[OPEN] Save JhfAliveOrder {}",jhfAliveOrder);
		FillingOrderInfo fillingOrderInfo = OrderInfoFactory.getInstance().convertToFillOrderInfo(jhfAliveOrder);
		PositionInfo posInfo = PositionInfoFactory.getInstance().createPositionInfo(
				fillingOrderInfo,
				jhfWlOrder.getExecutionDate(),
				jhfWlOrder.getExecutionPrice(),
				false,null,
				CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue(),
				StayTypeEnum.STAY_TYPE_1_ENUM.getValue()
				);
		String settledate = ServiceFactory.getCoreService().getSettleDate(jhfWlOrder.getCurrencyPair(),jhfWlOrder.getExecutionDate());
//		log.info("settledate:"+settledate);
		posInfo.setSettleDate(settledate);
		posInfo.setOriginalSettleDate(settledate);
		//插入JHF_ALIVE_EXECUTION
		JhfAliveExecution excutionBean = ExecutionFactory.getInstance().create(fillingOrderInfo,posInfo,null,null);
		tradeDao.storeExecution(excutionBean);
		log.info("[OPEN] Save JhfAliveExecution {}",excutionBean);
		//插入JHF_ALIVE_CONTRACT
		JhfAliveContract contractBean = ContractFactory.getInstance().create(excutionBean, posInfo, 
				settledate, fillingOrderInfo);
		contractBean.setChangeReason(new BigDecimal(ContractChangeReasonEnum.CONTRACT_REASON_OPEN_ENUM.getValue()));
		tradeDao.storeContract(contractBean);
		log.info("[OPEN] Save JhfAliveContract {}",contractBean);
		//插入JHF_WL_ORDER_BIND
		JhfWlOrderBind jhfWlOrderBind = createJhfWlOrderBind(jhfAliveOrder,jhfWlOrder,excutionBean);
		netterDao.save(jhfWlOrderBind);
		log.info("[OPEN] Save JhfWlOrderBind {}",jhfWlOrderBind);
		log.info("COMPLETOR OPEN ORDER FINISHED  orderId={}",jhfWlOrder.getWlOrderId());
	}

	/**
	 * 
	 * 
	 * 1.	插入JHF_ALIVE_ORDER
	 * 2.	插入JHF_ALIVE_EXECUTION
	 * 3.	锁住待更新的JHF_ALIVE_CONTRACT
	 * 4.	插入JHF_CONTRACT_BIND
	 * 5.	更新JHF_ALIVE_CONTRACT
	 * 6.	插入JHF_UNREALIZED_CASHFLOW
	 * 7.	插入JHF_WL_ORDER_BIND

	 * 
	 * @param contract
	 * @param jhfWlOrder 
	 * @throws Exception 
	 */
	private void settle(JhfAliveContract contract, JhfWlOrder jhfWlOrder,BigDecimal netAmount) throws Exception {
		//插入JHF_ALIVE_ORDER
		JhfAliveOrder jhfAliveOrder = createJhfAliveOrder(jhfWlOrder,netAmount);
		jhfAliveOrder.setChangeReason(new BigDecimal(ChangeReasonEnum.ORDER_REASON_EXECUTED_ENUM.getValue()));
		netterDao.save(jhfAliveOrder);
		log.info("[SETTLE] Save JhfAliveOrder {}",jhfAliveOrder);
		FillingOrderInfo fillingOrderInfo = OrderInfoFactory.getInstance().convertToFillOrderInfo(jhfAliveOrder);
		PositionInfo posInfo = PositionInfoFactory.getInstance().createPositionInfo(
				fillingOrderInfo,
				jhfWlOrder.getExecutionDate(),
				jhfWlOrder.getExecutionPrice(),
				false,null,
				CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue(),
				StayTypeEnum.STAY_TYPE_1_ENUM.getValue()
				);
		String settledate = ServiceFactory.getCoreService().getSettleDate(jhfWlOrder.getCurrencyPair(),jhfWlOrder.getExecutionDate());
//		log.info("settledate:"+settledate);
		posInfo.setSettleDate(settledate);
		posInfo.setOriginalSettleDate(settledate);
		
		BigDecimal bdRate = getSpotRate(posInfo.getCurrencyPair(), posInfo.getSpot(),false);
        // createCashflow
        Vector vtrRows =  createCashflow( posInfo, false, bdRate);
        // 取得cash flow金额
        BigDecimal  bdJpySpot = (BigDecimal)vtrRows.elementAt(0);
        BigDecimal  bdJpySwap = (BigDecimal)vtrRows.elementAt(1);
		//插入JHF_ALIVE_EXECUTION
		JhfAliveExecution excutionBean = ExecutionFactory.getInstance().create(
				OrderInfoFactory.getInstance().convertToFillOrderInfo(jhfAliveOrder),posInfo,bdRate,null);
		tradeDao.storeExecution(excutionBean);
		log.info("[SETTLE] Save JhfAliveExecution {}",excutionBean);
		//锁住待更新的JHF_ALIVE_CONTRACT
		JhfAliveContract contractBean = tradeDao.getContractUseUpgradeLock(contract.getContractId());
		contractBean.setAmountSettling(netAmount);
		// 计算swap损益和spot损益	
		String sCurrencyCode = CurrencyHelper.getCounterCurrencyCode(contractBean.getCurrencyPair());
		JhfCurrency currencyBean = ServiceFactory.getConfigService().getCurrency(sCurrencyCode);
		posInfo = calcSwapProfitLoss(jhfAliveOrder, posInfo, contractBean, currencyBean);
		posInfo = calcSpotProfitLoss(jhfAliveOrder, posInfo, contractBean, currencyBean);

		
		JhfAliveContract jac = MiddleDAOFactory.getTradeDao().executeSettleOrderToContract(
				OrderInfoFactory.getInstance().convertToFillOrderInfo(jhfAliveOrder), 
        		excutionBean, 
        		contractBean, 
        		posInfo, bdJpySpot, bdJpySwap,true);
		
		log.info("[SETTLE] Save JhfAliveContract {}",jac);
		
		//插入JHF_WL_ORDER_BIND
		JhfWlOrderBind jhfWlOrderBind = createJhfWlOrderBind(jhfAliveOrder,jhfWlOrder,excutionBean);
		netterDao.save(jhfWlOrderBind);
		log.info("[SETTLE] Save JhfWlOrderBind {}",jhfWlOrderBind);
		log.info("COMPLETOR SETTLE ORDER FINISHED  orderId={}",jhfWlOrder.getWlOrderId());
		
	}
	
	protected Vector createCashflow(PositionInfo pdt, boolean lessThanOneLot, BigDecimal bdRate)
	throws TradeException {

		log.info(" CreateCashflow() Begin . orderId=" + pdt.getOrderId());
		Vector<BigDecimal> vtrReturn = new Vector<BigDecimal>();
		try {
			log.info(" Begin Query CounterCcy getCounterCurrencyCode() . orderId="+ pdt.getOrderId());
			// 対価通貨
			String sCounterCcy = CurrencyHelper.getCounterCurrencyCode(pdt.getCurrencyPair());
			log.info(" After Query CounterCcy getCounterCurrencyCode() . orderId="+ pdt.getOrderId());

			Timestamp toToday = DateHelper.getTodaysTimestamp();

			log.info(" Get SettleDate Begin . orderId=" + pdt.getOrderId());
			// /** 郭永胜 提出的更改 */
			// String sValueDate =
			// m_coreService.getSettleDate(pdt.getCurrencyPair());
			log.info(" After Get SettleDate . orderId=" + pdt.getOrderId());

			// BigDecimal bdJpyAmount = new BigDecimal("0");

			// 决济损益部分
			CashflowInfo cashflowInfo = new CashflowInfo();
			cashflowInfo.setValueDate(pdt.getOriginalSettleDate());
			cashflowInfo.setProductId(pdt.getProductId());
			cashflowInfo.setCustomerId(pdt.getCustomerId());
			cashflowInfo.setEventDate(pdt.getExecutionDate());
			cashflowInfo.setEventDateTime(toToday);
			cashflowInfo.setSourceId(pdt.getExecutionId());

			log.info(" Begin  create spot cashflow ." + pdt.getOrderId());
			// 如果是决济(或是日决济)
			// if (iType == TradeTypeEnum.TRADE_SETTLE_ENUM.getValue()
			// || iType == TradeTypeEnum.TRADE_DAILY_SETTLE_ENUM.getValue()) {

			cashflowInfo.setCurrencyCode(COUNTERCCY_JPY);
			cashflowInfo.setAmountOriginal(pdt.getSpot());
			cashflowInfo.setCurrencyCodeOriginal(sCounterCcy);

			log.info("Set cashflowId in cashflowInfo . orderId="
					+ pdt.getOrderId());
			if (StringUtils.isNotEmpty(pdt.getCashflowIdSpot())) {
				cashflowInfo.setCashflowId(pdt.getCashflowIdSpot());
			} else {
				log.warn(" position's CashflowIdSpot is NULL .orderId="+ pdt.getOrderId());
				cashflowInfo.setCashflowId(IdGenerateFacade.getCashflowId());
				log.info(" cashflowIdSpotRetry="+ cashflowInfo.getCashflowId() + ".orderId="+ pdt.getOrderId());
			}
			// 设置spot损益类型
			cashflowInfo.setCashflowType(CashflowTypeEnum.CASHFLOW_SPOT_ENUM.getValue());
			// 设置从ratecache中取得的汇率(流动资金发生时的换算汇率)
			cashflowInfo.setRate(bdRate);
			// cash flow金额
			BigDecimal bdJpyAmount = evalYen(COUNTERCCY_JPY, pdt.getSpot(),bdRate);
			cashflowInfo.setAmount(bdJpyAmount);

			cashflowInfo.setBalance(null);
			log.info(" Save SPOT cashflow to DB begin . orderId="+ pdt.getOrderId());
			cashflowDao.createCashflow(CashflowFactory.getInstance().create(cashflowInfo, pdt));
			log.info(" Save SPOT cashflow to DB over  . orderId="+ pdt.getOrderId());
			// 增加spot汇率
			vtrReturn.addElement(bdJpyAmount);
			// }

			log.info(" After  create spot cashflow ." + pdt.getOrderId());

			// swap损益部分

			if (pdt.getSwap().compareTo(BigDecimal.ZERO) != 0) {

				if (StringUtils.isNotEmpty(pdt.getCashflowIdSwap())) {
					cashflowInfo.setCashflowId(pdt.getCashflowIdSwap());
				} else {
					log.warn(" position's CashflowIdSwap is NULL .orderId="+ pdt.getOrderId());
					cashflowInfo.setCashflowId(IdGenerateFacade.getCashflowId());
					log.info(" cashflowIdSwapRetry="+ cashflowInfo.getCashflowId() + ".orderId="+ pdt.getOrderId());
				}

				cashflowInfo.setCashflowType(CashflowTypeEnum.CASHFLOW_SWAP_ENUM.getValue());
				cashflowInfo.setAmountOriginal(pdt.getSwap());

				/**
				 * 如果是#1 JPY为单位，则不需要将swap根据最新汇率进行兑换，然后写入 cashflow;如果是#2
				 * CCY2为单位，则需要进行CCY2/JPY的兑换。
				 */
				log.info(" Begin Query CurrencyPair getCounterCurrencyCode() .orderId="+ pdt.getOrderId());
				JhfCurrencyPair jhfCurrencyPair = ServiceFactory.getConfigService().getJhfCurrencyPair(pdt.getCurrencyPair());
				log.info(" After Query CurrencyPair getCounterCurrencyCode() .orderId="+ pdt.getOrderId());

				if (jhfCurrencyPair != null
						&& jhfCurrencyPair.getSwapUnit().intValue() == SwapUnitEnum.JPY_ENUM
								.getValue()) {
					cashflowInfo.setRate(new BigDecimal("1"));
					cashflowInfo.setCurrencyCodeOriginal(COUNTERCCY_JPY);
				} else {
					cashflowInfo.setRate(bdRate);
					cashflowInfo.setCurrencyCodeOriginal(sCounterCcy);
				}
				// cash flow金额
				bdJpyAmount = evalYen(COUNTERCCY_JPY, pdt.getSwap(),cashflowInfo.getRate());
				cashflowInfo.setAmount(bdJpyAmount);
				cashflowInfo.setBalance(null);

				log.info(" Save SWAP cashflow to DB begin . orderId="+ pdt.getOrderId());
				cashflowDao.createCashflow(CashflowFactory.getInstance().create(cashflowInfo, pdt));
				log.info(" Save SWAP cashflow to DB over  . orderId="+ pdt.getOrderId());
				
			} else {
				log.info("Swap is Zero .orderId=" + pdt.getOrderId());
				bdJpyAmount = BigDecimal.ZERO;
			}
			// 增加swap汇率
			vtrReturn.addElement(bdJpyAmount);
			
			//losscut 手续费
//			if(pdt.getExecutionType() == ExecutionTypeEnum.EXEC_LOSS_CUT_ENUM.getValue()){
//			if(pdt.getSettleCommission().compareTo(BigDecimal.ZERO) != 0 ){
//				
//				if( StringUtils.isNotEmpty( pdt.getCashflowIdSettleCommission() ) ){
//					cashflowInfo.setCashflowId(pdt.getCashflowIdSettleCommission());
//				}else{
//					log.warn(" position's CashflowIdSettleCommission is NULL .orderId="+pdt.getOrderId());
//					cashflowInfo.setCashflowId( IdGenerateFacade.getCashflowId());
//					log.info(" CashflowIdSettleCommissionRetry="+ cashflowInfo.getCashflowId() +".orderId="+pdt.getOrderId());
//				}
//				
//				cashflowInfo.setCashflowType(CashflowTypeEnum.CASHFLOW_SETTLE_COMMISSION_ENUM.getValue());
//				cashflowInfo.setAmountOriginal(pdt.getSettleCommission().negate());
//				cashflowInfo.setRate(new BigDecimal("1"));
//				cashflowInfo.setCurrencyCodeOriginal(COUNTERCCY_JPY);
//
//				// cash flow金额
////				bdJpyAmount = evalYen(COUNTERCCY_JPY,pdt.getSettleCommission(), cashflowInfo.getRate());
//				cashflowInfo.setAmount(pdt.getSettleCommission().negate());
//				cashflowInfo.setBalance(null);
//				
//				log.info(" Save losscut commission cashflow to DB begin . orderId="+pdt.getOrderId());
//				m_cashflowDao.createCashflow(CashflowFactory.getInstance().create(cashflowInfo,pdt));
//				log.info(" Save losscut commission cashflow to DB over  . orderId="+pdt.getOrderId());
//			}
			//手续费cashflow
//			if(pdt.getExecutionType() == ExecutionTypeEnum.EXEC_LOSS_CUT_ENUM.getValue()){
//
//				if(lessThanOneLot){
//					//0.1lot 设置手续费
//					createOpenCommission(pdt,	cashflowInfo);
//					//product中losscut手续费不为0，则设置手续费为 LOSSCUT_SETTLE 类型，否则设置为 通常手续费类型
//					if(pdt.getSettleCommission().compareTo(BigDecimal.ZERO) != 0 ){
//						createSettleCommission(pdt,cashflowInfo ,
//								BigDecimal.valueOf( SettleCommissionTypeEnum.LOSSCUT_SETTLE_COMMISSION_ENUM.getValue() ));
//					}else{
//						createSettleCommission(pdt, cashflowInfo ,
//								BigDecimal.valueOf( SettleCommissionTypeEnum.NORMAL_SETTLE_COMMISSION_ENUM.getValue() ));
//					}
//				}else{
//					createSettleCommission(pdt,cashflowInfo ,
//							BigDecimal.valueOf( SettleCommissionTypeEnum.LOSSCUT_SETTLE_COMMISSION_ENUM.getValue() ));
//				}
//				
//			}else if(lessThanOneLot){
//					
//				createOpenCommission(pdt,	cashflowInfo);
//				createSettleCommission(pdt, cashflowInfo ,
//						BigDecimal.valueOf( SettleCommissionTypeEnum.NORMAL_SETTLE_COMMISSION_ENUM.getValue() ));
//			}
			log.info(" CreateCashflow() over . orderId=" + pdt.getOrderId());

		} catch (Exception e) {
			throw new TradeException(e);
		}

		return vtrReturn;
	}
	
	private void updateJhfWLOrder(JhfWlOrder jhfWlOrder) throws DaoException {
		dao.update(jhfWlOrder);
		log.info("[SETTLE] UPDATE JhfWLOrder {}",jhfWlOrder);
	}
	

	public SchedulerExecutor getScheduler() {
		return scheduler;
	}

	public void setScheduler(SchedulerExecutor scheduler) {
		this.scheduler = scheduler;
	}
	/**
	 * <pre>
	 * 计算swap损益的函数，swap损益的计算方法为：
	 * 
	 * (1).计算本次决济时的swap值，swap计算公式为：
	 * swap = swap利息*本次决济注文数/未决济数量
	 * (2).计算总新规手续费：
	 * 总新规手续费 ＝ 单笔新规手续费×决济总注文数/未决济数量
	 * (3).计算决济手续费：
	 * 总决济手续费 ＝ 单笔决济手续费×决济总注文数/未决济数量
	 * </pre>
	 * @param order
	 * @param position
	 * @param contract
	 * @param currency
	 * @return 包含swap损益值、新规手续费、决济手续费等属性的 PositionInfo类实例
	 * @author zuolin <zuolin@bestwiz.cn>
	 * @see #calcSpotProfitLoss(OrderInfo, PositionInfo, JHFContract, JHFCurrency)
	 */
	private PositionInfo calcSwapProfitLoss(JhfAliveOrder jhfAliveOrder,
			PositionInfo position, JhfAliveContract contract, JhfCurrency currency) throws Exception {
		
		if (jhfAliveOrder == null || contract == null) {
			return position;
		}
		
		try {
			BigDecimal bdAmountOrg = contract.getAmount(); // 原来的数量
			BigDecimal bdAmountSettled = contract.getAmountSettled(); // 已决济的数量
			
			// 未决济数量（包括决济注文中）
			BigDecimal bdAmountNoSettled = bdAmountOrg.subtract(bdAmountSettled);
			// 此次决济的数量
			BigDecimal bdAmount = jhfAliveOrder.getOrderAmount();
			// SWAPPOIT累计
			BigDecimal bdSwapTotal = contract.getSwapInterest();
			// 新规手续费
//===			BigDecimal bdOpenCommissionTotal = contract.getOpenCommission();
			// 决计手续费
//===			BigDecimal settleCommissionTotal = contract.getSettlementCommission() ;
		
			// 计算本次决济时的swap值，swap计算公式为：(swap = swap利息*本次决济注文数/未决济数量)
			BigDecimal bdSwap = bdSwapTotal.multiply(bdAmount);

			bdSwap = bdSwap.divide(bdAmountNoSettled, currency.getCurrencyDecimal().intValue(),currency.getCurrencyRound().intValue());
			position.setSwap(bdSwap);
			
			// 计算总新规手续费：(总新规手续费 ＝ 单笔新规手续费×决济总注文数/未决济数量)
//===			BigDecimal dbOpenComm = bdOpenCommissionTotal.multiply(bdAmount);
			
//===			dbOpenComm = dbOpenComm.divide(bdAmountNoSettled,  currency.getCurrencyDecimal().intValue(),currency.getCurrencyRound().intValue());
			
			// 计算决济手续费: (总决济手续费 ＝ 单笔决济手续费×决济总注文数/未决济数量)
//===			BigDecimal dbSettleComm = settleCommissionTotal.multiply(bdAmount);

//===			dbSettleComm= dbSettleComm.divide(bdAmountNoSettled,  currency.getCurrencyDecimal().intValue(),currency.getCurrencyRound().intValue());
			
			// 手数料は日本円。円未満切り捨て
//===			position.setCalcOpenCommission(dbOpenComm);
//===			position.setSettleCommission(dbSettleComm);
			return position;
		} catch (Exception e) {
			log.error("PAS calcSwapProfitLoss Errors : ",e);
			throw e;
		}

	}
	
	/**
	 * <pre>
	 * 计算spot损益的函数，计算公式为：
	 * （用户卖的价格－用户买入时的价格）×决济的总数量
	 * </pre>
	 * 
	 * @param order
	 * @param position
	 * @param contract
	 * @param currency
	 * @return 包含spot损益值的 PositionInfo类实例
	 * @author zuolin <zuolin@bestwiz.cn>
	 * @see #calcSwapProfitLoss(OrderInfo, PositionInfo, JHFContract, JHFCurrency)
	 */
	private PositionInfo calcSpotProfitLoss(JhfAliveOrder jhfAliveOrder,
			PositionInfo position, JhfAliveContract contract, JhfCurrency currency) throws TradeException {
		// 建値(约定价格)
		BigDecimal bdContractPrice = contract.getExecutionPrice(); 
		// 约定价格(交易价格)
		BigDecimal bdExecutionPrice = position.getPrice();        
		// 数量
		BigDecimal bdAmount = jhfAliveOrder.getOrderAmount(); 

		
		BigDecimal bdProfitLoss = null; // 损益
		
		if (jhfAliveOrder.getSide().intValue() == SideEnum.SIDE_SELL.getValue()) {        // 卖的决济注文(相返为买)
			bdProfitLoss = bdExecutionPrice.subtract(bdContractPrice);
		} else if (jhfAliveOrder.getSide().intValue() == SideEnum.SIDE_BUY.getValue()) { // 买的决济注文(相返为买卖)
			bdProfitLoss = bdContractPrice.subtract(bdExecutionPrice);
		}
		
		bdProfitLoss = bdProfitLoss.multiply(bdAmount);  // １通貨あたりの損益×数量
		if (currency != null) {
			bdProfitLoss = bdProfitLoss.setScale(currency
					.getCurrencyDecimal().intValue(), 
							currency.getCurrencyRound().intValue()); // 小数点以下桁数設定
		}
		
		position.setSpot(bdProfitLoss); // spot损益的设置
		return position;
	}
	
	/**
	 * 取得换算后的汇率
	 * @param sCurrencyPair - trade currrencyPair
	 * @return  换算后的货币对的汇率
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	private BigDecimal getSpotRate(String sCurrencyPair, BigDecimal bdSpot, boolean bOpen) throws Exception {
		BigDecimal bdReturn = null;
		//1. 到得换算货币对
		String sCounterCcy = CurrencyHelper.getCounterCurrencyCode(sCurrencyPair);
		
		//2. 如果取得的换算货币对为日元,返回1
		if (sCounterCcy.equals(COUNTERCCY_JPY)) {
			return new BigDecimal("1");
		}
		//3. 将取得的货币换算成日元
		String sSpotRateCcyPair = sCounterCcy + "/" + COUNTERCCY_JPY;
		//4. 从rateCache中取得换算后的货币对
		FxSpotRateInfo fxSpotRateInfo = null;
		fxSpotRateInfo = RateCacheFactory.getRatecache().getFxSpotRate(sSpotRateCcyPair);
		if(fxSpotRateInfo == null) {
			log.error("***** custSpotRateInfo Was Null *****");
			throw new Exception("RateCache.getCustomerRate Is Null!!!!" + sSpotRateCcyPair);
		}
		//5. 新式样要求转换日元时使用評価汇率。外币的益转换为日元时使用bid、外币的损用日元填写时使用ask。
		if (bOpen) {
			bdReturn = fxSpotRateInfo.getBidRate();
			return bdReturn;
		}
		
		if (bdSpot.compareTo(new BigDecimal("0")) < 0) {
			bdReturn = fxSpotRateInfo.getAskRate();
		} else {
			bdReturn = fxSpotRateInfo.getBidRate();
		}
		return bdReturn;
	}
	/**
	 * 计算Spot * Rate
	 * @param BigDecimal  (bdVal)
	 * @param BigDecimal  (bdRate)
	 * @return  Spot * Rate
	 * @author zuolin <zuolin@bestwiz.cn>
	 */
	private BigDecimal evalYen(String sCurrency, BigDecimal bdVal, BigDecimal bdRate) throws Exception {
		BigDecimal bdAmount = bdVal;
		BigDecimal bdValue = bdAmount.multiply(bdRate);
		bdValue = RateCalcHelpers.caleCurrencyRound(sCurrency, bdValue) ;
		return bdValue;
	}
	
//
//	private void prepare() {
//		
//		BufferedReader br = new BufferedReader(new InputStreamReader(Completor.class.getResourceAsStream("QKeyPackage.xml")));
//		XStream xstream = new XStream(new DomDriver());
//		
//		xstream.processAnnotations(QKeyPackage.class);
//		
//		QKeyPackage quque = (QKeyPackage)xstream.fromXML(br );
//		try {
//			br.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println(quque.getqCount());
//		System.out.println(quque.getMap().get("USD/JPY"));
//		System.out.println(quque.getMap().get("EUR/JPY"));
//		System.out.println(quque.getMap().get("NZD/JPY"));
//		
//		qp = quque;
//		
//	}
	
	public static void main(String[] args) {
/*		BufferedReader br = new BufferedReader(new InputStreamReader(Completor.class.getResourceAsStream("QKeyPackage.xml")));
		XStream xstream = new XStream(new DomDriver());
		
		xstream.processAnnotations(QKeyPackage.class);
		
		QKeyPackage quque = (QKeyPackage)xstream.fromXML(br );
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(quque.getqCount());
		System.out.println(quque.getMap().get("USD/JPY"));
		System.out.println(quque.getMap().get("EUR/JPY"));
		System.out.println(quque.getMap().get("NZD/JPY"));*/
	}


	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
