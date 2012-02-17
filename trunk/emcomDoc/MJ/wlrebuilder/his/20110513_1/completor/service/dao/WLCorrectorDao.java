package jp.emcom.adv.fx.completor.service.dao;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Query;

import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.CashflowTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.PositionSourceFlagEnum;
import cn.bestwiz.jhf.core.bo.exceptions.DaoException;
import cn.bestwiz.jhf.core.dao.BaseMainDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContractBind;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveExecution;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfHedgeCusttrade;
import cn.bestwiz.jhf.core.dao.bean.main.JhfSysPositionInsert;
import cn.bestwiz.jhf.core.dao.bean.main.JhfUnrealizedCashflow;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrderBind;

/**
 * 
 *
 */
@SuppressWarnings("unchecked")
public class WLCorrectorDao extends BaseMainDao {
	
	/**
	 * 
	 */
	public JhfAliveOrder getAliveOrder(String orderId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveOrder");
        hql.append(" WHERE orderId = :varOrderId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varOrderId", orderId);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveOrder> list = (List<JhfAliveOrder>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveOrder by orderId: " + orderId);
		} else {
			return list.get(0);
		}
	}
	
	public JhfAliveExecution getAliveExecution(String executionId)
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveExecution");
        hql.append(" WHERE executionId = :varExecutionId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varExecutionId", executionId);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveExecution> list = (List<JhfAliveExecution>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveOrder by executionId: " + executionId);
		} else {
			return list.get(0);
		}
	}
	
	public JhfAliveContract getAliveContractByContractId(String contractId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveContract");
        hql.append(" WHERE contractId = :varContractId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varContractId", contractId);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveContract> list = (List<JhfAliveContract>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveOrder by contractId: " + contractId);
		} else {
			return list.get(0);
		}
	}
	
	
	public JhfAliveContract getAliveContractByOrderId(String orderId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveContract");
        hql.append(" WHERE orderId = :varOrderId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varOrderId", orderId);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveContract> list = (List<JhfAliveContract>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveOrder by orderId: " + orderId);
		} else {
			return list.get(0);
		}
	}
	
	public JhfHedgeCusttrade getHedgeCusttrade(String id)
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfHedgeCusttrade");
        hql.append(" WHERE id = :varId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varId", id);
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfHedgeCusttrade> list = (List<JhfHedgeCusttrade>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfHedgeCusttrade by id: " + id);
		} else {
			return list.get(0);
		}
	}

	public JhfSysPositionInsert getSysPositionInsert(String sourceId, PositionSourceFlagEnum sourceFlag)
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfSysPositionInsert");
        hql.append(" WHERE positionSourceId = :varPositionSourceId");
        hql.append(" AND positionSourceFlag = :varPositionSourceFlag");
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varPositionSourceId", sourceId);
		query.setBigDecimal("varPositionSourceFlag", new BigDecimal(sourceFlag.getValue()));
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfSysPositionInsert> list = (List<JhfSysPositionInsert>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfSysPositionInsert by wlOrderId: " + sourceId);
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * 
	 */
	public JhfWlOrder findJhfWlOrder(String wlOrderId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfWlOrder ");
        hql.append(" WHERE wlOrderId = :varWlOrderId "); 
        hql.append(" AND activeFlag = :varActiveFlag ");
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varWlOrderId", wlOrderId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfWlOrder> list = (List<JhfWlOrder>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfWlOrder by wlOrderId: " + wlOrderId);
		} else {
			return list.get(0);
		}
	}
	
	public JhfWlOrderBind findWlOrderBindByExecutionId(String executionId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfWlOrderBind");
        hql.append(" WHERE executionId = :varExecutionId"); 
        hql.append(" AND activeFlag = :varActiveFlag ");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varExecutionId", executionId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfWlOrderBind> list = (List<JhfWlOrderBind>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfWlOrderBind by executionId: " + executionId);
		} else {
			return list.get(0);
		}
	}
	
	public List<JhfWlOrderBind> findWlOrderBinds(String wlOrderId) {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfWlOrderBind");
        hql.append(" WHERE id.wlOrderId = :varWlOrderId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        hql.append(" ORDER BY inputDate ASC, id.orderId ASC"); 
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varWlOrderId", wlOrderId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		return (List<JhfWlOrderBind>)query.list();
	}
	
	public JhfAliveExecution findAliveExecutionByOrderId(String orderId)
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveExecution");
        hql.append(" WHERE orderId = :varOrderId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varOrderId", orderId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveExecution> list = (List<JhfAliveExecution>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveExecution by orderId: " + orderId);
		} else {
			return list.get(0);
		}
	}
	
	public JhfAliveContractBind findAliveContractBindByExecutionId(String executionId) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveContractBind");
        hql.append(" WHERE id.executionId = :varExecutionId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varExecutionId", executionId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfAliveContractBind> list = (List<JhfAliveContractBind>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfAliveExecution by executionId: " + executionId);
		} else {
			return list.get(0);
		}
	}
	
	public List<JhfAliveContractBind> findAliveContractBindsByContractId(String contractId) {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveContractBind");
        hql.append(" WHERE id.contractId = :varContractId"); 
        hql.append(" AND activeFlag = :varActiveFlag");
        hql.append(" ORDER BY inputDate ASC, id.executionId ASC"); 
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varContractId", contractId);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		return (List<JhfAliveContractBind>)query.list();
	}
	
	public JhfUnrealizedCashflow findUnrealizedCashflow(String cashflowSourceId, CashflowTypeEnum cashflowType) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfUnrealizedCashflow");
        hql.append(" WHERE cashflowSourceId = :varCashflowSourceId");
        hql.append(" AND cashflowType = :varCashflowType");
        hql.append(" AND activeFlag = :varActiveFlag");
        
        //
        Query query = getSession().createQuery(hql.toString());
		query.setString("varCashflowSourceId", cashflowSourceId);
		query.setBigDecimal("varCashflowType", new BigDecimal(cashflowType.getValue()));
		query.setBigDecimal("varActiveFlag",  new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfUnrealizedCashflow> list = (List<JhfUnrealizedCashflow>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfUnrealizedCashflow by cashflowSourceId: " + cashflowSourceId + ", cashflowType: " + cashflowType);
		} else {
			return list.get(0);
		}
	}
}
