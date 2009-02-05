package cn.bestwiz.jhf.trader.trader.listener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.bo.enums.CustTraderModeEnum;
import cn.bestwiz.jhf.core.bo.enums.ExecutionTypeEnum;
import cn.bestwiz.jhf.core.bo.enums.ManualStatusEnum;
import cn.bestwiz.jhf.core.bo.enums.SideEnum;
import cn.bestwiz.jhf.core.bo.enums.SuccessFlagEnum;
import cn.bestwiz.jhf.core.configcache.ConfigService;
import cn.bestwiz.jhf.core.configcache.ConfigServiceImpl;
import cn.bestwiz.jhf.core.dao.bean.main.JhfCustomerBlacklist;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.bean.CoverRequestInfo;
import cn.bestwiz.jhf.core.jms.bean.FxSpotRateInfo;
import cn.bestwiz.jhf.core.jms.bean.OrderBindInfo;
import cn.bestwiz.jhf.core.jms.bean.OrderResponseInfo;
import cn.bestwiz.jhf.core.jms.bean.OrderResponseListInfo;
import cn.bestwiz.jhf.core.ratecache.RateCacheFactory;

import cn.bestwiz.jhf.core.service.ServiceFactory;
import cn.bestwiz.jhf.core.service.exception.TradeException;
import cn.bestwiz.jhf.core.util.BeanCopy;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.core.util.PropertiesLoader;
import cn.bestwiz.jhf.trader.trader.DefaultOrderService;
import cn.bestwiz.jhf.trader.trader.business.ExecWriter;
import cn.bestwiz.jhf.trader.trader.exception.ExecWriterException;

/**
 * 接收JMS消息,将消息发送给Hedger
 * 
 * @author zuolin <zuolin@bestwiz.cn>
 * 
 * @created 2006-8-9 17:16:29
 * 
 * @copyright 2006, BestWiz(Dalian) Co.,Ltd
 * 
 * @version $Id: OrderRequestListener.java,v 1.16 2007/10/17 03:14:15 zuolin Exp $
 */
public class OrderRequestListener implements SimpleCallback {

    // 定义log
    private final static Log m_log = LogUtil.getLog(OrderRequestListener.class);
    // 定义线程池
    private ExecutorService m_executor = null;
    // 定义DefaultOrderService
    private static DefaultOrderService m_defaultOrderService = null;
    // 定义properties
    private Properties m_props = null;
    // 定义thread pool file 
    private final static String PROPS_FILE_NAME = "trader_thread_pool.properties";
    // 定义ExecWriter
    private static ExecWriter m_execWriter = null;
    private static ConfigService cfgService = null;
    // 初始化成员变量
    public OrderRequestListener() throws Exception{
    	try {
    		m_defaultOrderService = DefaultOrderService.getInstance();
    		m_execWriter = ExecWriter.getInstance();
    		cfgService = ServiceFactory.getConfigService();
    		// 读取线程池大小的配置文件,如果配置文件<0,使用一个默认值
    		m_props = PropertiesLoader.getProperties(PROPS_FILE_NAME);
    		int iPoolSize = Integer.valueOf(m_props.getProperty("ORDERREQUEST_POOL_SIZE"));
    		if (iPoolSize < 1 ){
    			iPoolSize =5;
    		}
    		m_executor = Executors.newFixedThreadPool(iPoolSize);
    	} catch (Exception e) {
    		m_log.error("OrderRequestListener initlize Exception: ",e);
    		throw e;
    	}
    }
    
