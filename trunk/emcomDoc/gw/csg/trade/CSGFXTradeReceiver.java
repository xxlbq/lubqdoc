package cn.bestwiz.jhf.gws.csg.trade;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.SimpleReceiver;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.common.exception.FXMessageException;
import cn.bestwiz.jhf.gws.csg.callback.CsgAbstractCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgFxRequestCallback;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHFXTradeReceiver;

public class CSGFXTradeReceiver {
	/**
	 * 日志文件对象
	 */
	private static Log log = LogUtil.getLog(GKGOHFXTradeReceiver.class);

	/**
	 * 内置的JMS消息的接收对象
	 */
	private SimpleReceiver receiver = null;

	/**
	 * 创建JMS消息接收者
	 * 
	 * @throws FXMessageException 与JMS通讯的错误
	 */
	public CSGFXTradeReceiver() throws FXMessageException {
		try {
			receiver 	= new SimpleReceiver(DestinationConstant.gwCSGOrderRequestQueue);

		} catch (JMSException e) {
			log.error("Destination cannot be found, Please chech Jboss config", e);
			throw new FXMessageException("NamingException", e);
		} catch (Exception e) {
			log.error("Unknown Exception While Creat JMS Object", e);
			throw new FXMessageException("Unknown Excepion", e);
		}

	}

	public void addCallback(SimpleCallback callback){
		receiver.addCallback(callback);
	}
	
	/**
	 * 关闭当前接收器
	 */
	public void close() {

		if (receiver != null) {
			receiver.close();
			
		}

	}
	
//	public static void main(String[] args) throws FXMessageException {
//		CsgContext.init();
//		CsgContext.connect();
//		CSGFXTradeReceiver r = new CSGFXTradeReceiver();
//		
//	}
}
