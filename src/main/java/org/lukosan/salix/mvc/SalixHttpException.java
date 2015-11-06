package org.lukosan.salix.mvc;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

public class SalixHttpException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;
	
	public SalixHttpException(HttpStatus status) {
		this.status = status;
	}
	
	public SalixHttpException(int status) {
		this.status = Arrays.stream(HttpStatus.values()).filter(i -> i.value() == status).findFirst().get();
	}
	
	public HttpStatus getStatus() {
		return status;
	}

}
