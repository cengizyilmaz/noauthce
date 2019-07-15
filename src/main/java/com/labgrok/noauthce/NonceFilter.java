package com.labgrok.noauthce;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;

/**
 * @author Cengiz YILMAZ Request Filter is handled in first and handle whole
 *         kind of the request types. During filtering phase, Post request from
 *         the API is handled from the filter and convert to the customized Http
 *         Servlet request which parsed to get the nonce token.
 */
@Component
public class NonceFilter extends OncePerRequestFilter {

	@SuppressWarnings("unchecked")
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			FilterChain filterChain) throws ServletException, IOException {

		if (httpRequest != null && httpRequest.getMethod().equals("POST") && httpRequest.getContentType() != null
				&& httpRequest.getContentType().contains("application/json")) {

			NonceHttpReadRequest wrappedRequest = new NonceHttpReadRequest(httpRequest);

			if (wrappedRequest != null) {
				String payload = wrappedRequest.getBody();

				HashMap<String, Object> json = new Gson().fromJson(payload, HashMap.class);

				if (json != null && json.containsKey(NonceUtil.CLIENTID.getValue())) {
					wrappedRequest.setClientID(json.get(NonceUtil.CLIENTID.getValue()).toString());

				}
				if (json != null && json.containsKey(NonceUtil.NONCETOKEN.getValue())) {
					wrappedRequest.setNonceToken(json.get(NonceUtil.NONCETOKEN.getValue()).toString());
				}

			}
			filterChain.doFilter(wrappedRequest, httpResponse);
		} else {
			filterChain.doFilter(httpRequest, httpResponse);
		}

	}

}
