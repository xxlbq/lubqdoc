package jp.emcom.adv.fx.completor.service.dao;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Query;

import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.bo.enums.ForceRelationFlagEnum;
import cn.bestwiz.jhf.core.bo.exceptions.DaoException;
import cn.bestwiz.jhf.core.dao.BaseMainDao;
import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfGroupDefaultProduct;

/**
 *
 * 
 */
@SuppressWarnings("unchecked")
public class WLNetterDao extends BaseMainDao {

	/**
	 * 
	 */
	public JhfGroupDefaultProduct findGroupDefaultProduct(String customerId, String ccy) 
	throws DaoException {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append("SELECT G FROM JhfGroupDefaultProduct G, JhfCustomerStatus C");
        hql.append(" WHERE G.id.groupId = C.groupId");
        hql.append(" AND C.customerId =:varCustomerId");
        hql.append(" AND G.id.currencyPair = :varCCY"); 
        hql.append(" AND G.activeFlag =:varGroupActiveFlag AND C.activeFlag = :varCustomerActiveFlag"); 
        
        //
        final Query query = getSession().createQuery(hql.toString());
		query.setString("varCustomerId", customerId);
		query.setString("varCCY", ccy);
		query.setBigDecimal("varGroupActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setBigDecimal("varCustomerActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		final List<JhfGroupDefaultProduct> list = (List<JhfGroupDefaultProduct>)query.list();
		if(list.size() != 1) {
			throw new DaoException("failed to find JhfGroupDefaultProduct by customerId: " + customerId + ", currencyPair: " + ccy);
		} else {
			return list.get(0);
		}
	}

	/**
	 * 
	 */
	public List<JhfAliveContract> findNotSettledContracts(String customerId, String ccy) {
		//
		final StringBuffer hql = new StringBuffer();
        hql.append(" FROM JhfAliveContract");
        hql.append(" WHERE customerId = :varCustomerId"); 
        hql.append(" AND currencyPair = :varCurrencyPair");
        hql.append(" AND amountNoSettled > 0");
        hql.append(" AND status = :varStatus");  
        hql.append(" AND forceRelationFlag = :varForceRelationFlag");  
        hql.append(" And activeFlag = :varActiveFlag ");
        hql.append(" ORDER BY inputDate ASC, contractId ASC"); 
        
        //
        final Query query = getSession().createQuery(hql.toString());
		query.setString("varCustomerId", customerId);
		query.setString("varCurrencyPair", ccy);
		query.setBigDecimal("varStatus", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setBigDecimal("varForceRelationFlag", new BigDecimal(ForceRelationFlagEnum.FLAG_OTHER_ENUM.getValue()));
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		return (List<JhfAliveContract>)query.list();
	}
}
