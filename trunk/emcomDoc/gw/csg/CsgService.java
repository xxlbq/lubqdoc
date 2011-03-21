package cn.bestwiz.jhf.gws.csg;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleSender;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;
import cn.bestwiz.jhf.gws.common.callback.IMessageCallback;
import cn.bestwiz.jhf.gws.common.exception.CPConnectException;
import cn.bestwiz.jhf.gws.common.exception.GWException;
import cn.bestwiz.jhf.gws.common.factory.FXMsgTransFactory;
import cn.bestwiz.jhf.gws.common.fxtrans.FXMessageReceiver;
import cn.bestwiz.jhf.gws.common.fxtrans.FXRateSender;
import cn.bestwiz.jhf.gws.common.fxtrans.FXTradeSender;
import cn.bestwiz.jhf.gws.common.service.Serviceable;
import cn.bestwiz.jhf.gws.common.util.GWDataLogger;
import cn.bestwiz.jhf.gws.common.util.SchduleSeason;
import cn.bestwiz.jhf.gws.csg.callback.CsgAbstractCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgFxRequestCallback;
import cn.bestwiz.jhf.gws.csg.trade.CSGFXTradeReceiver;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHFixListener;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHInitiator;
import cn.bestwiz.jhf.gws.gkgoh.GKGOHService;
import cn.bestwiz.jhf.gws.gkgoh.callback.GKGOHFXTradeCallback;
import cn.bestwiz.jhf.gws.gkgoh.common.GKGOHMessageKeeper;
import cn.bestwiz.jhf.gws.gkgoh.common.GKGOHProperty;
import cn.bestwiz.jhf.ratecheck.bugrate.BugRateFactory;
import cn.bestwiz.jhf.ratecheck.bugrate.BugRateWorkThread;

public class CsgService implements Serviceable {

	/**
	 * 日志文件对象
	 */
	private final static Log log = LogUtil.getLog(CsgService.class);

	/**
	 * 本类的内置实例
	 */
	private static CsgService _instance = new  CsgService();;

	/**
	 * 对银行的缓存对象
	 */
	private GKGOHMessageKeeper GKGOHControl = null;

	/**
	 * 对JMS的消息接收器，用于接收交易请求消息
	 */
	private FXMessageReceiver tradeRequestReceiver = null;


	/**
	 * 对JMS的消息发送器，用于发送交易回复消息
	 */
	private FXTradeSender tradeResponseSender = null;


	/**
	 * 对JMS的消息发送器，用于发布汇率消息
	 */
	private FXRateSender rateSender = null;

	/**
	 * 银行信息监听者，用于获取银行消息
	 */
	private GKGOHFixListener gkgohListener = null;

	/**
	 * CP状态消息JMS发送者
	 */
	private SimpleSender statusSender = null;

	/**
	 * Establishes sessions with FIX servers
	 */
	private GKGOHInitiator initiator = null;

	private BugRateWorkThread bugrate = null;

	
	/**
	 * 构造
	 * 
	 */

	private CsgService() {
		log.info("Create object of GKGOHService");
	}

	/**
	 * 获得当前控制数据类的唯一实例。
	 * 
	 * @return 本类的唯一可用实例对象
	 * @author houtw <houtw@bestwiz.cn>
	 */

	public static   CsgService getInstance() {
		return _instance;
	}

	/**
	 * 启动DB银行相关的一切服务
	 * 
	 */

