package jp.emcom.adv.fx.completor.biz.executor;

import java.util.List;

import cn.bestwiz.jhf.core.application.Lifecycle;
import cn.bestwiz.jhf.core.dao.bean.main.JhfWlOrder;

/**
 * 
 * @author Jingqi Xu
 */
public interface WLExecutor extends Lifecycle {
	
	void execute(List<JhfWlOrder> candidates) throws Exception;
}
