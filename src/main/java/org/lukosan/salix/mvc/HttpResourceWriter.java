package org.lukosan.salix.mvc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.lukosan.salix.ResourceWriter;

public class HttpResourceWriter implements ResourceWriter {

	private HttpServletResponse response;
	private boolean consumed = false;
	
	public HttpResourceWriter(HttpServletResponse response) {
		this.response = response;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		consume();
		return response.getOutputStream();
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		consume();
		return response.getWriter();
	}

	private synchronized void consume() throws IOException {
		if(this.consumed)
			throw new IOException("HttpServletResponse has already been consumed");
		this.consumed = true;
	}

}
