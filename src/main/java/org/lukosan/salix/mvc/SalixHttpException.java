package org.lukosan.salix.mvc;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

public class SalixHttpException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;
	private String view;
	
	public SalixHttpException(HttpStatus status) {
		this.status = status;
	}
	
	public SalixHttpException(int status, String view) {
		this.status = Arrays.stream(HttpStatus.values()).filter(i -> i.value() == status).findFirst().get();
		this.view = view;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getView() {
		return view;
	}

}
