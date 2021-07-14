package foundation.icon.score.example;

import foundation.icon.score.client.ScoreInterface;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;

@ScoreInterface
public interface IcxTransfer {
    @Payable
    @External
    void transfer(Address _address);

    @External(readonly = true)
    BigInteger getTransferred(Address _address);

    @EventLog(indexed = 1)
    void Transferred(Address _from, BigInteger icx);
}
