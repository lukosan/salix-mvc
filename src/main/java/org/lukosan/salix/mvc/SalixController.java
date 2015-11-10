package org.lukosan.salix.mvc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lukosan.salix.MapUtils;
import org.lukosan.salix.SalixUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SalixController {

	@Autowired
	private SalixServiceProxy salixService;
	@Autowired
	private SalixHandlerMapping salixHandlerMaping;
	
	@RequestMapping("/salix-url-handler")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Model model) throws SalixHttpException {
		
		SalixUrl url = salixService.url(request.getRequestURI(), request.getServerName());
		
		if(null == url)
			throw new SalixHttpException(HttpStatus.NOT_FOUND);
		
		if(url.getStatus() != HttpStatus.OK.value())
			throw new SalixHttpException(url.getStatus());

		Map<String, Object> map = url.getMap();

		if(map.containsKey("response")) {
			setResponseProperties(response, MapUtils.getMap(url.getMap(), "response"));
		}
		
		if(map.containsKey("resources")) {
			Map<String, Object> resources = MapUtils.getMap(map, "resources");
			for(String resourceName : resources.keySet())
				resources.put(resourceName, salixService.resource(resources.get(resourceName).toString(), 
						request.getServerName()).getMap());
		}
		
		return new ModelAndView(url.getView(), url.getMap());
	}
	
	protected void setResponseProperties(HttpServletResponse response, Map<String, Object> map) {
		// TODO cache control etc.
	}

	@RequestMapping("/salix-url-handler-reload")
	public void reload() {
		salixHandlerMaping.reloadHandlers();
	}
	
	@ExceptionHandler({ SalixHttpException.class })
	public void handledExceptions(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		SalixHttpException she = (SalixHttpException) ex;
		response.setStatus(she.getStatus().value());
	}

}