package com.labgrok.noauthce;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NonceController {
	@RequestMapping(method=RequestMethod.POST)
	public String dumpPost(@RequestBody Object incomingJson) {
		return "OK";
	}

}
