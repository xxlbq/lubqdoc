package cn.bestwiz.jhf.gws.csg.callback;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;

import quickfix.fix42.ExecutionReport;

import cn.bestwiz.jhf.core.jms.bean.GwCpTradeRequestInfo;
import cn.bestwiz.jhf.core.jms.bean.GwCpTradeResponseInfo;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.util.DateHelper;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.common.constant.GwCPErrorConstant;
import cn.bestwiz.jhf.gws.common.exception.FXMessageException;
import cn.bestwiz.jhf.gws.common.fxtrans.FXTradeSender;
import cn.bestwiz.jhf.gws.csg.common.CSGMessageKeeper;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHService;
import cn.bestwiz.jhf.gws.gkgoh.common.MailUtil;

public class CsgTradeCallback extends CsgAbstractCallback{

	private static final Log log = LogUtil.getLog(CsgRateCallback.class);
	private CSGMessageKeeper messageKeeper = null;
	/**
	 * 内置的JMS消息的发送对象
	 */
	private FXTradeSender sender = null;
	
	
	public CsgTradeCallback(int count) {
		super(count);
		messageKeeper = CSGMessageKeeper.getInstance();// 获得可用的控制数据对象。
		sender = GKGOHService.getInstance().getTradeResponseSender();// 内置的JMS消息的发送对象
	}

	
	public void dealItem(Object workItem) {
		
		log.info("CsgTradeCallback fire "+workItem);
//		System.out.println("TAKE FROM Q :"+((OrderTicket)workItem).getOamount());
		
		
	}

	
	
	private void handleTradeSuccess(ExecutionReport msg) {
		GwCpTradeRequestInfo tradeRequestInfo = null;
		GwCpTradeResponseInfo tradeResponse = new GwCpTradeResponseInfo(); // 内部产生的交易回复信息。
		
		try {
			// 1按确认信息中的TradeID从缓存查找交易请求对象
			tradeRequestInfo = messageKeeper.getRequests(msg.getClOrdID().getValue());

			// 2如果交易的请求不存在于列表当中，则构造错误返回并发送警告邮件。
			if (tradeRequestInfo == null) {
				// 如果请求交易的汇率未存在于列表当中，则应向系统发出交易失败的回复。
				log.warn("No TradeRequestInfo Matched for the COVERID: " + msg.getClOrdID().getValue());

				return;
			}

			// 如果同意进行交易，根据银行对交易的成功回复，
			// 生成本系统可识别的回复信息对象（GwCpTradeResponseInfo类的对象）
			tradeResponse = generateNormalResponseInfo(tradeRequestInfo, msg);
		} catch (Exception e) {
			// 捕获其他错误，需生成失败回复信息对象
			tradeResponse = generateErrorResponseInfo(tradeRequestInfo, GwCPErrorConstant.ERROR_CODE_1005, GwCPErrorConstant.getErrorMsg(GwCPErrorConstant.ERROR_CODE_1005));
			log.error("Unknown Exception", e);

			// send sys mail
			MailUtil.sendTradeResponseSysMail("GW-GKGOH1502", tradeResponse);
		}
		sendResponseInfo(tradeResponse);
	}
	
	
	/**
	 * 处理交易失败
	 * 
	 * @param msg
	 *            待处理的交易失败消息
	 */
	private void handleTradeError(ExecutionReport msg) {
		GwCpTradeRequestInfo tradeRequestInfo = null;
		GwCpTradeResponseInfo tradeResponse = new GwCpTradeResponseInfo(); // 内部产生的交易回复信息。

		try {
			// 1按失败信息中的TradeID从缓存查找交易请求对象
			tradeRequestInfo = messageKeeper.getRequests(msg.getClOrdID().getValue());

			// 2如果请求交易的汇率不存在于列表当中，则构造错误返回并发送警告邮件。
			if (tradeRequestInfo == null) {
				// 如果请求交易的汇率未存在于列表当中，则应向系统发出交易失败的回复。
				log.warn("No TradeRequestInfo Matched for the COVERID: " + msg.getClOrdID().getValue());

				return;
			}

			// 如果拒绝进行交易，根据银行对交易的拒绝回复，
			// 生成本系统可识别的回复信息对象（GwCpTradeResponseInfo类的对象）
			tradeResponse = generateErrorResponseInfo(tradeRequestInfo, "TradeError", msg.getText().getValue());
			log.info("Rejected message[trade Rejected]=" + msg.toString());
			
			
			
		} catch (Exception e) {
			// 捕获其他错误，需生成失败回复信息对象
			tradeResponse = generateErrorResponseInfo(tradeRequestInfo, GwCPErrorConstant.ERROR_CODE_1005, GwCPErrorConstant.getErrorMsg(GwCPErrorConstant.ERROR_CODE_1005));
			log.error("Unknown Exception", e);

			// send sys mail
			MailUtil.sendTradeResponseSysMail("GW-GKGOH1502", tradeResponse);
		}
		sendResponseInfo(tradeResponse);
		
	}
	
	
	/**
	 * 向FX系统发送响应信息
	 * 
	 * @author houtw <houtw@bestwiz.cn>
	 */
	private void sendResponseInfo(GwCpTradeResponseInfo tradeResponse) {
		try {
			synchronized (sender) {
				sender.sendTrade(tradeResponse);
			}
		} catch (JMSException e) {
			log.error("CP Message send Exception", e);
		} catch (FXMessageException e) {
			log.error("CP Message send Exception", e);
		}
	}
	
	
	
