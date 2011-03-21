package cn.bestwiz.jhf.gws.csg.common;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import quickfix.fix42.MarketDataIncrementalRefresh;
import cn.bestwiz.jhf.core.jms.bean.GwCpTradeRequestInfo;
import cn.bestwiz.jhf.core.util.LogUtil;



/**
 * SAXO银行服务中用到的缓存数据对象的封装类
 * 
 * @author JHF Team <jhf@bestwiz.cn>
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd
 */
public class CSGMessageKeeper {
	/**
	 * 实例
	 */
	private static CSGMessageKeeper _instance = new CSGMessageKeeper();

	/**
	 * 存储交易请求
	 */
	private ConcurrentHashMap<String, GwCpTradeRequestInfo> requestsCache = null;

	

	/**
	 * 接收到的汇率订阅回复，按顺序加入此列表，便于对应的接收线程读取
	 */




	private ConcurrentHashMap<String, MarketDataIncrementalRefresh.NoMDEntries> rateBidMap = null;

	/**
	 * 接收到的Ask银行汇率，按货币对加入此列表
	 */

	private ConcurrentHashMap<String, MarketDataIncrementalRefresh.NoMDEntries> rateAskMap = null;


	public static final String DEALTYPE_BUY = "BUY";

	/**
	 * 汇率交易类型
	 */

	public static final String DEALTYPE_SELL = "SELL";

	/**
	 * 对银的连接是否应该处于开启状态
	 */

	private boolean ConnectOn = false;

	private final String end = "End Service";

	/**
	 * 日志文件对象
	 */

	private static Log log = LogUtil.getLog(CSGMessageKeeper.class);

	/**
	 * 构造
	 * 
	 */

	private CSGMessageKeeper() {
		this.requestsCache = new ConcurrentHashMap<String, GwCpTradeRequestInfo>();
		this.rateBidMap = new ConcurrentHashMap<String, MarketDataIncrementalRefresh.NoMDEntries>();
		this.rateAskMap = new ConcurrentHashMap<String, MarketDataIncrementalRefresh.NoMDEntries>();
	}

	/**
	 * 获取唯一实例
	 * 
	 * @return
	 */
	public static  CSGMessageKeeper getInstance() {
		return _instance;
	}

	/**
	 * 清理全部数据内容
	 * 
	 */
	public void clearCache() {
		this.requestsCache.clear();
		this.rateBidMap.clear();
		this.rateAskMap.clear();
	}

	/**
	 * 关闭全部数据内容
	 * 
	 */
	public void close() {
		this.requestsCache.clear();
		this.rateBidMap.clear();
		this.rateAskMap.clear();
//		this.requestsCache = null;
		this.rateBidMap = null;
		this.rateAskMap = null;
	}

	/**
	 * 取得交易请求
	 * 
	 * @param key
	 * @return GwCpTradeRequestInfo
	 */
	public GwCpTradeRequestInfo getRequests(String key) {
		GwCpTradeRequestInfo info = this.requestsCache.get(key);
		this.requestsCache.remove(key);
		return info;
	}

	/**
	 * 加入交易请求
	 * 
	 * @param key
	 * @param data
	 */
	public void putRequests(String key, GwCpTradeRequestInfo data) {
		this.requestsCache.put(key, data);
	}




	/**
	 * 是否应该与银行处于连接状态
	 * 
	 * @return 应否处于连接状态
	 */
	public boolean isConnectOn() {
		return ConnectOn;
	}



	
	public ConcurrentHashMap<String, quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries> getRateBidMap() {
		return rateBidMap;
	}
	
	public ConcurrentHashMap<String, quickfix.fix42.MarketDataIncrementalRefresh.NoMDEntries> getRateAskMap() {
		return rateAskMap;
	}

	/**
	 * 设置是否应该与银行处于连接状态
	 * 
	 * @param connectOn设置应否处于连接状态
	 */
//	public void setConnectOn(boolean connectOn, String reason) {
//		log.info("setConnectOn to " + connectOn + " Reason is " + reason);
//		if (connectOn != this.ConnectOn) {
//			this.ConnectOn = connectOn;
//			try {
//				ChangeCpStatus.changeStatus(CSGProperty.COUNTERPARTY_ID,
//						connectOn, reason, CSGService.getInstance()
//								.getStatusSender());
//			} catch (GWException e) {
//				e.printStackTrace();
//				log.error("Change CP status Error", e);
//			}
//		}
//	}
}