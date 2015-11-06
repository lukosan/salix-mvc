package org.lukosan.salix.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lukosan.salix.SalixService;
import org.lukosan.salix.SalixUrl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

public class SalixHandlerMapping extends SimpleUrlHandlerMapping {

	private static final Log logger = LogFactory.getLog(SalixHandlerMapping.class);
	
	@Autowired
	private SalixService salixService;
	@Autowired
	private SalixController salixController;
	
	@Override
	public void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		reloadHandlers();
	}
	
	public void reloadHandlers() {
		Method method = ReflectionUtils.findMethod(SalixController.class, "handle", (Class<?>[]) null);
		setOrder(Integer.MIN_VALUE);
		Map<String, Object> map = new HashMap<String, Object>();
		salixService.activeUrls().forEach(w -> map.put(w.getUrl(), new HandlerMethod(salixController, method)));
		unbrittleRegisterHandlers(map);
	}
	
	public void add(SalixUrl url) {
		Method method = ReflectionUtils.findMethod(SalixController.class, "handle", (Class<?>[]) null);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(url.getUrl(), new HandlerMethod(salixController, method));
		unbrittleRegisterHandlers(map);
	}
	
	private void unbrittleRegisterHandlers(Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String url = entry.getKey();
			Object handler = entry.getValue();
			// Prepend with slash if not already present.
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			// Remove whitespace from handler bean name.
			if (handler instanceof String) {
				handler = ((String) handler).trim();
			}
			try {
				registerHandler(url, handler);
			} catch(IllegalStateException ise) {
				logger.info(String.format("Couldn't register handler for [%s] as it already exists.", url));
			} 
		}
	}
	
}
