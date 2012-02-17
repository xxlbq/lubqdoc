package jp.emcom.adv.fx.completor.biz.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import jp.emcom.adv.fx.completor.biz.info.CompletorOrderInfo;
import jp.emcom.adv.fx.completor.handler.OrderCompletorHandler;
import jp.emcom.adv.fx.completor.service.WLCompletorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bestwiz.jhf.core.dao.bean.main.JhfAliveContract;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;
import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.util.CollectionsUtil;

public class Completor implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(Completor.class);
	
	private List<String> customerIdList;

	private Map<String,Map<String,List<JhfWlOrder>>> orderList;
	private WLCompletorService ws;
	

	private static CountDownLatch latch = null;

	
	public void setWs(WLCompletorService ws) {
		this.ws = ws;
	}
	
	public static CountDownLatch getLatch() {
		return latch;
	}
	
	public void setCustomerIdList(List<String> customerIdList) {
		this.customerIdList = customerIdList;
	}
	public Completor(){}

	
	public void run() {
		
		log.info("i am running ..");
//		try {
//			Thread.currentThread().sleep(15* 1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			
			List<JhfWlOrder> allOrd = ((WLCompletorService)ws).queryCompletorOrders(
					customerIdList);
			if(CollectionsUtil.isEmpty(allOrd)){
				log.info("NOT ANY ORDER COMPLETE .");
				return;
			}
			log.info("completor order size={}",allOrd.size());
			latch = new CountDownLatch(allOrd.size());
			List<CompletorOrderInfo> completorOrderInfoList = buildOrderInfo(allOrd,latch);
			
			
			putOrderQueue(completorOrderInfoList);
			
			
		} catch (Exception e) {
			log.error("RUN COPMLETOR ERROR !",e);
			e.printStackTrace();
		}
		try {
			log.info("Completor Job Waiting Finished .");
			latch.await();
			log.info("Completor Job Have Finished .");
			
		} catch (InterruptedException e) {
			if(null!=latch){
				log.info("latch current count is {}",latch.getCount(),e);
			}
			e.printStackTrace();
		}
		
	}

	
	private List<CompletorOrderInfo> buildOrderInfo(List<JhfWlOrder> allOrd,
			CountDownLatch cb) {
		List<CompletorOrderInfo> list = new ArrayList<CompletorOrderInfo>();
		for (JhfWlOrder jhfWlOrder : allOrd) {
			list.add(new CompletorOrderInfo(jhfWlOrder,cb) );
		}
		return list;
	}

	private void putOrderQueue(List<CompletorOrderInfo> allOrd) {
		
		for (CompletorOrderInfo info : allOrd) {
			if(OrderCompletorHandler.getIgnMap().containsKey(
					info.getJhfWlOrder().getClientId() + info.getJhfWlOrder().getCurrencyPair())){
				log.warn("Ignore Order  orderId",info.getJhfWlOrder().getWlOrderId());
				info.getLatch().countDown();
				continue;
			}
//			if(notCompletorOrder(info)){
//				
//				OrderCompletorHandler.getIgnMap().put(
//						info.getJhfWlOrder().getClientId() + info.getJhfWlOrder().getCurrencyPair(), new Object());
//				log.info("Put order In Ignore Cache : order={}",info.getJhfWlOrder());
//				info.getLatch().countDown();
//				continue;
//			}
			
			try {
				OrderCompletorHandler.getQuqueMap().get(info.getJhfWlOrder().getCurrencyPair()).put(info);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
//	/**
//	 *  
//	 * @param info
//	 * @return
//	 */
//	private boolean notCompletorOrder(CompletorOrderInfo info) {
//
//		//ORDER_DATE != CoreService.getFrontDate()
//		String frontdate = null;
//		JhfWlOrder jhfWlOrder = null;
//		try {
//			
//			jhfWlOrder = info.getJhfWlOrder();
//			
//			//status =4 
//			if(jhfWlOrder.getCompletionStatus().intValue() == CompletionStatusEnum.ERROR.getIntValue()){
//				return true;
//			}
//			frontdate = ServiceFactory.getCoreService().getFrontDate();
//		} catch (CoreException e) {
//			log.error("CoreService().getFrontDate() fire error ! ",e);
//			return true;
//		}
//		if( ! jhfWlOrder.getOrderDate().equals( frontdate)){
//			return true;
//		}
//		return false;
//	}



//	public static void main(String[] args) throws JMSException, IOException, InterruptedException {
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(Completor.class.getResourceAsStream("QKeyPackage.xml")));
//		
//		XStream xstream = new XStream(new DomDriver());
//		
////		xstream.processAnnotations(QKeyPackage.class);
////		xstream.processAnnotations(QKey.class);
////
////		
////		QKeyPackage request = (QKeyPackage)xstream.fromXML(br);
////		br.close();
//		
////		for (QKey k : request.get) {
////			System.out.println(k.getPairList());
////		}
//
//	}

	
	
	public void complete() {
		
		
		for (Map<String,List<JhfWlOrder>> customerOrders: orderList.values()) {
			try {
//				DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
				netCustomerOrders(customerOrders);
//				DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
			} catch (Exception e) {
				log.error("error fire in loop :"+ e);
//				DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
				continue;
			}
		}
		
	}
	private void netCustomerOrders(Map<String, List<JhfWlOrder>> customerOrders) {

		for (List<JhfWlOrder> nettingList : customerOrders.values()) {
			String cid =nettingList.get(0).getClientId();
			String ccy=nettingList.get(0).getCurrencyPair();
			if(isIngnoreOrder(cid,ccy)){
//				log something
				continue;
			}
			List<JhfAliveContract> clist = queryAliveContract(cid,ccy);
			if(isTwoSideContract(clist)){
				addIgnoreOrder(cid,ccy);
//				log something
				continue;
			}
			
			if(doNetting(clist,nettingList)){
				addIgnoreOrder(cid,ccy);
//				log something
			}
			
		}
	}
	
	
//	
//	private boolean doNetting(List<JhfAliveContract> clist,
//			List<WLOrder> nettingList) {
//		
//		for(WLOrder worder:nettingList){
//			for (JhfAliveContract contract : clist) {
//				try {
////					JhfAliveContract contract = null;
////					WLOrder wlord = null;
//					if(contract.getSide() != worder.getSide()){
//						
//						netTwoSide(contract,worder);
//					}else{
//						addOneSide(contract,worder);
//					}
//				} catch (Exception e) {
//					return false;
//				}
//			}
//
//		}
//		return true;
//		
//	}

	
	private boolean doNetting(List<JhfAliveContract> clist,
			List<JhfWlOrder> nettingList) {
		
		for(JhfWlOrder worder:nettingList){
			for (JhfAliveContract contract : clist) {
				try {
//					JhfAliveContract contract = null;
//					WLOrder wlord = null;
					if(contract.getSide() != worder.getSide()){
						
						netTwoSide(contract,worder);
					}else{
						addOneSide(contract,worder);
						break;
					}
				} catch (Exception e) {
					return false;
				}
			}

		}
		return true;
		
	}
	
//	private NetResult fireNet(List<JhfAliveContract> clist,JhfWlOrder worder) {
//		
//			for (JhfAliveContract contract : clist) {
//				try {
//
//					if(contract.getSide() != worder.getSide()){
//						
//						netTwoSide(contract,worder);
//					}else{
//						addOneSide(contract,worder);
//					}
//					
////					if(worder.getAmount() == 0){
////						return new NetResult(null, clist);
////					}
//					
//				} catch (Exception e) {
////					return false;
//					return null;
//				}
//			}		
//			
//			return null;
//	}
	
	
	
	
	private void addOneSide(JhfAliveContract contract, JhfWlOrder worder) {
		// TODO Auto-generated method stub
		
	}
	private void netTwoSide(JhfAliveContract contract, JhfWlOrder wlord) {
		try{

			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			fixOneSide(contract,wlord);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);

		}catch (Exception e) {
			log.error("===== Initlize MAIN DB connection Errors: ", e);
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
		}
		
	}

	private void fixOneSide(JhfAliveContract contract, JhfWlOrder wlord) {
		// TODO Auto-generated method stub
		
	}

	private boolean isIngnoreOrder(String cid, String ccy) {
		// TODO Auto-generated method stub
		return false;
	}

	private void addIgnoreOrder(String cid, String ccy) {
		// TODO Auto-generated method stub
		
	}

	private boolean isTwoSideContract(List<JhfAliveContract> clist) {
		// TODO Auto-generated method stub
		return false;
	}
	private List<JhfAliveContract> queryAliveContract(String cid, String ccy) {
		// TODO Auto-generated method stub
		return null;
	}

}
