/*
 ***************************************************************************************
 * 
 * @Title:  NullParameterException.java   
 * @Package io.github.junxworks.junx.core.exception   
 * @Description: (用一句话描述该文件做什么)   
 * @author: Michael
 * @date:   2018-7-11 15:34:36   
 * @version V1.0 
 * @Copyright: 2018 JunxWorks. All rights reserved. 
 * 
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package io.github.junxworks.junx.core.exception;

/**
 * 不支持的参数.
 *
 * @author: Michael
 * @date:   2017-5-7 17:32:52
 * @since:  v1.0
 */
public class NullParameterException extends BaseRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2143051854631661844L;

	public NullParameterException() {
		super("");
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String)
	 */
	public NullParameterException(String msg) {
		super(msg);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(Throwable)
	 */
	public NullParameterException(Throwable ex) {
		super(ex);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String,Throwable)
	 */
	public NullParameterException(String msg, Throwable ex) {
		super(msg, ex);
	}

	/**
	 * @see BaseRuntimeException#BaseRuntimeException(String,Object...)
	 */
	public NullParameterException(String msg, Object... args) {
		super(msg, args);
	}
}
