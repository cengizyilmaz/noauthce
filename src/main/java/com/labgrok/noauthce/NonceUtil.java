package com.labgrok.noauthce;

public enum NonceUtil {
	NONCETOKEN("nonce"),
	NONCETIMESTAMP("NTS"),
	PATH("/"),
	CLIENTID("clientID"),
	USERNAME("username");
	
	 private String value;
    
    private NonceUtil(final String value)
    {
        this.value = value;
    }
    
    public String getValue()
    {
        return value;
    }
	
}
