package org.lukosan.salix.mvc;

import org.lukosan.salix.SalixPublisher;
import org.springframework.beans.factory.annotation.Autowired;

public class MvcPublisher implements SalixPublisher {

	@Autowired
	private SalixHandlerMapping salixHandlerMaping;
	
	@Override
	public void publish() {
		salixHandlerMaping.reloadHandlers();
	}

}