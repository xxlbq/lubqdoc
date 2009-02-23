package cn.bestwiz.jhf.core.orderqueue;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.bean.OrderBindInfo;
import cn.bestwiz.jhf.core.bo.bean.OrderResponseListInfo;
import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.SimpleReceiver;
import cn.bestwiz.jhf.core.jms.SimpleSender;
import cn.bestwiz.jhf.core.util.LogUtil;

/**
 * 
 * <p>
 * jms trade processor
 * </p>
 * 
 * @author emfx team
 * @copyright 2009, EMCOM Advanced Technology (Dalian) Co., Ltd.
 */
public class JmsOrderQueue implements OrderQueue {
	//
	private final static Log LOG = LogUtil.getLog(JmsOrderQueue.class);

	//
	private static ConcurrentMap<String, OrderResponseListInfo> MARKET_ORDER_CONFIRMATION = new ConcurrentHashMap<String, OrderResponseListInfo>();
	
	//
	private String frontId;
	private SimpleSender orderSender;
	private SimpleReceiver orderReceiver;
	
	//
	private Object confirmationLock = new Object();


	/**
	 * 
	 */
	JmsOrderQueue(boolean isOrderSender) throws Exception {
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==initializing JmsOrderQueue");
		}
		
		//
		if(isOrderSender) {
			orderSender = SimpleSender.getInstance(DestinationConstant.OrderRequestQueue);	
		} else {
			orderReceiver = new SimpleReceiver(DestinationConstant.OrderRequestQueue, 0, true);
		}
	}
	
	JmsOrderQueue(String frontId) throws Exception {
		//
		this.frontId = frontId;
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==initializing JmsOrderQueue, front id: " + this.frontId);
		}
		
		//
		orderSender = SimpleSender.getInstance(DestinationConstant.OrderRequestQueue, true, true);
		orderSender.addCallback(new OrderResponseListener());
	}
	
	/**
	 * 
	 */
	public void putOrder(OrderBindInfo obi) throws Exception {
		//
		orderSender.sendMessage(obi);
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==put an order: " + obi);
		}
	}
	
	public OrderBindInfo takeOrder() throws Exception {
		//
		Message message = orderReceiver.receiveMessage();
		ObjectMessage om = (ObjectMessage) message;
		OrderBindInfo obi = (OrderBindInfo)om.getObject();
		obi.setOriginalMessage(message);
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==took an order: " + obi);
		}
		
		//
		return obi;
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
			LOG.info("get order response timed out, order bind id: " + orderBindId);
		}
		return null;
	}
	
	public void putOrderResponse(OrderBindInfo obi, OrderResponseListInfo orli) throws Exception {

		//
		if(obi.getOriginalMessage() == null) {
			LOG.error("===[Tc4Trader]==invalid OrderBindInfo: " + obi + ", detail: original message is null");
			return;
		}
		
		if(obi.getOriginalMessage().getJMSReplyTo() == null) {
		   LOG.error("===[Tc4Trader]==invalid OrderBindInfo: " + obi + ", detail: jms reply to is null");
		   return;
		}
		
		//
		Message om = obi.getOriginalMessage();
		orderReceiver.sendReplyMessage(om, orli);
		
		//
		if(LOG.isDebugEnabled()) {
			LOG.debug("===[Tc4Trader]==put an order response: " + orli);
		}
	}
	
	/**
	 * 
	 */
	private class OrderResponseListener implements SimpleCallback {

		public void onMessage(Serializable message) {
			//
			if (message == null) {
				LOG.warn("===[Tc4Trader]==got a null message");
				return;
			}
			if (!(message instanceof OrderResponseListInfo)) {
				LOG.error("===[Tc4Trader]==got a invalid message: "+ message);
				return;
			}
			OrderResponseListInfo orli = (OrderResponseListInfo) message;
			
			//
			if(LOG.isDebugEnabled()) {
				LOG.debug("===[Tc4Trader]==got a OrderResponseListInfo: " + orli.toString());
			}
			MARKET_ORDER_CONFIRMATION.put(orli.getOrderBindId(), orli);
			
			//
			synchronized(confirmationLock) {
				confirmationLock.notifyAll();
			}
		}
	}
}