	public void startService() throws GWException {

		log.info("Create MesNEgeSendFactory");

		try {
			

			log.info("------- Create CSG Server ------- ");
			CsgContext.init();
			CsgContext.connect();
			
			CsgAbstractCallback callback = new CsgFxRequestCallback(CsgContext.getProp().getFxRequestCount());
			CSGFXTradeReceiver fxReceiver = new CSGFXTradeReceiver();
			fxReceiver.addCallback((CsgFxRequestCallback)callback);
			
			
			
			
			
			// 初始化bugratechek service
			bugrate = BugRateFactory.initService(PropertiesLoader.getProperties(GKGOHProperty.BugRatePropertiesFile));

			// 创建Cron Season
			SchduleSeason.getInstance().startUp();

			// 创建与Jboss的通讯对象创建工厂
			FXMsgTransFactory fxMessageSendFactory = FXMsgTransFactory.getInstance();// 创建产生对象用的工厂对象

			// 创建缓存对象
			GKGOHControl = GKGOHMessageKeeper.getInstance();

			// // 创建与JMS的通信器：汇率发布器
			log.info(" 1 Create rateSender");
			rateSender = fxMessageSendFactory.creatRateSender(DestinationConstant.gwCounterPartyGKGOHRateTopic);
			
			// // 创建与JMS的通信器：交易回复发送器
			log.info(" 2 Create tradeResponseSender");
			tradeResponseSender = fxMessageSendFactory.createTradeSender();

			// // 创建与JMS的通信器：交易回复发送器
			log.info(" 3 Create CPStatusSender");
			statusSender = fxMessageSendFactory.createStatusSender();

			// 创建与JMS的通信器：交易请求接收器
			log.info(" 4 Create tradeRequestReceiver");
			tradeRequestReceiver = fxMessageSendFactory.creatDppTradeReceiver(GKGOHProperty.COUNTERPARTY_ID);
			IMessageCallback tradeRequestCallback = new GKGOHFXTradeCallback(GKGOHProperty.TRADE_COUNT);
			tradeRequestReceiver.addCallback(tradeRequestCallback);

			log.info(" 5 Create GKGOH  Listener objcet");
			gkgohListener = new GKGOHFixListener();
			
			log.info(" 6 Create Fix initiator objcet");
			initiator = new GKGOHInitiator();
			initiator.setApp(gkgohListener);
			initiator.startUp();

			
			log.info("-------------Started GKGOH Service-------------");

		} catch (Exception e) {
			log.error("Start up GKGOH Service error", e);
			GWException ex = new GWException(e);
			throw ex;
		}
	}

	/**
	 * 关闭DB银行相关的一切服务
	 * 
	 */

	public void endService() {

		try {
			log.info("End GKGOH Service");

			// 关闭QuickfIx
			log.info("  Destroy initiator");
			if (initiator != null) {
				initiator.close();
				initiator = null;
			}

			// 关闭CP消息监听
			log.info("  Close the FIX Listener");
			if (gkgohListener != null) {
				gkgohListener.colse();
				gkgohListener = null;
			}
			// 关闭缓存
			log.info("  Destroy LBControl");
			if (GKGOHControl != null) {
				GKGOHControl.close();
				GKGOHControl = null;
			}

			// 关闭从JMS接收的交易请求
			log.info("  Destroy tradeRequestReceiver");
			if (tradeRequestReceiver != null) {
				tradeRequestReceiver.close();
				tradeRequestReceiver = null;
			}


			// 关闭向JMS的交易回复发送器
			log.info("  Destroy tradeResponseSender");
			if (tradeResponseSender != null) {
				tradeResponseSender.close();
				tradeResponseSender = null;
			}

			// 关闭CP状态发送者
			log.info("  Destroy CPStatusSender");
			if (statusSender != null) {
				statusSender.close();
				statusSender = null;
			}

			// 关闭向JMS的汇率发送器
			log.info("  Destroy rateSender");
			if (rateSender != null) {
				rateSender.close();
				rateSender = null;
			}

			// stop bugrate process
			if (bugrate != null)
				bugrate.shutdown();

			// 清空控制数据对象
			log.info(" Clear GWDataLogger");
			GWDataLogger.clear();
			log.info("------------Ended GKGOH Service------------");

		} catch (Exception e) {
			CPConnectException cpce = new CPConnectException(e);
			log.error("Shut down GoldmanSachs Service error", cpce);
		}
	}

	/**
	 * 获得对GKGOH的缓存对象
	 * 
	 * @return 缓存对象
	 */
	public GKGOHMessageKeeper getGKGOHControl() {
		return GKGOHControl;
	}

	/**
	 * 获得与JMS通讯的汇率发送器实例
	 * 
	 * @return 内置的汇率发布对象
	 */
	public FXRateSender getRateSender() {
		return rateSender;
	}

	/**
	 * 获得与JMS通讯的交易发送器实例
	 * 
	 * @return 内置的交易发送器实例
	 */
	public FXTradeSender getTradeResponseSender() {
		return tradeResponseSender;
	}

	/**
	 * 获得CP状态发送者
	 * 
	 * @return CP状态发送者
	 */
	public SimpleSender getStatusSender() {
		return statusSender;
	}


	/**
	 * 获得银行信息监听者实例
	 * 
	 * @return 银行信息监听者实例
	 */
	public GKGOHFixListener getGKGOHListener() {
		return gkgohListener;
	}

	public BugRateWorkThread getBugrate() {
		return bugrate;
	}
}
