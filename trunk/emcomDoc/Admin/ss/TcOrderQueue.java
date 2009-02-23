package cn.bestwiz.jhf.core.orderqueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.bean.FillingOrderInfo;
import cn.bestwiz.jhf.core.bo.bean.OrderBindInfo;
import cn.bestwiz.jhf.core.bo.bean.OrderResponseListInfo;
import cn.bestwiz.jhf.core.util.LogUtil;

/**
 * 
 * <p>
 * tc trade processor
 * </p>
 * 
 * @author emfx team
 * @copyright 2009, EMCOM Advanced Technology (Dalian) Co., Ltd.
 */
public class TcOrderQueue implements OrderQueue {
	//
	private final static Log LOG = LogUtil.getLog(TcOrderQueue.class);

	// order request
	private static BlockingQueue<OrderBindInfo> MARKET_ORDER_QUEUE = new LinkedBlockingQueue<OrderBindInfo>(Integer.MAX_VALUE);
	
	// trader response
	private static ConcurrentMap<String, BlockingQueue<OrderResponseListInfo>> MARKET_ORDER_RESPONSE = new ConcurrentHashMap<String, BlockingQueue<OrderResponseListInfo>>();
	
	
	// 
	private static ConcurrentMap<String, OrderResponseListInfo> MARKET_ORDER_CONFIRMATION = new ConcurrentHashMap<String, OrderResponseListInfo>();
	
	//
	private String frontId;
	private Object confirmationLock = new Object();


	/**
	 * 
	 */
	TcOrderQueue() {
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==initializing TcOrderQueue");
		}
	}
	
	TcOrderQueue(String frontId) throws Exception {
		//
		this.frontId = frontId;
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==initializing TcOrderQueue, front id: " + this.frontId);
		}
		
		//
		addResponseQueue(this.frontId);
		
		//
		OrderResponseWorker worker = new OrderResponseWorker();
		worker.setDaemon(true);
		worker.start();
	}
	
	/**
	 * 
	 */
	public void putOrder(OrderBindInfo obi) throws Exception {
		//
		MARKET_ORDER_QUEUE.put(obi);
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==put an order: " + obi);
		}
	}
	
	public OrderBindInfo takeOrder() throws Exception {
		//
		OrderBindInfo obi = MARKET_ORDER_QUEUE.take();
		
		//
		OrderBindInfo r = copy(obi); // Different from JmsOrderQueue
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==took an order: " + r);
		}
		return r;
	}
	
	public OrderResponseListInfo getOrderResponse(String orderBindId, long timeout) throws Exception {
		//
		if(timeout < 0) {
			timeout = 0;
		}
		
		//
		synchronized(confirmationLock) {
			//
			long timeToWait = timeout;
			while(timeToWait >= 0) {
				//
				OrderResponseListInfo r = MARKET_ORDER_CONFIRMATION.remove(orderBindId);
				if(r != null) { // Got it
					if(LOG.isDebugEnabled()) {
						LOG.debug("===[Tc4Trader]==got a order response: " + r);
					}
					return r;
				} else if(timeToWait == 0){
					break;
				} else {
					long mark = System.currentTimeMillis();
					confirmationLock.wait(timeout);
					timeToWait -= (System.currentTimeMillis() - mark);
				}
			}
		}
		
		//
		if(LOG.isInfoEnabled()) {
			LOG.info("===[Tc4Trader]==get order response timed out, order bind id: " + orderBindId);
		}
		return null;
	}
	
	public void putOrderResponse(OrderBindInfo obi, OrderResponseListInfo orli) throws Exception {
		//
		if(obi.getFrontId() == null || obi.getFrontId().trim().equals("")) {
		   LOG.error("===[Tc4Trader]==invalid OrderBindInfo: " + obi + ", detail: front id is null or empty");
		   return;
		}
		
		//
		BlockingQueue<OrderResponseListInfo> responseQueue = MARKET_ORDER_RESPONSE.get(obi.getFrontId());
		if (responseQueue == null) {
			LOG.error("===[Tc4Trader]==failed to get response queue for front id: " + obi.getFrontId());
			addResponseQueue(obi.getFrontId());
		}
		
		//
		responseQueue.put(orli);
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==put an order response: " + orli);
		}
	}
	
	/**
	 * 
	 */
	private boolean addResponseQueue(String frontId) throws Exception {
		//
		BlockingQueue<?> existingQueue = MARKET_ORDER_RESPONSE.putIfAbsent(frontId, new LinkedBlockingQueue<OrderResponseListInfo>());
		if(existingQueue != null) {
			LOG.warn("===[Tc4Trader]==the market order response queue for front id: " + frontId + " exists");
			return false;
		} else {
			return true;
		}
	}
	
	private OrderBindInfo copy(OrderBindInfo obi) throws Exception {
		//
		OrderBindInfo r = new OrderBindInfo();
		
		//
		r.setCurrencyPair(obi.getCurrencyPair());
		r.setSide(obi.getSide());
		r.setPriceId(obi.getPriceId());
		r.setTradeAskPrice(obi.getTradeAskPrice());
		r.setTradeBidPrice(obi.getTradeBidPrice());
		r.setAmount(obi.getAmount());
		r.setOrderBindId(obi.getOrderBindId());
		r.setType(obi.getType());
		r.setMobileFlag(obi.isMobileFlag());
		r.setCustomerId(obi.getCustomerId());
		r.setSlippage(obi.getSlippage());
		r.setMode(obi.getMode());
		r.setStopOrderFlag(obi.getStopOrderFlag());
		r.setBlackOrder(obi.isBlackOrder());
		r.setSlipConfigType(obi.getSlipConfigType());
		r.setTradeDate(obi.getTradeDate());
		r.setFrontId(obi.getFrontId());
		r.setOriginalMessage(obi.getOriginalMessage());
		
		//
		if(obi.getOrderMap() != null) {
			//
			Map<String, FillingOrderInfo> orderMap = new HashMap<String, FillingOrderInfo>();
			for(String key : obi.getOrderMap().keySet()) {
				//
				FillingOrderInfo src = obi.getOrderMap().get(key);
				FillingOrderInfo dest = new FillingOrderInfo();
				PropertyUtils.copyProperties(dest, src);
				
				//
				orderMap.put(key, dest);
			}
			
			//
			r.setOrderMap(orderMap);
		}

		//
		return r;
	}
	

	/**
	 * 
	 */
	private class OrderResponseWorker extends Thread {
		
		public void run() {
			
			while (true) {
				try {
					//
					OrderResponseListInfo orli = MARKET_ORDER_RESPONSE.get(frontId).take();
					if (orli == null) {
						LOG.warn("===[Tc4Trader]==got a invalid OrderResponseListInfo");
						continue;
					}

					//
					if(LOG.isDebugEnabled()) {
						LOG.debug("===[Tc4Trader]==got a OrderResponseListInfo: " + orli.toString());
					}
					MARKET_ORDER_CONFIRMATION.put(orli.getOrderBindId(), orli);
					
					//
					synchronized(confirmationLock) {
						confirmationLock.notifyAll();
					}
				} catch (Exception e) {
					LOG.error("unhandled exception in run", e);
				}
			}
		}
	}
}
