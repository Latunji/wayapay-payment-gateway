package com.wayapaychat.paymentgateway.pojo.wema;

import java.math.BigDecimal;

public class WemaTxnQueryRequest {
	
    private String craccount;
    
    private String sessionid;
    
    private String txndate;
    
    private BigDecimal amount;

    public String getCraccount() {
        return craccount;
    }

    public void setCraccount(String craccount) {
        this.craccount = craccount;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getTxndate() {
        return txndate;
    }

    public void setTxndate(String txndate) {
        this.txndate = txndate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
