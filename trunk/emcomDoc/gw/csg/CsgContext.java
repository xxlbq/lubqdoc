package cn.bestwiz.jhf.gws.csg;

import java.io.IOException;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.gws.common.callback.AsynchCallback;
import cn.bestwiz.jhf.gws.common.callback.IMessageCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgAbstractCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgFxRequestCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgRateCallback;
import cn.bestwiz.jhf.gws.csg.callback.CsgTradeCallback;
import cn.bestwiz.jhf.gws.csg.config.CsgProperty;
import cn.bestwiz.jhf.gws.csg.util.Times;

import com.pst.bean.com.CustomerCCYPair;
import com.pst.bean.com.OrderTicket;
import com.pstcl.clientsys.PSTClientContext;
import com.pstcl.clientsys.common.exception.BusinessException;
import com.pstcl.clientsys.fin.Reraize;
import com.pstcl.clientsys.order.server.PSTCResTask;
import com.pstcl.clientsys.rate.server.PSTCRateTask;
import com.pstcl.clientsys.timer.HBWatchTask;

public class CsgContext {
	private static Log log = LogUtil.getLog(CsgContext.class);
	
	//PSTClientコンテキスト作成 
	private static PSTClientContext context = null;
	public static PSTClientContext getContext() {
		return context;
	}




	private static boolean isConnect ;
	private static boolean isSubscribe ;
	
	private Reraize reraize = Reraize.getReraize();
	private static CsgProperty prop ;
	
	public static CsgProperty getProp() {
		return prop;
	}




	private static CsgAbstractCallback rateCallback = null;





	private static IMessageCallback tradeCallback = null;
	
	public static void init(){
		prop = new CsgProperty();
		rateCallback	= new CsgRateCallback(prop.getRateCount());
		tradeCallback	= new CsgTradeCallback(prop.getTradeCount());
		
	}
	
	public static void connect(){
		
		try {
			PSTClientContext.initContext();
			log.info("==== connect() begin");
			
			context		= PSTClientContext.getContext();
			//
			context.setRateTask(new PSTCRateTask() {
				
				@Override
				protected void onReceiveCCYPair(CustomerCCYPair ccyPair) {
					// 
					log.info("==== onReceiveCCYPair");
					((CsgRateCallback)rateCallback).enQueue(ccyPair);
				}
			});

			
			//
			context.setResTask(new PSTCResTask() {
				
				@Override
				protected void onReceiveResult(OrderTicket orderTick) {
					// 
					log.info("==== onReceiveResult:"+ReflectionToStringBuilder.toString(
							orderTick, ToStringStyle.MULTI_LINE_STYLE, true, true));
					((AsynchCallback) tradeCallback).enQueue(orderTick);
				}
			});
			
			
			
			log.info("connectToBridge ");
			//
			isConnect 	= context.connectToBridge();
//			context.getPhoenix().dealFinish()
			if(isConnect){
				log.info("==== PSTClientContext is Connect ");
				
//				for (String pair : prop.getPairs()) {
//					isSubscribe = context.subscribe(pair);
//					log.info("Pair:" + pair + ",Subscribe:["+isSubscribe + "]");
//				}
				
				context.subscribe("USD/JPY");
				Times t = new Times(context);
				t.startTimer();

			}else{
				log.info("==== PSTClientContext lose Connect ");
			}
			
//			CsgAbstractCallback c = new CsgFxRequestCallback(10);
//			
//			((CsgFxRequestCallback)c).dealItemTest(context);
			
			
//			reraize.join();
			log.info("==== connect() end");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	
	
	
	public static void main(String[] args) {
		
		
//		CsgContext.init();
//		CsgContext.connect();
		
		
		
//		for(int i=0;i<100;i++){
//			
//			CustomerCCYPair p = new CustomerCCYPair();
//			p.setOAmount(new BigDecimal(i));
//			((CsgRateCallback)CsgConnector.rateCallback).enQueue(p);
//			System.out.println("element "+i);
//		}
		
		
		
//		for (String pair : CsgProperty.getPairs()) {
//			log.info("Pair:" + pair );
//		}

		

	}
}
