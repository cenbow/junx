/*
 ***************************************************************************************
 * 
 * @Title:  SimpleEventBus.java   
 * @Package io.github.junxworks.junx.event.impl   
 * @Description: (用一句话描述该文件做什么)   
 * @author: Michael
 * @date:   2018-7-11 15:47:42   
 * @version V1.0 
 * @Copyright: 2018 JunxWorks. All rights reserved. 
 * 
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package io.github.junxworks.junx.event.impl;

import io.github.junxworks.junx.event.EventContext;

/**
 * 单线程的EventBus，任务调度都是在当前执行线程中执行。
 * 具体说明，请参考{@link io.github.junxworks.junx.event.EventBus}
 * @author: Michael
 * @date:   2017-5-10 14:27:19
 * @since:  v1.0
 */
public class SimpleEventBus extends AbstractEventBus {

	@Override
	public void publish(EventContext event) throws Exception {
		this.dispatcher.dispatch(event);
	}

}
