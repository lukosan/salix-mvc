package org.lukosan.salix.autoconfigure;

import javax.annotation.PostConstruct;
import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lukosan.salix.SalixProperties;
import org.lukosan.salix.SalixPublisher;
import org.lukosan.salix.SalixScopeRegistry;
import org.lukosan.salix.mvc.DisableUrlSessionFilter;
import org.lukosan.salix.mvc.MvcPublisher;
import org.lukosan.salix.mvc.SalixController;
import org.lukosan.salix.mvc.SalixHandlerMapping;
import org.lukosan.salix.mvc.SalixServiceProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SalixProperties.class)
public class MvcAutoConfiguration {

	@Configuration
	@ConditionalOnClass({ Servlet.class })
	@ConditionalOnWebApplication
	@ConditionalOnProperty(prefix = "salix.mvc.frontend", name = "enabled", matchIfMissing = true)
	public static class SalixHandlerMappingConfiguration {
		
		private static final Log logger = LogFactory.getLog(SalixHandlerMappingConfiguration.class);
		
		@PostConstruct
		public void postConstruct() {
			if(logger.isInfoEnabled())
				logger.info("PostConstruct " + getClass().getSimpleName());
		}
		
		@Bean
		public SalixServiceProxy salixServiceProxy() {
			return new SalixServiceProxy();
		}
		@Bean
		public SalixController salixController() {
			return new SalixController();
		}
		
		@Bean
		public SalixHandlerMapping salixHandlerMapping() {
			return new SalixHandlerMapping();
		}
		
		@Bean
		public SalixPublisher mvcPublisher() {
			return new MvcPublisher();
		}
		
		@Bean
		@ConditionalOnProperty(prefix = "salix.mvc.disablejsession", name = "enabled", matchIfMissing = true)
		public DisableUrlSessionFilter disableUrlSessionFilter() {
			return new DisableUrlSessionFilter();
		}
		
		@Bean
		@ConditionalOnMissingBean
		public SalixScopeRegistry salixScopeRegistry() {
			return new SalixScopeRegistry();
		}
		
	}
	
}