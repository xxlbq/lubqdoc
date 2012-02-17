package jp.emcom.adv.fx.completor.service.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.transform.Transformers;

import cn.bestwiz.jhf.core.bo.enums.BoolEnum;
import cn.bestwiz.jhf.core.dao.BaseInfoDao;

public class WLRebuilderDao extends BaseInfoDao{

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findCloseRate(String clientId, String ccp ,long msgTime) {
		final StringBuffer hql = new StringBuffer();
        hql.append(" SELECT WL_RATE_ID,ASK_RATE,BID_RATE  FROM JHF_WL_RATE_").append(clientId);
        hql.append(" WHERE CURRENCY_PAIR = :varCurrencyPair "); 
        hql.append(" AND MESSAGE_TIME_LONG <=:varMsgTime ");
        hql.append(" AND ACTIVE_FLAG =:varActiveFlag ");
        hql.append(" ORDER BY MESSAGE_TIME_LONG DESC LIMIT 1 ");
        //
        Query query = getSession().createSQLQuery(hql.toString());
        query.setString("varCurrencyPair", ccp);
        query.setLong("varMsgTime", msgTime);
		query.setBigDecimal("varActiveFlag", new BigDecimal(BoolEnum.BOOL_YES_ENUM.getValue()));
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		return (List<Map<String, Object>>)query.list();
	}
}
