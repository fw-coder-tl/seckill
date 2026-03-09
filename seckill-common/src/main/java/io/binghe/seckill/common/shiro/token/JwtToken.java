package io.binghe.seckill.common.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;
public class JwtToken implements AuthenticationToken {

	private static final long serialVersionUID = 4758816034257581191L;
	private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
