package jp.emcom.adv.fx.completor.biz.info;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;

public class CompletorOrderInfo {
	
	private JhfWlOrder jhfWlOrder ;
	private CountDownLatch latch ;
	
	public CompletorOrderInfo(JhfWlOrder jhfWlOrder, CountDownLatch latch) {
		super();
		this.jhfWlOrder = jhfWlOrder;
		this.latch = latch;
	}
	public CountDownLatch getLatch() {
		return latch;
	}
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	public JhfWlOrder getJhfWlOrder() {
		return jhfWlOrder;
	}
	public void setJhfWlOrder(JhfWlOrder jhfWlOrder) {
		this.jhfWlOrder = jhfWlOrder;
	}
	
	public String getKey(){
		if(jhfWlOrder == null){
			return null;
		}
		return jhfWlOrder.getClientId().concat(jhfWlOrder.getCurrencyPair());
	}
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
	
	
	
}
