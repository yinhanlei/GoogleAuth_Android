package com.dommy.qrcode.bean;

import android.os.CountDownTimer;

/**
 * Created by yhl
 * on 2020/8/4
 */
public class CodeBean {
    private String secret;
    private String issuer;

    public CodeBean(String secret, String issuer) {
        this.secret = secret;
        this.issuer = issuer;
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
