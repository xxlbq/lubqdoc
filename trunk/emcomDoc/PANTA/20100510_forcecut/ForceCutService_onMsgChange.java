package cn.bestwiz.jhf.trader.forcecut;

import java.io.Serializable;

import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;

import cn.bestwiz.jhf.core.dao.util.DbSessionFactory;
import cn.bestwiz.jhf.core.formula.FormulaFactory;
import cn.bestwiz.jhf.core.jms.DestinationConstant;
import cn.bestwiz.jhf.core.jms.SimpleCallback;
import cn.bestwiz.jhf.core.jms.SimpleReceiver;
import cn.bestwiz.jhf.core.jms.exception.JMSException;
import cn.bestwiz.jhf.core.util.LogUtil;
import cn.bestwiz.jhf.trader.forcecut.bean.DeficientStatus;
import cn.bestwiz.jhf.trader.forcecut.bean.McutFcutInfo;
import cn.bestwiz.jhf.trader.forcecut.checker.DeficientOnChangeService;

public class ForceCutService {
	
	private static final Log log = LogUtil.getLog(ForceCutService.class);
	private DeficientOnChangeService deficientService = null;
	private boolean isStop = false;
	private SimpleReceiver mcReceiver = null;
	
	public void start() throws Exception {
    	initializeDB();
    	initFormula();
    	initService();
//    	startService();
	}


//	private void startService() {
//		while(isStop == false){
//			try{
//				log.info("Forcecut receiveMessage  ");
//				ObjectMessage om = (ObjectMessage)(mcReceiver.receiveMessage());
//				
//				if(null == om){
//					log.info("ObjectMessage is null ,Forcecut receive next ...");
//					continue;
//				}
//				
//				Serializable message = om.getObject();
//				if(message == null){
//					log.info("message is null ,Forcecut receive next ...");
//					continue;
//				}
//
//				if(message instanceof McutFcutInfo){
//					log.info("received mc msg! ref="+message);
//					McutFcutInfo info = (McutFcutInfo)message;
//					McutFcutInfo replyMessage = null;
//					try {
//						if(DeficientStatus.DEFICIENT.getValue() == info.getStatus().getValue()){
//							log.info("receive [NORMAL] to [DEFICIENT] msg ,cid:"+info.getCustomerId());
//							deficientService.normal2Deficient(info.getCustomerId());
//							replyMessage = buildReplyMessage(info);
//							
//						}else if(DeficientStatus.NORMAL.getValue() == info.getStatus().getValue()){
//							log.info("receive [DEFICIENT] to [NORMAL] msg ,cid:"+info.getCustomerId());
//							deficientService.deficient2Normal(info.getCustomerId());
//							replyMessage = buildReplyMessage(info);
//							
//						}else if(DeficientStatus.FORCECUT.getValue() == info.getStatus().getValue()){
//							log.info("receive [DEFICIENT] to [FORCECUT] msg ,cid:"+info.getCustomerId());
//							deficientService.deficient2Forcecut(info, mcReceiver, om);
//							replyMessage = buildReplyMessage(info);
//							
//						}else {
//							log.error(" error type:"+info.getStatus().getValue());
//						}
//
//						mcReceiver.sendReplyMessage(om, replyMessage);
//						
//					} catch (Exception e) {
//						log.error("======================  ForceCutService INIT ERROR  ! ",e);
//					}
//
//					
//				}else{
//					log.error("forcecut recieve A NOT valid msg! ref="+message);
//				}
//			
//				
//			}catch(Exception dx){
//				dx.printStackTrace();
//				log.error("CheckerWithJms err!" , dx);
//			}
//		}
//	}
		


	private McutFcutInfo buildReplyMessage(McutFcutInfo info) {
		log.info("build reply msg ,cid:"+info.getCustomerId());
		return info;
	}


	private  void initService() throws JMSException {
		deficientService = new DeficientOnChangeService();
		mcReceiver = new SimpleReceiver(DestinationConstant.McutFcutQueue,true,true);
		mcReceiver.addCallback(new SimpleCallback() {
			
			public void onMessage(Serializable message) {
				try{
					log.info("Forcecut receiveMessage  ");
//					ObjectMessage om = (ObjectMessage)sm;
//					
//					if(null == om){
//						log.info("ObjectMessage is null ,Forcecut receive next ...");
////						continue;
//						return;
//					}
//					
//					Serializable message = om.getObject();
//					if(message == null){
//						log.info("message is null ,Forcecut receive next ...");
////						continue;
//						return;
//					}

					if(message instanceof McutFcutInfo){
						log.info("received mc msg! ref="+message);
						McutFcutInfo info = (McutFcutInfo)message;
						McutFcutInfo replyMessage = null;
						try {
							if(DeficientStatus.DEFICIENT.getValue() == info.getStatus().getValue()){
								log.info("receive [NORMAL] to [DEFICIENT] msg ,cid:"+info.getCustomerId());
								deficientService.normal2Deficient(info.getCustomerId());
								replyMessage = buildReplyMessage(info);
								
							}else if(DeficientStatus.NORMAL.getValue() == info.getStatus().getValue()){
								log.info("receive [DEFICIENT] to [NORMAL] msg ,cid:"+info.getCustomerId());
								deficientService.deficient2Normal(info.getCustomerId());
								replyMessage = buildReplyMessage(info);
								
							}else if(DeficientStatus.FORCECUT.getValue() == info.getStatus().getValue()){
								log.info("receive [DEFICIENT] to [FORCECUT] msg ,cid:"+info.getCustomerId());
								deficientService.deficient2Forcecut(info, mcReceiver, message);
								replyMessage = buildReplyMessage(info);
								
							}else {
								log.error(" error type:"+info.getStatus().getValue());
							}

							mcReceiver.sendReplyMessage(om, replyMessage);
							
						} catch (Exception e) {
							log.error("======================  ForceCutService INIT ERROR  ! ",e);
						}

						
					}else{
						log.error("forcecut recieve A NOT valid msg! ref="+message);
					}
				
					
				}catch(Exception dx){
					dx.printStackTrace();
					log.error("CheckerWithJms err!" , dx);
				}
				
			}
		});
	}
	
	
	private  void initFormula() throws Exception {
    	//init formula
    	FormulaFactory.DEFAULT.initializeForForceCut();
	}
	

	
	private void initializeDB() throws Exception{
		
		try{
			DbSessionFactory.beginTransaction(DbSessionFactory.MAIN);
			DbSessionFactory.commitTransaction(DbSessionFactory.MAIN);
		}catch(Exception dx){
			DbSessionFactory.rollbackTransaction(DbSessionFactory.MAIN);
			dx.printStackTrace();
			log.error("init main db err!" , dx);
		}
		
		try{
			DbSessionFactory.beginTransaction(DbSessionFactory.INFO);
			DbSessionFactory.commitTransaction(DbSessionFactory.INFO);
		}catch(Exception dx){
			DbSessionFactory.rollbackTransaction(DbSessionFactory.INFO);
			dx.printStackTrace();
			log.error("init info db err!" , dx);
		}
		
		try{
			DbSessionFactory.beginTransaction(DbSessionFactory.UNIQUE);
			DbSessionFactory.commitTransaction(DbSessionFactory.UNIQUE);
		}catch(Exception dx){
			DbSessionFactory.rollbackTransaction(DbSessionFactory.UNIQUE);
			dx.printStackTrace();
			log.error("init unique db err!" , dx);
		}
	}

}
