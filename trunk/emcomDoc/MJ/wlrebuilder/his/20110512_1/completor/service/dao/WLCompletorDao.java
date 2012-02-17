package jp.emcom.adv.fx.completor.service.dao;

import java.math.BigDecimal;
import java.util.List;

import jp.emcom.adv.fx.commons.bo.enums.CompletionStatusEnum;
import jp.emcom.adv.fx.commons.bo.enums.OrderStatusEnum;

import org.hibernate.Query;

import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.exceptions.DaoException;
import cn.bestwiz.jhf.core.dao.BaseMainDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfSysPositionInsert;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlClientConfig;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;

/**
 *
 * 
 */
@SuppressWarnings("unchecked")
public class WLCompletorDao extends BaseMainDao {

	/**
	 * 
	 */
	public List<JhfWlOrder> findCompletionCandidates(List<String> clientIds) {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfWlOrder ORD ");
        hql.append(" WHERE ORD.orderStatus = :varOrderStatus"); 
        hql.append(" AND ORD.completionStatus IN (:varCompletionStatus1, :varCompletionStatus2, :varCompletionStatus3, :varCompletionStatus4, :varCompletionStatus5)");
        hql.append(" AND ORD.clientId IN (:varClientIds)");
        hql.append(" AND ORD.activeFlag =:varActiveFlag");
        hql.append(" ORDER BY ORD.inputDate, ORD.wlOrderId"); 
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varOrderStatus", OrderStatusEnum.FILLED.getValue());
		query.setParameterList("varClientIds", clientIds);
		query.setBigDecimal("varCompletionStatus1", CompletionStatusEnum.PENDING.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus2", CompletionStatusEnum.PROGRESSING.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus3", CompletionStatusEnum.FAILED.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus4", CompletionStatusEnum.CORRECTED.getValueAsBigDecimal());
		query.setBigDecimal("varCompletionStatus5", CompletionStatusEnum.CORRECTION_FAILED.getValueAsBigDecimal());
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		return (List<JhfWlOrder>)query.list();
	}

	public List<JhfWlClientConfig> findAutoRebuildWorkingId(List<String> clientIds) {
		
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfWlClientConfig ");
        hql.append(" WHERE clientId IN (:varClientIds) "); 
        hql.append(" AND positionAutoRebuildConstraint = :varConstraint ");
        hql.append(" AND activeFlag =:varActiveFlag ");
        
        //
        Query query = getSession().createQuery(hql.toString());
        query.setParameterList("varClientIds", clientIds);
		query.setBigDecimal("varConstraint", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		query.list();
		
		return query.list();
	}
 
	public List<String> findUsingCurrencyPair(String clientId) {
		
		final StringBuffer hql = new StringBuffer();
        hql.append(" SELECT id.currencyPair FROM JhfWlRateConfig ");
        hql.append(" WHERE id.clientId =:varClientIds "); 
        hql.append(" AND activeFlag =:varActiveFlag ");
        //
        Query query = getSession().createQuery(hql.toString());
        query.setString("varClientIds", clientId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (List<String>)query.list();
	}

	

	public void saveWLOrder(JhfWlOrder ord) throws DaoException{
		save(ord);
	}

	public void saveJhfSysPositionInsert(JhfSysPositionInsert sysPos) throws DaoException {
		save(sysPos);
	}

	public long haveRebuildedPos(String customerId, String currencyPair,
			String frontDate) {
		
		final StringBuffer hql = new StringBuffer();
        hql.append(" SELECT COUNT(*) FROM JhfWlPositionAutoRebuild ");
        hql.append(" WHERE id.frontDate =:varFrontDate "); 
        hql.append(" AND   id.customerId =:varCustomerId "); 
        hql.append(" AND   id.currencyPair =:varCurrencyPair "); 
        hql.append(" AND activeFlag =:varActiveFlag ");
        //
        Query query = getSession().createQuery(hql.toString());
        query.setString("varFrontDate", frontDate);
        query.setString("varCustomerId", customerId);
        query.setString("varCurrencyPair", currencyPair);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (Long)query.uniqueResult();
	}

	public long haveCompletionOrder(String customerId, String currencyPair,
			String frontDate) {
		
		final StringBuffer hql = new StringBuffer();
        hql.append(" SELECT COUNT(*) FROM JhfWlOrder ");
        hql.append(" WHERE orderDate =:varFrontDate "); 
        hql.append(" AND   customerId =:varCustomerId "); 
        hql.append(" AND   currencyPair =:varCurrencyPair "); 
        hql.append(" AND   orderStatus = 2 "); 
        hql.append(" AND   completionStatus <> 3 "); 
        hql.append(" AND   activeFlag =:varActiveFlag ");
        //
        Query query = getSession().createQuery(hql.toString());
        query.setString("varFrontDate", frontDate);
        query.setString("varCustomerId", customerId);
        query.setString("varCurrencyPair", currencyPair);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (Long)query.uniqueResult();
	}

	public BigDecimal sumRebuildAmount(String customerId, String currencyPair) {
		
		final StringBuffer hql = new StringBuffer();
        hql.append(" SELECT SUM(side * amountNoSettled) AS TOTAL FROM JhfAliveContract ");
        hql.append(" WHERE "); 
        hql.append("       customerId =:varCustomerId "); 
        hql.append(" AND   currencyPair =:varCurrencyPair "); 
        hql.append(" AND   forceRelationFlag = 0 "); 
        hql.append(" AND   activeFlag =:varActiveFlag ");
        //
        Query query = getSession().createQuery(hql.toString());
        query.setString("varCustomerId", customerId);
        query.setString("varCurrencyPair", currencyPair);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		
		return (BigDecimal)query.uniqueResult();
	}

	
}
