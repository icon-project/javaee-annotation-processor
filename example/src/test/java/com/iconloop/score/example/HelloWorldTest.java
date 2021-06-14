package com.iconloop.score.example;

import com.iconloop.score.client.DefaultScoreClient;
import com.iconloop.score.client.ScoreClient;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.crypto.KeystoreException;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldTest {
    static String DEPLOY_PARAM_NAME = "name";
    static Map<String, Object> DEPLOY_PARAMS = Map.of("_name", DEPLOY_PARAM_NAME);

    @ScoreClient
    static HelloWorld helloWorld;

    @ScoreClient
    static NameGetter nameGetter;

    //HelloWorldImpl will be generated in test scope
    @ScoreClient(suffix = "")
    static HelloWorldImpl helloWorldImpl;

    @BeforeAll
    static void beforeAll() {
        //deploy with System properties
        ////if System.getProperty("address") is null,
        ////try deploy "scoreFilePath" for content, "params." prefixed properties for parameters
        DefaultScoreClient client = DefaultScoreClient.of(System.getProperties());



        //create with DefaultScoreClient
        helloWorld = new HelloWorldScoreClient(client);

        //manually deploy
        nameGetter = new NameGetterScoreClient(
                DefaultScoreClient._deploy(
                        System.getProperty("url"),
                        System.getProperty("nid"),
                        System.getProperty("keyStorePath"),
                        System.getProperty("keyStorePass"),
                        System.getProperty("scoreFilePath"),
                        DEPLOY_PARAMS));

        //manually deploy with constructor parameters
        String url = System.getProperty("url");
        BigInteger nid = new BigInteger(System.getProperty("nid"), 16);
        KeyWallet wallet = null;
        try {
            wallet = KeyWallet.load(System.getProperty("keyStorePass"), new File(System.getProperty("keyStorePath")));
        } catch (IOException | KeystoreException e) {
            throw new RuntimeException(e);
        }
        String scoreFilePath = System.getProperty("scoreFilePath");
        helloWorldImpl = HelloWorldImpl._deploy(
                new IconService(new HttpProvider(url)),
                nid,
                wallet,
                scoreFilePath,
                DEPLOY_PARAM_NAME);
    }

    @Test
    void name() {
        assertEquals(System.getProperty("param._name"), helloWorld.name());
        assertEquals(DEPLOY_PARAMS.get("_name"), nameGetter.name());
        assertEquals(DEPLOY_PARAM_NAME, helloWorldImpl.name());

    }

}