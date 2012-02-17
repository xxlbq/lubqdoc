package jp.emcom.adv.fx.completor.biz.executor;

import java.util.List;

import jp.emcom.adv.fx.completor.biz.AutoBuildInfo;

import cn.bestwiz.jhf.core.application.Lifecycle;

public interface ParExecutor extends Lifecycle{
	
	void execute(List<AutoBuildInfo> autoBuildInfo) throws Exception;
}
