package org.lukosan.salix.mvc;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lukosan.salix.MapUtils;
import org.lukosan.salix.ResourceWriter;
import org.lukosan.salix.SalixResource;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SalixController {

	@Autowired
	private SalixServiceProxy salixService;
	@Autowired
	private SalixHandlerMapping salixHandlerMaping;
	
	private Set<String> served = new HashSet<String>();
	
	@RequestMapping("/salix-url-handler")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Model model) throws SalixHttpException, IOException {
		
		SalixUrl url = salixService.url(request.getRequestURI(), request.getServerName());
		
		if(null == url)
			throw new SalixHttpException(HttpStatus.NOT_FOUND);
		
		if(url.getStatus() != HttpStatus.OK.value()) {
			throw new SalixHttpException(url.getStatus(), url.getView());
		}

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
	public void resource(@PathVariable String sourceId, WebRequest webRequest, HttpServletRequest request, HttpServletResponse response) throws SalixHttpException, IOException {
		
		if(served.contains(request.getServerName() + "_" + sourceId) && webRequest.checkNotModified(LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)*1000))
			return;
		
		served.add(request.getServerName() + "_" + sourceId);
		
		SalixResource resource = salixService.resource(sourceId, request.getServerName());
		
		if(null == resource)
			throw new SalixHttpException(HttpStatus.NOT_FOUND);
		
		if(StringUtils.hasText(resource.getContentType()))
			response.setContentType(resource.getContentType());
		
		response.setDateHeader("Expires", System.currentTimeMillis() + 300000L);
		response.setHeader("Cache-Control", "max-age=300");
		
		ResourceWriter writer = new HttpResourceWriter(response);
		resource.writeTo(writer);	
	}
	
	protected void setResponseProperties(HttpServletResponse response, Map<String, Object> map) {
		// TODO cache control etc. but do we really want that here, as it's more of a container/webserver thing?
	}

	@RequestMapping("/salix-url-handler-reload")
	public void reload() {
		salixHandlerMaping.reloadHandlers();
		served = new HashSet<String>();
	}
	
	@ExceptionHandler({ SalixHttpException.class })
	public void handledExceptions(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
		SalixHttpException she = (SalixHttpException) ex;
		if(she.getStatus() == HttpStatus.MOVED_PERMANENTLY || she.getStatus() == HttpStatus.FOUND) {
			response.setStatus(she.getStatus().value());
			response.sendRedirect(she.getView());
		}
		response.setStatus(she.getStatus().value());
	}

}