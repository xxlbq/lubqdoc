package cn.bestwiz.jhf.gws.csg.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;

public class CsgProperty {
	
	private static final Log LOG = LogUtil.getLog(CsgProperty.class);
	private Properties p = null;

	private String counterpartyId = null;

	private int rateCheckInterval = -1 ;
	private int tradeCount = -1 ;
	private int rateCount = -1 ;
	private int fxRequestCount = -1 ;
	
	
	private Map<String,String> pairs = null;
	public CsgProperty() {

		try {
			p = PropertiesLoader.getProperties("GwCSGConfig.properties");
			
			this.counterpartyId		= PropertiesLoader.getStringProperty(p, "COUNTERPARTY_ID", "<NULL>");
			this.rateCheckInterval 	= PropertiesLoader.getIntegerProperty(p, "RATE_CHECK_INTERVAL", -1);
			this.tradeCount 		= PropertiesLoader.getIntegerProperty(p, "TRADE_COUNT", -1);
			this.rateCount 			= PropertiesLoader.getIntegerProperty(p, "RATE_COUNT", -1);
			this.fxRequestCount 	= PropertiesLoader.getIntegerProperty(p, "FX_REQUEST_COUNT", -1);
			
			this.pairs = new HashMap<String,String>();
			// 获得本系统使用的货币对列表。
			setPair(pairs);

			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	
	private void setPair(Map<String, String> pairs) {
		String[] fxCurrencyPair = p.getProperty("FX_PAIR").split(",");
		LOG.info("pairs:"+fxCurrencyPair);
		
		for (String ccy : fxCurrencyPair) {
			pairs.put(ccy, ccy);
		}
	}

	public String getCounterpartyId() {
		return counterpartyId;
	}


	public int getRateCheckInterval(){
		return this.rateCheckInterval ;
	}
	
	public int getTradeCount(){
		return this.tradeCount ;
	}
	
	public int getRateCount(){
		return rateCount;
	}
	
	public int getFxRequestCount(){
		return fxRequestCount ;
	}
	
	public Map getPairs(){
		return pairs;
	}
	
}
