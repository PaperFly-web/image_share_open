package com.paperfly.imageShare.common.utils;

import com.paperfly.imageShare.common.constant.CodeConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年10月27日 下午9:59:27
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public R() {
		put("code", CodeConstant.OK);
		put("msg","");
		put("data","{}");
	}
	
	public static R error() {
		return error(CodeConstant.INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static R error(String msg) {
		return error(CodeConstant.INTERNAL_SERVER_ERROR, msg);
	}
	
	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);

		return r;
	}

	/**
	 * 用户操作的错误
	 * @param msg 错误信息
	 * @return
	 */
	public static R userError(String msg){
		R r = new R();
		r.put("code", CodeConstant.USER_ERROR);
		r.put("msg", msg);

		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}
	
	public static R ok() {
		return new R();
	}

	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode(){
		return (int)this.get("code");
	}

	public Object getData(){
		return this.get("data");
	}

	public String getMsg(){
		return (String) this.get("msg");
	}
}