    /**
     * 接收front opm losscut mobile的交易消息． 
     * @author zuolin<zuolin@bestwiz.cn>
     * */
	public void onMessage(Serializable message) {
        if (null == message) {
            m_log.error("==== OrderBindInfo Message From Provider is null");
            return;
        }
        if (!(message instanceof OrderBindInfo)) {
        	m_log.error("==== OrderBindInfo Message From Provider is not instance of OrderBindInfo");
        	return;
        }

        final OrderBindInfo orderBindInfo = (OrderBindInfo)message;
        
        if (m_log.isInfoEnabled() || m_log.isDebugEnabled()) {
        	m_log.info(" OrderBind Info  = " + orderBindInfo.toString());
        }
        m_executor.execute(new Runnable() {
            public void run() {
            	try {
            		checkCustTradeMode(orderBindInfo);
				} catch (Exception e) {
					m_log.error("=== Send to hedger Errors ",e);
				}
            }
        });
	}

    /**
     * 关闭线程池的方法 
     * @author zuolin<zuolin@bestwiz.cn>
     * */
	public void close() {
        try {
            m_executor.shutdown();
        } catch (Exception e) {
            m_log.error(" close() error",e);
        }
	}

	/**
	 * 接收到front opm losscut mobile的交易消息,转发给hedger的方法
	 * @param  OrderBindInfo  (orderBindInfo) front opm losscut mobile的交易消息
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private void sendToCover(OrderBindInfo orderBindInfo) throws Exception  {
		try {
			m_log.info("Send To Cover Start:");
        	// 设置CoverFireInfo
        	CoverRequestInfo coverRequestInfo = new CoverRequestInfo();
        	coverRequestInfo.setCurrencyPair(orderBindInfo.getCurrencyPair());
        	coverRequestInfo.setSide(orderBindInfo.getSide());
        	coverRequestInfo.setAmount(orderBindInfo.getAmount());
        	coverRequestInfo.setTradeAskPrice(orderBindInfo.getTradeAskPrice());
        	coverRequestInfo.setTradeBidPrice(orderBindInfo.getTradeBidPrice());
        	coverRequestInfo.setOrderBindId(orderBindInfo.getOrderBindId());
        	coverRequestInfo.setExectionType(orderBindInfo.getType());
        	coverRequestInfo.setOrderId(orderBindInfo.getOrderId());
        	coverRequestInfo.setPriceId(orderBindInfo.getPriceId());
        	coverRequestInfo.setMode(orderBindInfo.getMode());
        	coverRequestInfo.setLimitOrderMap(orderBindInfo.getLimitOrderMap());
        	coverRequestInfo.setStopOrderFlag(orderBindInfo.getStopOrderFlag());
        	// 发送JMS To Cover模块
			m_defaultOrderService.getToCoverSender().sendMessage(coverRequestInfo);	
			if (m_log.isInfoEnabled() || m_log.isDebugEnabled()) {
				m_log.info(" send to cover : coverRequestInfo  = " + coverRequestInfo.toString());
			}
		} catch (Exception e){
			m_log.error("Send Cover Errors",e);
		}
	}
	
	
	/**
	 * 1.对注文的类型进行判断,如果是OPM的注文,根据OPM传过来的汇率带上是否为manaual汇率的标志,如果是manaul则直接进行约定,
	 *   然后发邮件通知CS人员与银行进行电话交易;否则判断交易模式是否为驻留持仓,如果是直接做交易,不写JHF_EXECUTION_BIND表,
	 *   但是插入到JHF_HEDGER_CUSTTRADE表;否则直接发给hedger
	 * 2.取得交易汇率,如果取得的汇率无效或是不可能交易,如果注文是时间成行,进行reverseFillOrder处理;如果是losscut注文则
	 *   没有untradable.
	 * 3.普通成行和一恬决济需要进行Slippage的判断.如果超过返回失败的消息.
	 * 4.判断是否为电话交易,电话交易则直接进行约定.
	 * 5.判断是否为驻留模式或是自设模式,如果是直接进行约定;否则直接发给hedger(普通模式和注留模式都由trader将消息发给hedger)  
	 * */
	private void checkCustTradeMode(OrderBindInfo orderBindInfo) throws Exception{
		try {
			
			/** OPM注文直接发送到hedger,不需要进行price检查,其他注文需要进行价格的检查.*/ 
			if (orderBindInfo.getType() != ExecutionTypeEnum.EXEC_OTHER_ENUM.getValue()) {
				// 一恬决济 标志
				boolean isBatch = false;
				OrderResponseInfo Info = null;
				OrderResponseListInfo lstInfo = null;
				if(orderBindInfo.getType() == 
					ExecutionTypeEnum.EXEC_BATCH_REALTIME_ENUM.getValue()) {
					isBatch = true;
					lstInfo = new OrderResponseListInfo();
					lstInfo.setOrderBindId(orderBindInfo.getOrderBindId());
				}  else {
					Info = new OrderResponseInfo();
					Info.setOrderId(orderBindInfo.getOrderId());
				}
				
				/** 控制前台发过来的amount 如果为0 返回失败的消息*/
				if (orderBindInfo.getAmount().compareTo(BigDecimal.ZERO) == 0) {
					if (orderBindInfo.getType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
        				m_log.error("orderBindInfo amount is zero !" + orderBindInfo.getOrderBindId());
        				Info.setSuccessFlag(false);
        				Info.setErrorCode(TradeException.FILL_ORDER_FAILURE);
        				Info.setErrorMsg("orderBindInfo amount is zero !!");
						sendToRtOrder(Info);
						return;
					} else if (orderBindInfo.getType() == ExecutionTypeEnum.EXEC_BATCH_REALTIME_ENUM.getValue()) {

						m_log.error("orderBindInfo amount is zero !");
						lstInfo.setSuccessFlag(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
						lstInfo.setErrorCode(TradeException.FILL_ORDER_FAILURE);
						lstInfo.setErrorMsg("orderBindInfo amount is zero !");
						sendToRtlistOrder(lstInfo);
						return;
					}
				}
				
				// 取得汇率,如果第一次tradable为false,再进行一次.
				FxSpotRateInfo fxSpotRateInfo = null;
				for(int i = 0; i < 2; i++) {
		        	// 从ratecache中取得最新汇率,需要取得附本.
					fxSpotRateInfo = (FxSpotRateInfo) BeanCopy.copy(
							RateCacheFactory.getRatecache().getFxSpotRate(orderBindInfo.getCurrencyPair()));
					if (fxSpotRateInfo == null) {
						m_log.error("Get Rate ERRORS!");
						if (isBatch) {
							m_log.error("Get Rate ERRORS!");
							lstInfo.setSuccessFlag(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
							lstInfo.setErrorCode(TradeException.ORDER_RATE_DISADVANTAGE_ERROR);
							lstInfo.setErrorMsg("Get Rate ERRORS!");
							sendToRtlistOrder(lstInfo);
							
						} else {
							// 只有成行注文发消息
							if (orderBindInfo.getType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
								Info.setSuccessFlag(false);
								Info.setErrorCode(TradeException.ORDER_RATE_DISADVANTAGE_ERROR);
								Info.setErrorMsg("Get Rate ERRORS!");
								sendToRtOrder(Info);	
							}
						}
						
						// 如果是时间成行注文，进行注文修复
						if (orderBindInfo.getType() == ExecutionTypeEnum.TIMEORDER_ENUM.getValue()) {
							m_execWriter.reverseFillOrder(orderBindInfo.getOrderBindId());
						}
						return;
					}
					
					/** losscut注文时，没有untradable这个限制， 2007-05-18 邮件*/
					if (orderBindInfo.getType() != ExecutionTypeEnum.EXEC_LOSS_CUT_ENUM.getValue()) {
						if(!fxSpotRateInfo.isTradableFlag()) {
							if (i == 1) {
								if (isBatch) {
									// 将错误信息返回;
									lstInfo.setSuccessFlag(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
									lstInfo.setErrorMsg("RATE TRADEBLE FALSE!");
									lstInfo.setErrorCode(TradeException.ORDER_RATE_TRABLE_FALSE);
			        				// 向TOMCATE发送JSM
			        				sendToRtlistOrder(lstInfo);
								} else {
									// 只有成行注文发消息
									if (orderBindInfo.getType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
										// 将错误信息返回;
										Info.setSuccessFlag(false);
										Info.setErrorMsg("RATE TRADEBLE FALSE!");
										Info.setErrorCode(TradeException.ORDER_RATE_TRABLE_FALSE);
										// 向TOMCATE发送JSM
										sendToRtOrder(Info);
									}
								}
								
								// 如果是时间成行注文，进行注文修复
								if (orderBindInfo.getType() == ExecutionTypeEnum.TIMEORDER_ENUM.getValue()) {
									m_execWriter.reverseFillOrder(orderBindInfo.getOrderBindId());
								}
								
								return;
								
							} else {
								continue;
							}
						} else {
							break;
						}
					} else {
						break;
					}

				}
				
				if (orderBindInfo.getType() == ExecutionTypeEnum.EXEC_BATCH_REALTIME_ENUM.getValue() 
						|| orderBindInfo.getType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
					BigDecimal bdRate = null;
					BigDecimal bdOrderBindPrice = null;
					// 判断买卖
					if (orderBindInfo.getSide() == SideEnum.SIDE_BUY.getValue()) {
						bdRate = fxSpotRateInfo.getAskRate();
						bdOrderBindPrice = orderBindInfo.getTradeAskPrice();
					}
					
					if (orderBindInfo.getSide() == SideEnum.SIDE_SELL.getValue()) {
						bdRate = fxSpotRateInfo.getBidRate();
						bdOrderBindPrice = orderBindInfo.getTradeBidPrice();
					}
					
					/**
					 * 如果ExecutionType为成行的,或是一恬决济,
					 * 且MobileFlag不为true(front 的前台注文)
					 * 需要对注文的slippage进行校验
					 * 与周周确认,只是这两条注文不论汇率的类型都要进行汇率的检查
					 * */ 
					if (!orderBindInfo.isMobileFlag()) {
						/** 检查NowRate对顾客是否有利
						 * 顾客ASK时：系统当前的对顾价格 <= 顾客的注文价格;
						 * 顾客BID时：系统当前的对顾价格 >= 顾客的注文价格;
						 * 如果有利:用顾客注文价格直接约定,不考虑slippage范围,priceId为 orderBindInfo的priceId
						 *    否则:通过的判断：abs(OrderPrice-NowRate) <= Slippage。约定的价格：系统当前的对顾价格。
						 *  */ 
	            		m_log.info(orderBindInfo.getOrderBindId()
							+ "|" + orderBindInfo.getOrderId()
							+ "|" + orderBindInfo.getCustomerId()
							+ "|new rate=" + bdRate
							+ "|orderprice=" + bdOrderBindPrice
							+ "|new priceId=" + fxSpotRateInfo.getPriceId()
							+ "|orderBind priceId=" + orderBindInfo.getPriceId()
							+ "|side=" + orderBindInfo.getSide());
	            		
	            		if (!checkOutstandingPrice(bdRate, bdOrderBindPrice, orderBindInfo.getSide())) {
							//|最新汇率 - 新规注文的交易价格| > 0 超出范围,RealTime注文无效
							BigDecimal bdRateMargin = bdRate.subtract(bdOrderBindPrice).abs();
							// 检查汇率是否超出范围
							if (bdRateMargin.compareTo(orderBindInfo.getSlippage()) > 0) {
								if (isBatch){
									// 超出返回
									lstInfo.setSuccessFlag(SuccessFlagEnum.ALL_FAILURE_ENUM.getValue());
									lstInfo.setErrorCode(TradeException.REALTIME_SLIPPAGE_ERROR);
									lstInfo.setErrorMsg(" RealTime Order Is Invalid!");
									// 针对一恬决济时的处理 返回的List有可能为null 需要FRONT进行判断是否为null后再进行相关的处理
									ArrayList<OrderResponseInfo> responseList = new ArrayList<OrderResponseInfo>();
									OrderResponseInfo response = new OrderResponseInfo();
									response.setPriceId(orderBindInfo.getPriceId());
									responseList.add(response);
									lstInfo.setResponseList(responseList);
									m_log.info("OrderRequest Check RealTime , orderId = " + orderBindInfo.getOrderId()+
											", New Rate = " + bdRate + ",Ordle Price = " + bdOrderBindPrice);
									sendToRtlistOrder(lstInfo);
								} else {
									// 超出返回
									Info.setSuccessFlag(false);
									Info.setErrorCode(TradeException.REALTIME_SLIPPAGE_ERROR);
									Info.setErrorMsg(" RealTime Order Is Invalid!");
									Info.setPriceId(orderBindInfo.getPriceId());
									m_log.info("OrderRequest Check RealTime , orderId = " + orderBindInfo.getOrderId()+
											", New Rate = " + bdRate + ",Ordle Price = " + bdOrderBindPrice);
									sendToRtOrder(Info);
								}
								return;
							}	
							
							/**设置priceid**/
							orderBindInfo.setPriceId(fxSpotRateInfo.getPriceId());
							/** 所有注文都以最新汇率进行约定,分买卖方向*/ 
							orderBindInfo.setTradeAskPrice(fxSpotRateInfo.getAskRate());
							orderBindInfo.setTradeBidPrice(fxSpotRateInfo.getBidRate());
	            		}
					}
				} else {
					/**设置priceid**/
					orderBindInfo.setPriceId(fxSpotRateInfo.getPriceId());
					/** 所有注文都以最新汇率进行约定,分买卖方向*/ 
					orderBindInfo.setTradeAskPrice(fxSpotRateInfo.getAskRate());
					orderBindInfo.setTradeBidPrice(fxSpotRateInfo.getBidRate());
				}
				
				// 设置Manual汇率
				boolean bFlag = false;
				if (fxSpotRateInfo.getInManualStatus() == 
					ManualStatusEnum.IN_MANUAL_STATUS.getValue() ||
					orderBindInfo.getMode() == CustTraderModeEnum.SELF_MODE_ENUM.getValue()) 
					bFlag = true;
				
				//获得此注文的customer在 JHF_CUSTOMER_BLACKLIST 的对应状态来判断是否可以滞留
				boolean canStayPosition = cfgService.canCustomerStayPosOnSide(
						orderBindInfo.getCustomerId(),orderBindInfo.getSide(),orderBindInfo.getType());
				//如果注文是 成行注文，并且模式是 滞留模式，并且根据 JHF_CUSTOMER_BLACKLIST 中记录 验证其不可以滞留 ，则修改其交易模式为：普通模式。
				if( orderBindInfo.getMode() == CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue() 
						&& orderBindInfo.getType() == ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()
						&& (! canStayPosition )){
					orderBindInfo.setMode( CustTraderModeEnum.NORMAL_MODE_ENUM.getValue() );
				}
				
				//新增加顾客黑名单的功能,作用是对特定的顾客,即使货币对及注文类型设定成了滞留,也不对该顾客进行滞留,还是按照通常模式进行
				
				
				/** 如果是Manual汇率直接进行交易,否则判断是否为驻留模式 */ 
				if (bFlag) {
					m_log.info("[Trader Order Manual Mode]" + orderBindInfo.getOrderBindId() 
							+ "/" + orderBindInfo.getCurrencyPair());
					if (isBatch) {
						lstInfo = sendToBtExecWriter(orderBindInfo, bFlag, null);
						sendToRtlistOrder(lstInfo);
						return ;
					} else {
		       			if (sendToExecWriter(orderBindInfo, bFlag, null)) {
	        				// 只有成行注文发消息
	        				if (orderBindInfo.getType() 
	        						== ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
	        					Info.setSuccessFlag(true);
	            				sendToRtOrder(Info);
	        				}
	        				return;
						} else {
	        				// 只有成行注文发消息
	        				if (orderBindInfo.getType() 
	        						== ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
	        					Info.setSuccessFlag(false);
	        					Info.setErrorMsg("FILL ORDER FAILURE!");
	        					Info.setErrorCode(TradeException.FILL_ORDER_FAILURE);
	        					Info.setPriceId(orderBindInfo.getPriceId());
	    						sendToRtOrder(Info);
	        				}
							return ;
						}
					}
				} else if (orderBindInfo.getMode() == CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue()) {
	
						m_log.info("[Trader Order Stay Position Mode]" + orderBindInfo.getOrderBindId() 
								+ "/" + orderBindInfo.getCurrencyPair() + "/" + orderBindInfo.getMode());
						if (isBatch) {
							lstInfo = sendToBtExecWriter(orderBindInfo, bFlag, null);
							sendToRtlistOrder(lstInfo);
							return ;
						} else {
			       			if (sendToExecWriter(orderBindInfo, bFlag, null)) {
		        				// 只有成行注文发消息
		        				if (orderBindInfo.getType() 
		        						== ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
		        					Info.setSuccessFlag(true);
		            				sendToRtOrder(Info);
		        				}
		        				return;
							} else {
		        				// 只有成行注文发消息
		        				if (orderBindInfo.getType() 
		        						== ExecutionTypeEnum.EXEC_INSTANT_ENUM.getValue()) {
		        					Info.setSuccessFlag(false);
		        					Info.setErrorMsg("FILL ORDER FAILURE!");
		        					Info.setErrorCode(TradeException.FILL_ORDER_FAILURE);
		        					Info.setPriceId(orderBindInfo.getPriceId());
		    						sendToRtOrder(Info);
		        				}
								return ;
							}
						}
						
					
				} else {
					m_log.info("[Trader Order Normal Mode]" + orderBindInfo.getOrderBindId() 
							+ "/" + orderBindInfo.getCurrencyPair() + "/" + orderBindInfo.getMode());
					sendToCover(orderBindInfo);
				}
			} else {
				/**
				 * 这里只处理opm注文
				 * 1.根据OPM传过来的汇率带上是否为manaual汇率的标志,如果是manaul则直接进行约定,然后发邮件通知CS人员与银行进行电话交易,否则发给hedger进行交易.
				 * ２.如果不是电话交．判断orderbindinfo消息中的mode类型
				 *    如果是自设交易,直接发给hedger
				 *    如果是普通模式,直接发给hedger
				 *    如果是驻留模式,不发给hedger,不写JHF_EXECUTION_BIND表
				 * **/ 
				if (orderBindInfo.getInManualStatus()== ManualStatusEnum.IN_MANUAL_STATUS.getValue() || 
					orderBindInfo.getMode() == CustTraderModeEnum.SELF_MODE_ENUM.getValue()) {
					m_log.info("[Trader Opm Order Manual Mode Start]" + orderBindInfo.getOrderBindId() + "|"+orderBindInfo.getInManualStatus());
					sendToExecWriter(orderBindInfo, true, null);
					m_log.info("[Trader Opm Order Manual Mode End]"+ orderBindInfo.getOrderBindId() + "|"+orderBindInfo.getInManualStatus());
				} else {
					if (orderBindInfo.getMode() == CustTraderModeEnum.STAY_POSITION_MODE_ENUM.getValue()) {
						m_log.info("[Trader Opm Order Stay position Mode Start]" + orderBindInfo.getOrderBindId() + "|"+ orderBindInfo.getMode());
						sendToExecWriter(orderBindInfo, false, null);
						m_log.info("[Trader Opm Order Stay position Mode End]"+ orderBindInfo.getOrderBindId() + "|"+ orderBindInfo.getMode());
					} else {
						m_log.info("[Trader Opm Order Normal Mode Send to Hedge]" + orderBindInfo.getOrderBindId() + "|"+ orderBindInfo.getMode());
						sendToCover(orderBindInfo);
					}
				}
			}
		} catch (Exception e) {
			m_log.error("checkRateInfo error",e);
		}
	}
	
	/**
	 * 交易成功或是失败后发送结果消息的方法
	 * @param  OrderBindInfo  (orderBindInfo) JMS消息
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private void sendToRtOrder(OrderResponseInfo orderResponseInfo) throws Exception  {
		try {
			m_log.info("Send To Tomcat Start:");
			m_defaultOrderService.getRealTimeSender().sendMessage(orderResponseInfo);
			if (m_log.isInfoEnabled() || m_log.isDebugEnabled()) {
				m_log.info("Send To Tomcat End:"+ orderResponseInfo.toString());			
			}
		} catch (Exception e){
			m_log.error("Send OrderResponseInfo To Tomcat Errors",e);
		}
	}
	
	/**
	 * 交易成功或是失败后发送结果消息的方法
	 * @param  OrderResponseListInfo 
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private void sendToRtlistOrder(OrderResponseListInfo JmsInfo) throws Exception  {
		try {
			m_log.info("Send To Tomcat Start:");
			m_defaultOrderService.getBtRealTimeSender().sendMessage(JmsInfo);
			if (m_log.isInfoEnabled() || m_log.isDebugEnabled()) {
				m_log.info("Send To Tomcat End:"+ JmsInfo.toString());
			}
		} catch (Exception e){
			m_log.error("Send OrderResponseListInfo To Tomcat Errors",e);
		}
	}
	
	
	/**
	 * 一恬决济时调用ExecWriter
	 * @param  OrderBindInfo 收到front opm losscut mobile的交易消息．
	 * @param  boolean true  电话交易
	 *                 false 非电话交易
	 * @param  String  sCpExecutionId hedger与GW进行交易的约定id,如果电话交易则为null.  
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private OrderResponseListInfo sendToBtExecWriter(OrderBindInfo orderBindInfo,
			boolean bFlag, String sCpExecutionId) {
		m_log.debug("Send To EXW" + orderBindInfo.toString());
		return m_execWriter.doBtProcess(orderBindInfo, bFlag, sCpExecutionId);
	}
	
	
	/**
	 * 调用ExecWriter
	 * @param  OrderBindInfo 收到front opm losscut mobile的交易消息．
	 * @param  boolean true  电话交易
	 *                 false 非电话交易
	 * @param  String  sCpExecutionId hedger与GW进行交易的约定id,如果电话交易则为null.                 
	 * @return boolean true  交易成功
	 * 　　　　　　　　　　fa;se 交易失败                
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private boolean sendToExecWriter(OrderBindInfo orderBindInfo, boolean bFlag ,String sCpExecutionId) {
		m_log.debug("Send To EXW" + orderBindInfo.toString());
		try {
			m_execWriter.doProcess(orderBindInfo,bFlag,sCpExecutionId);
			return true;
		} catch (ExecWriterException e) {
			m_log.error("Error In Send Order Response Message To EXW)", e);
			return false;
		}
	}

	/**
	 * RealTime注文的Slippage判断
	 * 顾客ASK时：系统当前的对顾价格 <= 顾客的注文价格;
     * 顾客BID时：系统当前的对顾价格 >= 顾客的注文价格;
	 * @param  
	 * @author zuolin<zuolin@bestwiz.cn>
	 * */
	private boolean checkOutstandingPrice(BigDecimal newRate, BigDecimal orderPrice, int side) throws Exception {
		if (side == SideEnum.SIDE_BUY.getValue()) {
			return (newRate.compareTo(orderPrice) <=0);
		} else if (side == SideEnum.SIDE_SELL.getValue()) {
			return (newRate.compareTo(orderPrice) >=0);
		} 
		return false;
	}
}
