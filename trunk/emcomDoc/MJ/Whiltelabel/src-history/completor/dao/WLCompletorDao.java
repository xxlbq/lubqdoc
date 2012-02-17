package jp.emcom.adv.fx.completor.dao;

import java.math.BigDecimal;
import java.util.List;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.commons.bo.enums.OrderStatusEnum;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.exceptions.DaoException;
import cn.bestwiz.jhf.core.dao.BaseMainDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContractBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveExecution;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBind;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.CoreException;
import cn.bestwiz.jhf.core.util.CollectionsUtil;
import cn.bestwiz.jhf.core.util.DateHelper;

public class WLCompletorDao extends BaseMainDao{
	
	private static final Logger log = LoggerFactory.getLogger(WLCompletorDao.class);
	

	/**
	 * @param jhfAliveOrder
	 * @throws DaoException
	 */
	public void saveJhfAliveOrder(
			JhfAliveOrder jhfAliveOrder) throws DaoException {
		save(jhfAliveOrder);
	}



	/**
	 * @param jhfWlOrderBind
	 * @throws DaoException
	 */
	public void saveWlOrderBind(JhfWlOrderBind jhfWlOrderBind) throws DaoException {
		try {
			save(jhfWlOrderBind);
		} catch (DaoException e) {
			log.error("save JhfWlOrder error! order={}" ,jhfWlOrderBind, e);
			throw e;
		}
		
	}

	
	@SuppressWarnings("unchecked")
	public List<JhfAliveContract> queryContract(String customerId, String ccy,BigDecimal side) {
		
		StringBuffer sb = new StringBuffer();
        sb.append(" FROM  JhfAliveContract ");
        sb.append(" Where  "); 
        sb.append(" 	  customerId = :varCustomerId "); 
        sb.append(" AND   currencyPair = :varCurrencyPair ");
        sb.append(" AND   side = :varSide ");
        sb.append(" AND   amountNoSettled > 0   ");
        sb.append(" AND   status = 1              ");  
        sb.append(" And   activeFlag = :varActiveFlag ");
        
        Query query = getSession().createQuery(sb.toString());

		query.setString("varCustomerId",  	customerId);
		query.setString("varCurrencyPair",  ccy);
		query.setBigDecimal("varSide",  side);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));

		return (List<JhfAliveContract>)query.list();
	}

	
	/**
	 * @param customerId
	 * @return
	 */
	public String queryProductId(String customerId,String ccy) {
		StringBuffer sb = new StringBuffer();
        sb.append(" SELECT PRODUCT_ID FROM  JHF_GROUP_DEFAULT_PRODUCT G,JHF_CUSTOMER_STATUS C  ");
        sb.append(" WHERE G.CURRENCY_PAIR = :varCCY AND G.GROUP_ID = C.GROUP_ID AND C.CUSTOMER_ID =:varCustomerId   "); 
        sb.append(" 	  AND G.ACTIVE_FLAG =:varGroupActiveFlag  AND C.ACTIVE_FLAG = :varCustomerActiveFlag "); 
        
        Query query = getSession().createSQLQuery(sb.toString());

		query.setString("varCustomerId",  	customerId);
		query.setString("varCCY",  	ccy);
		query.setBigDecimal("varGroupActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setBigDecimal("varCustomerActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (String)query.uniqueResult();
	}

	

	@SuppressWarnings("unchecked")
	public List<JhfWlOrder> queryCompletorOrder(List<String>  customerIds) {
		
		StringBuffer sb = new StringBuffer();
        sb.append(" FROM JhfWlOrder ORD ");
        sb.append(" WHERE ORD.orderStatus = :varOrderStatus AND  "); 
        sb.append("       ORD.completionStatus IN (:varCompletionStatus1 ,:varCompletionStatus2 , :varCompletionStatus4 ,:varCompletionStatus5 )    ");
        
        if(CollectionsUtil.isNotEmpty(customerIds)){
        	sb.append(" 	  AND ORD.customerId IN (:varCids) "); 
        }
        
        sb.append(" 	  AND ORD.activeFlag =:varActiveFlag  ORDER BY  ORD.inputDate , ORD.wlOrderId "); 
        
        Query query = getSession().createQuery(sb.toString());

		query.setString("varOrderStatus",  	OrderStatusEnum.FILLED.getValue());
		query.setBigDecimal("varCompletionStatus1",  	CompletionStatusEnum.PENDING.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus2",  	CompletionStatusEnum.PROGRESSING.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus4",  	CompletionStatusEnum.ERROR.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus5",  	CompletionStatusEnum.CORRECTED.getValueAsBigDecimal());
		
		if(CollectionsUtil.isNotEmpty(customerIds)){
			
			query.setParameterList("varCids", customerIds);
        }
		
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
//		log.info("final SQL :"+query.getQueryString());
		return (List<JhfWlOrder>)query.list();
	}


	@SuppressWarnings("unchecked")
	public List<JhfAliveOrder> queryAliveOrder(JhfWlOrder jhfWlOrder) {
		StringBuffer sb = new StringBuffer();
        sb.append(" FROM JhfAliveOrder ALIVE_ORDER ");
        sb.append(" WHERE ALIVE_ORDER.orderId  IN (  "); 
        sb.append("        SELECT WL_ORDER_BIND.id.orderId FROM JhfWlOrderBind WL_ORDER_BIND  "); 
        sb.append("        WHERE WL_ORDER_BIND.id.wlOrderId = :varWLOrderId  AND WL_ORDER_BIND.activeFlag = :varWLActiveFlag "); 
        sb.append("        )  AND ALIVE_ORDER.activeFlag = :varAliveActiveFlag"); 
        
        Query query = getSession().createQuery(sb.toString());

		query.setString("varWLOrderId",  	jhfWlOrder.getWlOrderId());
		query.setBigDecimal("varWLActiveFlag",     new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setBigDecimal("varAliveActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return query.list();
	}


//	public void updateJhfAliveExecutionPrice(JhfAliveExecution execution,
//			JhfWlOrder jhfWlOrder) {
//		
//		StringBuffer sbHql 	= new StringBuffer(500);
//		sbHql.append(" UPDATE JhfAliveOrder ");
//		sbHql.append(" SET revisionNumber = (revisionNumber + 1) ");
//		sbHql.append("  ,orderStatus =:varFilledOrderStatus ");
//		sbHql.append("  ,boardRate = :varBoardRate ");
//		sbHql.append("  ,tradePrice = :varTradePrice ");
//		sbHql.append("  ,executionPrice = :varExecutionPrice ");
//		sbHql.append("  ,priceId = :varPriceId ");
//		sbHql.append("  ,changeReason = :varChangeReason ");
//		sbHql.append("  ,updateDate = :varUpdateDate ");
//		sbHql.append("  ,updateStaffId = :varUpdateStaffId ");
//		
//		sbHql.append("  ,executionMode = :varExecutionMode ");
//		sbHql.append("  ,tradertimePriceId = :varTradertimePriceId ");
//		sbHql.append("  ,tradertimePrice = :varTradertimePrice ");
//		sbHql.append("  ,cpPriceId = :varCpPriceId ");
//		sbHql.append("  ,cpPrice = :varCpPrice ");
//		
//		sbHql.append(" WHERE  ( orderStatus = :varFillingOrderStatus ");
//		sbHql.append("  OR orderStatus = :varWorkingOrderStatus ) ");
//		sbHql.append("  AND orderId = :varOrderId ");
//		sbHql.append("  AND revisionNumber = :varRevisionNumber ");
//		sbHql.append("  AND activeFlag = :varActiveFlag ");
//		
//		Query query = session.createQuery(sbHql.toString());
//		
//		query.setBigDecimal("varFilledOrderStatus", new BigDecimal(OrderStatusEnum.ORDER_FILLED_ENUM.getValue()));
//		query.setBigDecimal("varBoardRate",pdt.getBoardRate());
//		query.setBigDecimal("varTradePrice",pdt.getPrice());
//		query.setBigDecimal("varExecutionPrice",pdt.getPrice().add(pdt.getMarkup()));
//		
//		query.setString("varPriceId",fillingOrder.getPriceId());
////		query.setString("varPriceId",executionPriceId);
//		
//		query.setBigDecimal("varChangeReason",new BigDecimal(ChangeReasonEnum.ORDER_REASON_EXECUTED_ENUM.getValue()));
//		query.setTimestamp("varUpdateDate", updateDate);
//		query.setString("varUpdateStaffId", pdt.getUpdateStaffId());
//		query.setBigDecimal("varFillingOrderStatus", new BigDecimal(OrderStatusEnum.ORDER_FILLING_ENUM.getValue()));
//		query.setBigDecimal("varWorkingOrderStatus", new BigDecimal(OrderStatusEnum.ORDER_WORKING_ENUM.getValue()));
//		query.setString("varOrderId",fillingOrder.getOrderId());
//		query.setBigDecimal("varRevisionNumber",new BigDecimal( fillingOrder.getRevisionNumber()) );
//		query.setBigDecimal("varActiveFlag",new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()) );
//		
//		query.setBigDecimal("varExecutionMode",executionMode);
//		query.setString("varTradertimePriceId",pdt.getTradePriceInfo().getTraderPriceId() );
//		query.setBigDecimal("varTradertimePrice",tradertimePrice );
//		query.setString("varCpPriceId", cpPriceId);
//		query.setBigDecimal("varCpPrice",cpPrice );
//		
//		
//		int result = query.executeUpdate();
//		
//	}




	public JhfAliveContract queryContract(JhfAliveOrder aliveOrder) {
		// TODO Auto-generated method stub
		return null;
	}


	public void updateHedgeCusttradePrice(JhfAliveExecution execution,
			JhfWlOrder jhfWlOrder) throws CoreException {
		
		StringBuffer sbHql 	= new StringBuffer(500);
		String systemStaffId=ServiceFactory.getCoreService().getSystemStaffId();
		sbHql.append(" UPDATE JhfHedgeCusttrade ");
		sbHql.append(" SET revisionNumber = (revisionNumber + 1) ");
		sbHql.append("  ,price = :varNewPrice ");
		sbHql.append("  ,changeReason = :varChangeReason ");
		sbHql.append("  ,updateDate = :varUpdateDate ");
		sbHql.append("  ,updateStaffId = :varUpdateStaffId ");
		
		sbHql.append(" WHERE  id = :varExecutionId ");
		sbHql.append("  AND activeFlag = :varActiveFlag ");

		
		Query query = getSession().createQuery(sbHql.toString());

		
		query.setBigDecimal("varNewPrice",jhfWlOrder.getExecutionPrice());
		query.setBigDecimal("varChangeReason",jhfWlOrder.getExecutionPrice());
		query.setTimestamp    ("varUpdateDate",DateHelper.getSystemTimestamp() );
		query.setString("varExecutionId", execution.getExecutionId());
		query.setString("varUpdateStaffId", systemStaffId);
		query.setBigDecimal("varActiveFlag",new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()) );
		
//		int result = query.executeUpdate();
		query.executeUpdate();
		
	}


	public void updateSysPositionPrice(JhfAliveExecution execution,
			JhfWlOrder jhfWlOrder) throws CoreException {
		
		StringBuffer sbHql 	= new StringBuffer(500);
		String systemStaffId=ServiceFactory.getCoreService().getSystemStaffId();
		
		sbHql.append(" UPDATE JhfSysPositionInsert ");
		sbHql.append(" SET ");
		sbHql.append("  ,custCounterAmount = :varCounterAmount ");
		sbHql.append("  ,updateDate = :varUpdateDate ");
		sbHql.append("  ,updateStaffId = :varUpdateStaffId ");
		
		sbHql.append(" WHERE  positionSourceId = :varExecutionId ");
		sbHql.append("  AND activeFlag = :varActiveFlag ");

		
		Query query = getSession().createQuery(sbHql.toString());

		
		query.setBigDecimal("varCounterAmount",execution.getExecutionAmount().multiply(jhfWlOrder.getExecutionPrice()));
		query.setTimestamp    ("varUpdateDate",DateHelper.getSystemTimestamp() );
		query.setString("varUpdateStaffId", systemStaffId);
		query.setString("positionSourceId", execution.getExecutionId());
		query.setBigDecimal("varActiveFlag",new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()) );
		
//		int result = query.executeUpdate();
		query.executeUpdate();
		
		
	}


	public void updateURCashflowAmount(JhfAliveExecution execution,
			BigDecimal newSpotPl, BigDecimal newSpotPlJpyAmount) throws CoreException {
		StringBuffer sbHql 	= new StringBuffer(500);
		String systemStaffId=ServiceFactory.getCoreService().getSystemStaffId();
		
		sbHql.append(" UPDATE JhfUnrealizedCashflow ");
		sbHql.append(" SET ");
		sbHql.append("  ,cashflowAmountOriginal = :varNewSpotPl ");
		sbHql.append("  ,cashflowAmount = :varNewSpotPlJpyAmount ");
		sbHql.append("  ,updateDate = :varUpdateDate ");
		sbHql.append("  ,updateStaffId = :varUpdateStaffId ");
		
		sbHql.append(" WHERE  cashflowSourceId = :varExecutionId ");
		sbHql.append("  AND activeFlag = :varActiveFlag ");

		
		Query query = getSession().createQuery(sbHql.toString());

		
		query.setBigDecimal("varNewSpotPl",newSpotPl);
		query.setBigDecimal("varNewSpotPlJpyAmount",newSpotPlJpyAmount);
		query.setTimestamp    ("varUpdateDate",DateHelper.getSystemTimestamp() );
		query.setString("varUpdateStaffId", systemStaffId);
		query.setString("cashflowSourceId", execution.getExecutionId());
		query.setBigDecimal("varActiveFlag",new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()) );
		
//		int result = query.executeUpdate();
		query.executeUpdate();
		
	}


	@SuppressWarnings("unchecked")
	public List<JhfAliveContractBind> queryContractBind(String contractId) {
		StringBuffer sb = new StringBuffer();
        sb.append(" FROM JhfAliveContractBind CB ");
        sb.append(" WHERE CB.id.contractId = :varContractId  "); 
        sb.append("       AND CB.activeFlag = :varActiveFlag "); 
        
        Query query = getSession().createQuery(sb.toString());

		query.setString("varContractId",  	contractId);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return query.list();
	}
	public JhfAliveContractBind querySingleContractBind(String contractId) {
		
		return queryContractBind(contractId).get(0);
	}

	public JhfAliveExecution queryExecutionById(String executionId) throws DaoException {
		return 		(JhfAliveExecution)get(JhfAliveExecution.class, executionId) ;
	}



	public JhfAliveExecution queryExecution(JhfAliveOrder aliveOrder) {
		
		StringBuffer sb = new StringBuffer();
        sb.append(" FROM JhfAliveExecution EX ");
        sb.append(" WHERE EX.orderId = :varOrderId  "); 
        sb.append("       AND EX.activeFlag = :varActiveFlag "); 
        
        Query query = getSession().createQuery(sb.toString());

		query.setString("varOrderId",  	aliveOrder.getOrderId());
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (JhfAliveExecution)query.uniqueResult();
		
	}
	
}
