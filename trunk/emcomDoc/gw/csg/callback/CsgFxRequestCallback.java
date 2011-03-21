package cn.bestwiz.jhf.gws.csg.callback;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.idgenerate.exception.IdGenerateException;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.bean.GwCpTradeRequestInfo;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.common.fxtrans.FXTradeSender;
import cn.bestwiz.jhf.gws.common.util.IdGenerateFacade;
import cn.bestwiz.jhf.gws.csg.common.CSGMessageKeeper;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHService;

import com.pstcl.clientsys.PSTClientContext;

public class CsgFxRequestCallback extends CsgAbstractCallback implements SimpleCallback{

	private static final Log LOG = LogUtil.getLog(CsgFxRequestCallback.class);
	private CSGMessageKeeper messageKeeper = null;
	/**
	 * 内置的JMS消息的发送对象
	 */
	private FXTradeSender sender = null;
	
	
	public CsgFxRequestCallback(int count) {
		super(count);
		messageKeeper = CSGMessageKeeper.getInstance();// 获得可用的控制数据对象。
		sender = GKGOHService.getInstance().getTradeResponseSender();// 内置的JMS消息的发送对象
	}
	
	
	public void dealItem(Object workItem) {
		
		LOG.info("CsgFxRequestCallback fire "+workItem);
		
		GwCpTradeRequestInfo tradeRequest = (GwCpTradeRequestInfo) workItem;
		System.out.println("TAKE FROM Q :"+tradeRequest.getCpCoverId());
		
		messageKeeper.putRequests(tradeRequest.getCpCoverId(), tradeRequest);
		
//		PSTClientContext.getContext().re
		
		
	}
	public void dealItemTest(PSTClientContext context) throws ClassNotFoundException, IOException, IdGenerateException {
		
		LOG.info("CsgFxRequestCallback fire :");
		
//		GwCpTradeRequestInfo tradeRequest = (GwCpTradeRequestInfo) workItem;
//		System.out.println("TAKE FROM Q :"+tradeRequest.getCpCoverId());
//		
		context.requestLIBOrder(
				"USD", "JPY", new BigDecimal("100.00"), new BigDecimal("10000"), new BigDecimal("10000"), "Buy", "2010-11-01", "2010-11-03", "", 
				IdGenerateFacade.getFxPriceIdForGKGOH(), new BigDecimal("0.020"));
	}

	public void onMessage(Serializable message) {
		enQueue(message);
	}

}