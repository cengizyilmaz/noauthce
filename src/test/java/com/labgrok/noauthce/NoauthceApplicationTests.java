package com.labgrok.noauthce;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NoauthceApplicationTests {

	@Autowired
	protected MockMvc mvc;
	
	@Autowired
	private TokenService service;
	private String startedNonce = null;
	private Cookie startedNof = null;
	private String clientID = "1234567";

	@Before
	public void setUp() {
		Map<String, String> claimList = new HashMap<>();
		claimList.put(NonceUtil.USERNAME.getValue(), "test");
		ZoneOffset offset = OffsetDateTime.now().getOffset();
		Instant currentInstant = LocalDateTime.now().toInstant(offset);
		Date nof = Date.from(currentInstant);
		String nonce = service.generateToken(claimList, nof, clientID);
		Map<String, String> contentMap = new HashMap<>();
		contentMap.put(NonceUtil.NONCETOKEN.getValue(), nonce);
		contentMap.put(NonceUtil.CLIENTID.getValue(),clientID);
		startedNonce = mapToJson(contentMap);
		startedNof = new Cookie(NonceUtil.NONCETIMESTAMP.getValue(), String.valueOf(currentInstant.getEpochSecond()));
	}

	protected String mapToJson(Object obj) {
		return new Gson().toJson(obj);
	}

	protected <T> T mapFromJson(String json, Class<T> clazz) {

		return new Gson().fromJson(json, clazz);
	}

	@Test
	public void testCorrectNonceAndTimestamp() throws Exception {
		String uri = "/";
		String content = startedNonce;
		System.out.println("startedNof:" + startedNof);
		startedNof.setHttpOnly(true);
		startedNof.setDomain("/");

		MvcResult result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON)
				.content(content).cookie(startedNof)).andReturn();
		String nonce = result.getResponse().getHeader(NonceUtil.NONCETOKEN.getValue());
		Map<String, String> contentMap = new HashMap<>();
		contentMap.put(NonceUtil.NONCETOKEN.getValue(), nonce);
		content = mapToJson(contentMap);
		result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(content)
				.cookie(result.getResponse().getCookies())).andReturn();
		int status = result.getResponse().getStatus();
		assertEquals(200, status);
	}
}