	/**
	 * 根据生成的成功交易回复对象
	 * 
	 * @param request
	 *            交易请求信息
	 * @param msg
	 *            交易结果
	 * @return GwCpTradeResponseInfo
	 */
	private GwCpTradeResponseInfo generateNormalResponseInfo(GwCpTradeRequestInfo request, ExecutionReport msg) {
		GwCpTradeResponseInfo response = new GwCpTradeResponseInfo();

		try {
			String tradeDate = ServiceFactory.getCoreService().getFrontDate();
			response.setCpCoverId(msg.getClOrdID().getValue());
			response.setPriceId(request.getPriceId());
			response.setCounterpartyId(request.getCounterpartyId());
			response.setCpConfirmId(msg.getOrderID().getValue());
			response.setCurrencyPair(request.getCurrencyPair());
			response.setSide(request.getSide());
			BigDecimal amount = new BigDecimal(String.valueOf(msg.getCumQty().getValue()));
			BigDecimal rate = new BigDecimal(String.valueOf(msg.getLastPx().getValue()));
			response.setAmount(amount);
			response.setCounterAmount(amount.multiply(rate));
			response.setRate(rate);
			response.setTradeDate(tradeDate);
			response.setCpTradeDate(null);
			response.setValueDate(msg.getFutSettDate().getValue());
			response.setSuccessFlag(true);
			response.setResponseTime(msg.getTransactTime().getValue());
		} catch (Exception e) {
			log.error("create response info error.", e);
			response = null;
		}

		return response;
	}

	/**
	 * 根据生成的失败交易回复对象，生成对应的包含失败交易信息的本系统可识别的交易回复对象。
	 * 
	 * @param request
	 *            交易请求信息
	 * @param errorCode
	 *            内部错误代码
	 * @param errorMsg
	 *            错误信息
	 * @return GwCpTradeResponseInfo
	 */
	private GwCpTradeResponseInfo generateErrorResponseInfo(GwCpTradeRequestInfo request, String errorCode, String errorMsg) {
		GwCpTradeResponseInfo response = new GwCpTradeResponseInfo();

		try {
			response.setCpCoverId(request.getCpCoverId());
			response.setPriceId(request.getPriceId());
			response.setCounterpartyId(request.getCounterpartyId());
			response.setCurrencyPair(request.getCurrencyPair());
			response.setSide(request.getSide());
			response.setAmount(request.getAmount());
			response.setRate(request.getRate());
			response.setSuccessFlag(false);
			response.setErrorCode(errorCode);
			response.setErrorMsg(errorMsg);
			response.setResponseTime(DateHelper.getTodaysDate());
		} catch (Exception e) {
			log.error("create response info error.", e);
			response = null;
		}

		return response;
	}
}