package org.lukosan.salix.mvc;

import org.lukosan.salix.SalixResource;
import org.lukosan.salix.SalixScope;
import org.lukosan.salix.SalixService;
import org.lukosan.salix.SalixTemplate;
import org.lukosan.salix.SalixUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

public class SalixServiceProxy {

	@Autowired
	private SalixService salixService;
	
	@Value("${salix.mvc.ghost:}")
	private String localhostGhosting;

	public SalixUrl url(String requestURI, String serverName) {
		SalixUrl url = salixService.url(requestURI, ghost(serverName));
		if(null == url)
			url = salixService.url(requestURI, SalixScope.SHARED);
		return url;
	}

	public SalixResource resource(String sourceId, String serverName) {
		SalixResource resource = salixService.resource(sourceId, ghost(serverName));
		if(null == resource)
			resource = salixService.resource(sourceId, SalixScope.SHARED);
		return resource;
	}
	
	private String ghost(String serverName) {
		if(StringUtils.hasText(localhostGhosting) && serverName.equalsIgnoreCase("localhost"))
			return localhostGhosting;
		return serverName;
	}

	public SalixTemplate template(String templateName, String serverName) {
		SalixTemplate template = salixService.template(templateName, ghost(serverName));
		if(null == template)
			template = salixService.template(templateName, SalixScope.SHARED);
		return template;
	}

	public SalixTemplate template(String template) {
		return salixService.template(template);
	}

}
