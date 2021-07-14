package foundation.icon.score.client;

import foundation.icon.icx.data.TransactionResult;

public class RevertedException extends score.RevertedException {
    private int code;
    public RevertedException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
