package com.labgrok.noauthce;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Verification;

/**
 * @author Cengiz YILMAZ
 * 	The interceptor is special filter and these are
 *  lightweight for the spring services The interceptors executed before
 *  the service handle the request but after filter processed. The nonce
 *  token in the request body which is gathered from customized http
 *  servlet request is validated and check with the nonce time stamp from
 *  the cookie. If it is validated, the new nonce token is generated and
 *  send through http header and new nonce timestamp is also generated
 *  but sending through server side cookie with httpOnly flag
 */
@Component
public class NonceInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private TokenService service;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
	//	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	//	String username = auth.getName();
	//	boolean isAuthenticated = auth.isAuthenticated();
		// In order to test without auhtentication it is set to default value for
		// username and isAuthenticated flag
		String username = "test";
		boolean isAuthenticated = true;
		return isAuthenticated && checkToken(request, response, username);
	}

	

	private boolean isValidToken(Map<String, String> claimList, Long nofLeeway,
		 String hash, String token) {
		Algorithm algorithmHS = null;

		try {
			algorithmHS = Algorithm.HMAC256(hash);
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			return false;
		}
		try {
			Verification verifier = JWT.require(algorithmHS);
			for (Entry<String, String> item : claimList.entrySet()) {
				verifier.withClaim(item.getKey(), item.getValue());
			}
			
			if (nofLeeway != null) {
				verifier.acceptNotBefore(nofLeeway);
			}
		
			verifier.build().verify(token);
		} catch (JWTVerificationException exception) {
			return false;
		}

		return true;

	}

	private boolean checkToken(HttpServletRequest request, HttpServletResponse response, String username) {

		if (request.getMethod().equals("POST") && null != request.getContentType()
				&& request.getContentType().contains("application/json")) {
			NonceHttpReadRequest nonceRequest = WebUtils.getNativeRequest(request, NonceHttpReadRequest.class);
			if (nonceRequest != null) {
				// Validate Nonce token first from the request which is injected inside the Post
				// request
				Map<String, String> claimList = new HashMap<>();
				claimList.put(NonceUtil.USERNAME.getValue(), username);
				String clientID = nonceRequest.getClientID();
				if (isValidToken(claimList, 1L, clientID, nonceRequest.getNonceToken())) {
					
					// If the Nonce Token is valid, check the timestamp in the session cookie which
					// was set as httpOnly.
					List<Cookie> listCookies = Arrays.asList(request.getCookies());
					Cookie clientTimeStampCookie = listCookies.stream()
							.filter(item -> item.getName().equals(NonceUtil.NONCETIMESTAMP.getValue())).findFirst()
							.orElseGet(null);
					if (clientTimeStampCookie != null) {
						
						String lastChangeTokenInstant = clientTimeStampCookie.getValue();
						Date nof = JWT.decode(nonceRequest.getNonceToken()).getNotBefore();
						Date cookieDate = Date.from(Instant.ofEpochMilli(Long.parseLong(lastChangeTokenInstant)));
						// Check whether date in cookie which was set from previous request and not
						// before claim in the nonce token
						// It is not wanted to send a previously generated token and wanted one time
						// token.
						if (cookieDate.getTime() > nof.getTime()) {
							return false;
						}
						// If the date is validated, generate new nonce to the http header and new
						// timestamp for cookie
						ZoneOffset offset = OffsetDateTime.now().getOffset();
						Instant currentInstant = LocalDateTime.now().toInstant(offset);
						clientTimeStampCookie.setValue(String.valueOf(currentInstant.getEpochSecond()));
						nof = Date.from(currentInstant);
						String nonceToken = service.generateToken(claimList, nof, clientID);
						response.setHeader(NonceUtil.NONCETOKEN.getValue(), nonceToken);
						response.addCookie(clientTimeStampCookie);
						return true;
					} else {
						return false;
					}

				} else {
					return false;
				}

			} else {
				return false;
			}
		}
		return false;

	}
}
