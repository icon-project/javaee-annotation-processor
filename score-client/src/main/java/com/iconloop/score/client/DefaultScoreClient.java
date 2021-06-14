package com.iconloop.score.client;

import foundation.icon.icx.*;
import foundation.icon.icx.crypto.KeystoreException;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcError;
import foundation.icon.icx.transport.jsonrpc.RpcItemCreator;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultScoreClient {
    public static final Address ZERO_ADDRESS = new Address("cx0000000000000000000000000000000000000000");
    public static final BigInteger DEFAULT_STEP_LIMIT = new BigInteger("9502f900",16);
    public static final long BLOCK_INTERVAL = 1000;
    public static final long DEFAULT_RESULT_RETRY_WAIT = 1000;
    public static final long DEFAULT_RESULT_TIMEOUT = 10000;

    protected final IconService iconService;
    protected final BigInteger nid;
    protected final Wallet wallet;
    protected final Address address;
    protected BigInteger stepLimit = DEFAULT_STEP_LIMIT;
    protected long resultTimeout = DEFAULT_RESULT_TIMEOUT;

    public DefaultScoreClient(String url, String nid, String keyStorePath, String keyStorePassword, String address) {
        this(iconService(url), nid(nid), wallet(keyStorePath, keyStorePassword), new Address(address));
    }

    public DefaultScoreClient(IconService iconService, BigInteger nid, Wallet wallet, Address address) {
        this.iconService = iconService;
        this.nid = nid;
        this.wallet = wallet;
        this.address = address;
    }

    public DefaultScoreClient(DefaultScoreClient client) {
        this.iconService = client._iconService();
        this.nid = client._nid();
        this.wallet = client._wallet();
        this.address = client._address();
        this.stepLimit = client._stepLimit();
        this.resultTimeout = client._resultTimeout();
    }

    public static DefaultScoreClient _deploy(String url, String nid, String keyStorePath, String keyStorePassword, String scoreFilePath, Map<String, Object> params) {
        return _deploy(iconService(url), nid(nid), wallet(keyStorePath, keyStorePassword), scoreFilePath, params);
    }

    public static DefaultScoreClient _deploy(IconService iconService, BigInteger nid, Wallet wallet, String scoreFilePath, Map<String, Object> params) {
        Address address = deploy(iconService, nid, wallet, DEFAULT_STEP_LIMIT, ZERO_ADDRESS, scoreFilePath, params, DEFAULT_RESULT_TIMEOUT);
        return new DefaultScoreClient(iconService, nid, wallet, address);
    }

    public void _update(String scoreFilePath, Map<String, Object> params) {
        deploy(iconService, nid, wallet, DEFAULT_STEP_LIMIT, address, scoreFilePath, params, DEFAULT_RESULT_TIMEOUT);
    }

    public static DefaultScoreClient of(Properties properties) {
        return of("", properties);
    }
    public static DefaultScoreClient of(String prefix, Properties properties) {
        String url = properties.getProperty(prefix+"url");
        String nid = properties.getProperty(prefix+"nid");
        String keyStorePath = properties.getProperty(prefix+"keyStorePath");
        String keyStorePass = properties.getProperty(prefix+"keyStorePass");
        String address = properties.getProperty(prefix+"address");
        if (address != null) {
            System.out.printf("url: %s, nid: %s, keyStorePath: %s, address: %s%n",
                    url, nid, keyStorePath, address);
            return new DefaultScoreClient(url, nid, keyStorePath, keyStorePass, address);
        } else {
            String scoreFilePath = System.getProperty(prefix+"scoreFilePath");
            String paramsKey = prefix+"params.";
            Map<String, Object> params = new HashMap<>();
            for(Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                String key = ((String)entry.getKey());
                if (key.startsWith(paramsKey)) {
                    params.put(key.substring(paramsKey.length()), entry.getValue());
                }
            }
            System.out.printf("url: %s, nid: %s, keyStorePath: %s, scoreFilePath: %s, params: %s%n",
                    url, nid, keyStorePath, scoreFilePath, params);
            return _deploy(url, nid, keyStorePath, keyStorePass, scoreFilePath, params);
        }
    }

    public IconService _iconService() {
        return iconService;
    }

    public BigInteger _nid() {
        return nid;
    }

    public Wallet _wallet() {
        return wallet;
    }

    public Address _address() {
        return address;
    }

    public BigInteger _stepLimit() {
        return stepLimit;
    }

    public void _stepLimit(BigInteger stepLimit) {
        this.stepLimit = stepLimit;
    }

    public long _resultTimeout() {
        return resultTimeout;
    }

    public void _resultTimeout(long resultTimeout) {
        this.resultTimeout = resultTimeout;
    }

    public <T> T _call(Class<T> responseType, String method, Map<String, Object> params) {
        return call(responseType, iconService, address, method, params);
    }

    public void _send(String method, Map<String, Object> params) {
        send(iconService, nid, wallet, stepLimit, address, null, method, params, resultTimeout);
    }

    public void _send(BigInteger valueForPayable, String method, Map<String, Object> params) {
        send(iconService, nid, wallet, stepLimit, address, valueForPayable, method, params, resultTimeout);
    }

    public BigInteger _balance() {
        return balance(iconService, address);
    }

    public static IconService iconService(String url) {
        return new IconService(new HttpProvider(url));
    }

    public static BigInteger nid(String nid) {
        if (nid.startsWith("0x")) {
            return new BigInteger(nid.substring(2), 16);
        } else {
            return new BigInteger(nid);
        }
    }

    public static Wallet wallet(String keyStorePath, String keyStorePassword) {
        try {
            return KeyWallet.load(keyStorePassword, new File(keyStorePath));
        } catch (IOException | KeystoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static RpcObject mapToRpcObject(Map<String, Object> params) {
        RpcObject.Builder builder = new RpcObject.Builder();
        if (params != null) {
            for(Map.Entry<String, Object> entry : params.entrySet()) {
                builder.put(entry.getKey(), RpcItemCreator.create(entry.getValue()));
            }
        }
        return builder.build();
    }

    public static <T> T call(
            Class<T> responseType, IconService iconService, Address address,
            String method, Map<String, Object> params) {
        Call<T> call = new Call.Builder()
                .to(address)
                .method(method)
                .params(mapToRpcObject(params))
                .buildWith(responseType);
        try {
            return iconService.call(call).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static TransactionBuilder.Builder transactionBuilder(BigInteger nid, Wallet wallet, BigInteger stepLimit, Address address) {
        return TransactionBuilder.newBuilder()
                .nid(nid)
                .from(wallet.getAddress())
                .stepLimit(stepLimit)
                .to(address);
    }

    static Bytes sendTransaction(IconService iconService, Transaction tx, Wallet wallet) {
        try {
            return iconService.sendTransaction(new SignedTransaction(tx, wallet)).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void waitBlockInterval() {
        System.out.printf("wait block interval %d msec%n", BLOCK_INTERVAL);
        try {
            Thread.sleep(BLOCK_INTERVAL);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static TransactionResult send(
            IconService iconService, BigInteger nid, Wallet wallet, BigInteger stepLimit, Address address,
            BigInteger valueForPayable, String method, Map<String, Object> params,
            long timeout) {
        Transaction tx = transactionBuilder(nid, wallet, stepLimit, address)
                .value(valueForPayable)
                .call(method)
                .params(mapToRpcObject(params)).build();
        Bytes txh = sendTransaction(iconService, tx, wallet);
        waitBlockInterval();
        return result(iconService, txh, timeout);
    }

    public static Address deploy(
            IconService iconService, BigInteger nid, Wallet wallet, BigInteger stepLimit, Address address,
            String scoreFilePath, Map<String, Object> params,
            long timeout) {

        byte[] content;
        try {
            content = Files.readAllBytes(Path.of(scoreFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String contentType;
        if (scoreFilePath.endsWith(".jar")) {
            contentType = "application/java";
        } else if (scoreFilePath.endsWith(".zip")) {
            contentType = "application/zip";
        } else {
            throw new RuntimeException("not supported score file");
        }
        Transaction tx = transactionBuilder(nid, wallet, stepLimit, address)
                .deploy(contentType, content)
                .params(mapToRpcObject(params)).build();
        Bytes txh = sendTransaction(iconService, tx, wallet);
        waitBlockInterval();
        TransactionResult txr = result(iconService, txh, timeout);
        System.out.println("SCORE address: "+txr.getScoreAddress());
        return new Address(txr.getScoreAddress());
    }

    public static TransactionResult result(IconService iconService, Bytes txh, long timeout) {
        long etime = System.currentTimeMillis() + timeout;
        TransactionResult txr = null;
        while(txr == null) {
            try {
                txr = iconService.getTransactionResult(txh).execute();
            } catch (RpcError e) {
                if (e.getCode() == -31002 /* pending */
                        || e.getCode() == -31003 /* executing */
                        || e.getCode() == -31004 /* not found */) {
                    if (timeout > 0 && System.currentTimeMillis() >= etime) {
                        throw new RuntimeException("timeout");
                    }
                    try {
                        Thread.sleep(DEFAULT_RESULT_RETRY_WAIT);
                        System.out.println("wait for "+txh);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!BigInteger.ONE.equals(txr.getStatus())) {
            throw new RuntimeException(txr.getFailure().toString());
        }
        return txr;
    }

    public static BigInteger balance(IconService iconService, Address address) {
        try {
            return iconService.getBalance(address).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
