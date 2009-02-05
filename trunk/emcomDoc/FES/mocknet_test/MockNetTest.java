//package net;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import cn.bestwiz.jhf.formula.bean.ContractNetBindInfo;
//import cn.bestwiz.jhf.formula.bean.RateInfo;
//import cn.bestwiz.jhf.formula.bussiness.ParseNetContract;
//import cn.bestwiz.jhf.formula.data.CacheAssignNetBean;
//import cn.bestwiz.jhf.formula.data.CacheContractBean;
//
//
//
//public class MockNetTest  {
//
//	public static void main(String[] args) {
//		
//		
//		List<CacheContractBean> lstContract = createCacheContractBeanList();
//		List<CacheAssignNetBean> lstAssignNet = createCacheAssignNetBeanList();
//		Map<String,RateInfo> mapRate = createRateInfoMap();
//		
//		ParseNetContract pnc = new ParseNetContract(lstContract,lstAssignNet,mapRate);
//		
//		printContractNetBindInfoList(pnc.getContractNetBindInfo());
//		printFreeContractInfoList(pnc.getFreeContractInfo());
//		printNetPlValue(pnc.getNetPl());
//	}
//
//
//
//
//	private static void printNetPlValue(BigDecimal netPl) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//
//
//	private static void printFreeContractInfoList(
//			List<CacheContractBean> freeContractInfo) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//
//
//	private static void printContractNetBindInfoList(
//			List<ContractNetBindInfo> contractNetBindInfo) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//
//
//	private static Map<String, RateInfo> createRateInfoMap() {
//		
//		Map<String, RateInfo> rateMap = new HashMap<String, RateInfo>();
//		String usd_jpy = "USD/JPY";
//		RateInfo ri = new RateInfo(usd_jpy,"125.00","120.00");
//		
//		rateMap.put(usd_jpy, ri);
//		
//		return rateMap;
//	}
//
//
//
//
//	private static List<CacheAssignNetBean> createCacheAssignNetBeanList() {
//		
//		CacheAssignNetBean netBean1 = new CacheAssignNetBean(); 
//		netBean1.setAssignId(	"20071218ASNT00005906");
//		netBean1.setContractId(	"20071218CONT00044947");
//		netBean1.setCurrencyPair("USD/JPY");
//		netBean1.setNetAmount(new BigDecimal("100000"));
//		netBean1.setSide(new BigDecimal("-1"));
//		
//		
//		CacheAssignNetBean netBean2 = new CacheAssignNetBean(); 
//		netBean2.setAssignId(	"20071218ASNT00005906");
//		netBean2.setContractId(	"20071218CONT00044948");
//		netBean2.setCurrencyPair("USD/JPY");
//		netBean2.setNetAmount(new BigDecimal("170000"));
//		netBean2.setSide(new BigDecimal("1"));
//		
//		CacheAssignNetBean netBean3 = new CacheAssignNetBean(); 
//		netBean3.setAssignId(	"20071218ASNT00005906");
//		netBean3.setContractId(	"20071218CONT00044949");
//		netBean3.setCurrencyPair("USD/JPY");
//		netBean3.setNetAmount(new BigDecimal("50000"));
//		netBean3.setSide(new BigDecimal("-1"));
//		
//		CacheAssignNetBean netBean4 = new CacheAssignNetBean(); 
//		netBean4.setAssignId(	"20071218ASNT00005906");
//		netBean4.setContractId(	"20071218CONT00044950");
//		netBean4.setCurrencyPair("USD/JPY");
//		netBean4.setNetAmount(new BigDecimal("50000"));
//		netBean4.setSide(new BigDecimal("1"));
//		
//		CacheAssignNetBean netBean5 = new CacheAssignNetBean(); 
//		netBean5.setAssignId(	"20071218ASNT00005906");
//		netBean5.setContractId(	"20071218CONT00044951");
//		netBean5.setCurrencyPair("USD/JPY");
//		netBean5.setNetAmount(new BigDecimal("70000"));
//		netBean5.setSide(new BigDecimal("-1"));
//		
//		List<CacheAssignNetBean> list = new ArrayList<CacheAssignNetBean>();
//		list.add(netBean1);list.add(netBean2);list.add(netBean3);list.add(netBean4);list.add(netBean5);
//		
//		return list;
//	}
//
//
//
//
//	private static List<CacheContractBean> createCacheContractBeanList() {
//
////		CacheContractBean contractBean1 = new CacheContractBean();
////		contractBean1.setContractId("20071218CONT00044947");
////		contractBean1.setAmountNoSettled(amountNoSettled);
////		contractBean1.setAmountSettled();
////		contractBean1.setAmountSettling();
////		contractBean1.setAssignNetAmount();
////		contractBean1.setContractCode();
////		contractBean1.setContractDate("20071218");
////		contractBean1.setCounterCode();
////		contractBean1.setCurrencyPair();
////		contractBean1.setCustomerOrderNo("T000000010");
////		contractBean1.setOpenCommission();
////		contractBean1.setPrice();
////		contractBean1.setProductId();
////		contractBean1.setSettledDate();
////		contractBean1.setSide();
////		contractBean1.setSwap();
////		
////		
//		
//		
//		return null;
//	}
//}
