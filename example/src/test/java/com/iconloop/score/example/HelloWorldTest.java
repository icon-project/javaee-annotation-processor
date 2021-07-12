package com.iconloop.score.example;

import com.github.javafaker.Faker;
import com.iconloop.jsonrpc.Address;
import com.iconloop.jsonrpc.IconJsonModule;
import com.iconloop.jsonrpc.model.TransactionResult;
import com.iconloop.score.client.DefaultScoreClient;
import com.iconloop.score.client.ScoreClient;
import com.iconloop.score.example.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class HelloWorldTest {

    static DefaultScoreClient client = DefaultScoreClient.of(System.getProperties());

    @ScoreClient
    static NameGetter nameGetter = new NameGetterScoreClient(client);
    @ScoreClient
    static HelloWorld helloWorld = new HelloWorldScoreClient(client);
    @ScoreClient
    static IcxTransfer icxTransferReceiver = new IcxTransferScoreClient(client);

    static IcxTransfer icxTransferSender =
            new IcxTransferScoreClient(DefaultScoreClient.of("icxTransferSender.", System.getProperties()));
    static Faker faker = new Faker();


    @Test
    void name() {
        assertEquals(System.getProperty("params._name"), nameGetter.name());
    }

    @Test
    void greeting() {
        String greeting = HelloWorldImpl.DEFAULT_GREETING+" "+faker.name().name();
        helloWorld.setGreeting(greeting);
        assertEquals(HelloWorldImpl.greeting(helloWorld.name(), greeting),
                helloWorld.greeting());
    }

    @Test
    void dbAcceptable() {
        DBAcceptable dbAcceptable = new DBAcceptable();
        dbAcceptable.setStringNotNull("stringNotNull");
        String json = DBAcceptableJson.toJson(dbAcceptable).toString();
        helloWorld.setDBAcceptable(json);

        AssertEquals.assertEqualsDBAcceptable(dbAcceptable, DBAcceptableJson.parse(helloWorld.getDBAcceptable()));
    }

    @Test
    void dbAcceptableArray() {
        DBAcceptableArray dbAcceptableArray = new DBAcceptableArray();
        dbAcceptableArray.setStringNotNull(new String[]{"stringNotNull"});
        String json = DBAcceptableArrayJson.toJson(dbAcceptableArray).toString();
        helloWorld.setDBAcceptableArray(json);

        AssertEquals.assertEqualsDBAcceptableArray(dbAcceptableArray, DBAcceptableArrayJson.parse(helloWorld.getDBAcceptableArray()));
    }

    @Test
    void backwardCompatible() {
        ParameterAcceptable parameterAcceptable = new ParameterAcceptable();
        parameterAcceptable.setBooleanWrap(parameterAcceptable.isBooleanVal());
        parameterAcceptable.setByteWrap(parameterAcceptable.getByteVal());
        parameterAcceptable.setCharWrap(parameterAcceptable.getCharVal());
        parameterAcceptable.setShortWrap(parameterAcceptable.getShortVal());
        parameterAcceptable.setIntegerWrap(parameterAcceptable.getIntVal());
        parameterAcceptable.setLongWrap(parameterAcceptable.getLongVal());
        parameterAcceptable.setStringVal("");
        parameterAcceptable.setBigIntegerVal(BigInteger.ZERO);
        parameterAcceptable.setAddressVal(DefaultScoreClient.ZERO_ADDRESS);
        Struct struct = new Struct();
        struct.setValue("");
        parameterAcceptable.setStruct(struct);
        helloWorld.setParameterAcceptable(parameterAcceptable);
        AssertEquals.assertEqualsParameterAcceptable(parameterAcceptable, new ParameterAcceptable(helloWorld.getBackwardCompatible()));

        BackwardCompatible backwardCompatible = new BackwardCompatible(parameterAcceptable);
        backwardCompatible.setAdded("added");
        helloWorld.setBackwardCompatible(backwardCompatible);
        AssertEquals.assertEqualsBackwardCompatible(backwardCompatible, helloWorld.getBackwardCompatible());
    }

    @Test
    void enumerable() {
        String key = faker.name().lastName();
        String value = faker.name().firstName();
        helloWorld.putEnumerable(key, value);
        assertEquals(value, helloWorld.getEnumerable(key));
        Map<String, String> map = helloWorld.getEnumerableMap();
        assertEquals(value, map.get(key));
        helloWorld.removeEnumerable(key);
        assertNull(helloWorld.getEnumerable(key));
    }

    @Test
    void icxTransfer() {
        Address from = ((IcxTransferScoreClient) icxTransferSender)._address();
        BigInteger preTransferred = icxTransferReceiver.getTransferred(from);
        preTransferred = preTransferred == null ? BigInteger.ZERO : preTransferred;
        Address address = client._address();
        BigInteger value = BigInteger.ONE;
        BigInteger preBalance = client._balance(address);
        Consumer<TransactionResult> transferedEventChecker = (txr) -> {
            for(TransactionResult.EventLog el : txr.getEventLogs()) {
                System.out.println(el);
                if (el.getScoreAddress().equals(address) &&
                        el.getIndexed().get(0).equals("Transferred(Address,int)")) {
                    assertEquals(from,
                            new Address(el.getIndexed().get(1)));
                    assertEquals(value,
                            IconJsonModule.NumberDeserializer.BIG_INTEGER.convert(el.getData().get(0)));
                    return;
                }
            }
        };
        ((IcxTransferScoreClient) icxTransferSender).transfer(
                transferedEventChecker, BigInteger.ONE, address);
        assertEquals(preBalance.add(value), client._balance(address));
        assertEquals(preTransferred.add(value), icxTransferReceiver.getTransferred(from));
    }
}