package com.iconloop.jsonrpc.model;

import com.iconloop.jsonrpc.Address;

import java.math.BigInteger;

public class SendTransactionParam extends TransactionParam {
    private BigInteger stepLimit;

    public SendTransactionParam(BigInteger nid, Address to, BigInteger value, String dataType, Object data) {
        super(nid, to, value, dataType, data);
    }

    public BigInteger getStepLimit() {
        return stepLimit;
    }

    public void setStepLimit(BigInteger stepLimit) {
        this.stepLimit = stepLimit;
    }

    public void setFrom(Address from) {
        this.from = from;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SendTransactionParam{");
        sb.append("stepLimit=").append(stepLimit);
        sb.append(", version=").append(version);
        sb.append(", from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", value=").append(value);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", nid=").append(nid);
        sb.append(", nonce=").append(nonce);
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
