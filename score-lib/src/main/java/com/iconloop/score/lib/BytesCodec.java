package com.iconloop.score.lib;

import score.Address;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

public class BytesCodec<D> extends Codec<D, byte[]> {
    //d.getBytes(StandardCharsets.UTF_8)
    public static BytesCodec<String> STRING = new BytesCodec<>(
            String::getBytes,
            String::new);
    public static BytesCodec<Address> ADDRESS = new BytesCodec<>(
            Address::toByteArray,
            Address::new);
    public static BytesCodec<BigInteger> BIG_INTEGER = new BytesCodec<>(
            BigInteger::toByteArray,
            BigInteger::new);
    public static BytesCodec<Byte> BYTE = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d).toByteArray(),
            (e) -> new BigInteger(e).byteValue());
    public static BytesCodec<Short> SHORT = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d).toByteArray(),
            (e) -> new BigInteger(e).shortValue());
    public static BytesCodec<Integer> INTEGER = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d).toByteArray(),
            (e) -> new BigInteger(e).intValue());
    public static BytesCodec<Long> LONG = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d).toByteArray(),
            (e) -> new BigInteger(e).longValue());
    public static BytesCodec<Character> CHARACTER = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d).toByteArray(),
            (e) -> (char)new BigInteger(e).intValue());
    public static Map<Class, Codec> predefinedCodecs = Map.of(
            String.class, STRING,
            byte[].class, PASS,
            Address.class, ADDRESS,
            BigInteger.class, BIG_INTEGER,
            Byte.class, BYTE,
            Short.class, SHORT,
            Integer.class, INTEGER,
            Long.class, LONG,
            Character.class, CHARACTER
    );

    @SuppressWarnings("unchecked")
    public static <D> Codec<D, byte[]> resolve(Class<D> dClass) {
        return predefinedCodecs.get(dClass);
    }

    public BytesCodec(Function<D, byte[]> encode, Function<byte[], D> decode) {
        super(encode, decode);
    }
}
