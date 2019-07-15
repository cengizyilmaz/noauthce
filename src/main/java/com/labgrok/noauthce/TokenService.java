package com.labgrok.noauthce;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class TokenService {

	public String generateToken(Map<String, String> claimList, Date nof, String hash) {
		Algorithm algorithmHS = null;
		String token = null;
		try {
			algorithmHS = Algorithm.HMAC256(hash);
			Builder jwtBuilder = JWT.create();
			for (Entry<String, String> item : claimList.entrySet()) {
				jwtBuilder.withClaim(item.getKey(), item.getValue());
			}

			if (nof != null) {
				jwtBuilder.withNotBefore(nof);
			}

			token = jwtBuilder.sign(algorithmHS);

		} catch (IllegalArgumentException | UnsupportedEncodingException e) {

		}
		return token;

	}
}
