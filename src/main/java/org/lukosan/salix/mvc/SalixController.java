package org.lukosan.salix.mvc;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lukosan.salix.MapUtils;
import org.lukosan.salix.SalixResource;
import org.lukosan.salix.SalixResourceBinary;
import org.lukosan.salix.SalixResourceJson;
import org.lukosan.salix.SalixResourceText;
import org.lukosan.salix.SalixUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
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
			for(String resourceName : resources.keySet()) {
				SalixResource resource = salixService.resource(resources.get(resourceName).toString(), request.getServerName());
				switch(resource.getResourceType()) {
					case JSON : resources.put(resourceName, ((SalixResourceJson)resource).getMap());
					case TEXT : resources.put(resourceName, ((SalixResourceText)resource).getText());
					default : resources.put(resourceName, resource.getResourceUri());
				}
			}
		}
		
		return new ModelAndView(url.getView(), url.getMap());
	}
	
	@RequestMapping("/salix/resource/{sourceId:.+}")
	public void resource(@PathVariable String sourceId, HttpServletRequest request, HttpServletResponse response) throws SalixHttpException, IOException {
		
		SalixResource resource = salixService.resource(sourceId, request.getServerName());
		
		if(null == resource)
			throw new SalixHttpException(HttpStatus.NOT_FOUND);
		
		if(StringUtils.hasText(resource.getContentType()))
			response.setContentType(resource.getContentType());
		
		response.setDateHeader("Expires", System.currentTimeMillis() + 300000L);
		response.setHeader("Cache-Control", "max-age=300");
		
		switch(resource.getResourceType()) {
			case TEXT : response.getWriter().write(((SalixResourceText)resource).getText()); break;
			case JSON : response.getWriter().write(MapUtils.asString(((SalixResourceJson)resource).getMap())); break;
			case BINARY : response.getOutputStream().write(((SalixResourceBinary)resource).getBytes());
		}
	}
	
	protected void setResponseProperties(HttpServletResponse response, Map<String, Object> map) {
		// TODO cache control etc. but do we really want that here, as it's more of a container/webserver thing?
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