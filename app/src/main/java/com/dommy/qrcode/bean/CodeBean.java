package com.dommy.qrcode.bean;

/**
 * Created by yhl
 * on 2020/8/4
 */
public class CodeBean {
    private String user;
    private String secret;
    private String issuer;

    public CodeBean(String user, String secret, String issuer) {
        this.user = user;
        this.secret = secret;
        this.issuer = issuer;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
