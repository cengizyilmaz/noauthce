package com.labgrok.noauthce;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Cengiz YILMAZ It is needed to gather the nonce token from the post
 *         body before it is processing. This need to capture request object and
 *         wrap in to another request object Otherwise it is violate the request
 *         object Therefore the wrapping class for http servlet request needed
 *         to capture through Filter This class is aimed to wrap Http Servlet
 *         request and form into a json string from the HttpFilter
 */
public class NonceHttpReadRequest extends HttpServletRequestWrapper {

	private final String body;

	private String clientID;

	private String nonceToken;

	public NonceHttpReadRequest(HttpServletRequest request) throws IOException {
		super(request);
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = request.getInputStream();
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, decoder))) {
			int ch;
			while ((ch = bufferedReader.read()) > -1) {
				buffer.append((char) ch);
			}
		}

		catch (IOException ex) {
			throw ex;
		}

		body = buffer.toString();
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getNonceToken() {
		return nonceToken;
	}

	public void setNonceToken(String nonce) {
		this.nonceToken = nonce;
	}

	public String getBody() {
		return body;
	}

	public String toString() {

		return body;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				body.getBytes(StandardCharsets.UTF_8));
		ServletInputStream servletInputStream = new ServletInputStream() {
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setReadListener(ReadListener listener) {
				// TODO Auto-generated method stub

			}
		};
		return servletInputStream;
	}
}
