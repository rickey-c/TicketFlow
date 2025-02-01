package com.damai.captcha.util;

import com.damai.captcha.model.vo.PointVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/30 21:45
 */
public class JsonUtil {
	private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	public static List<PointVO> parseArray(String text, Class<PointVO> clazz) {
		if (text == null) {
			return null;
		} else {
			String[] arr = text.replaceFirst("\\[","")
					.replaceFirst("\\]","").split("\\}");
			List<PointVO> ret = new ArrayList<>(arr.length);
			for (String s : arr) {
				ret.add(parseObject(s,PointVO.class));
			}
			return ret;
		}
	}

	public static PointVO parseObject(String text, Class<PointVO> clazz) {
		if(text == null) {
			return null;
		}
		try {
			PointVO ret =  clazz.getDeclaredConstructor().newInstance();
			return ret.parse(text);
		}catch (Exception ex){
			logger.error("json解析异常", ex);

		}
		return null;
	}

	public static String toJsonString(Object object) {
		if(object == null) {
			return "{}";
		}
		if(object instanceof PointVO){
			PointVO t = (PointVO)object;
			return t.toJsonString();
		}
		if(object instanceof List){
			List<PointVO> list = (List<PointVO>)object;
			StringBuilder buf = new StringBuilder("[");
			list.stream().forEach(t->{
				buf.append(t.toJsonString()).append(",");
			});
			return buf.deleteCharAt(buf.lastIndexOf(",")).append("]").toString();
		}
		if(object instanceof Map){
			return ((Map)object).entrySet().toString();
		}
		throw new UnsupportedOperationException("不支持的输入类型:"
				+object.getClass().getSimpleName());
	}
}
