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
    public static BytesCodec<byte[]> BYTES = new BytesCodec<>(
            (d) -> d,
            (e) -> e);
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
    public static BytesCodec<Boolean> BOOLEAN = new BytesCodec<>(
            (d) -> BigInteger.valueOf(d ? 1 : 0).toByteArray(),
            (e) -> new BigInteger(e).signum() != 0);
    public static BytesCodec<Float> FLOAT = new BytesCodec<>(
            (d) -> BigInteger.valueOf(Float.floatToIntBits(d)).toByteArray(),
            (e) -> Float.intBitsToFloat(new BigInteger(e).intValue()));
    public static BytesCodec<Double> DOUBLE = new BytesCodec<>(
            (d) -> BigInteger.valueOf(Double.doubleToLongBits(d)).toByteArray(),
            (e) -> Double.longBitsToDouble(new BigInteger(e).longValue()));

    public static Map<Class, BytesCodec> predefinedCodecs = Map.ofEntries(
            Map.entry(String.class, STRING),
            Map.entry(byte[].class, BYTES),
            Map.entry(Address.class, ADDRESS),
            Map.entry(BigInteger.class, BIG_INTEGER),
            Map.entry(Byte.class, BYTE),
            Map.entry(Short.class, SHORT),
            Map.entry(Integer.class, INTEGER),
            Map.entry(Long.class, LONG),
            Map.entry(Character.class, CHARACTER),
            Map.entry(Boolean.class, BOOLEAN),
            Map.entry(Float.class, FLOAT),
            Map.entry(Double.class, DOUBLE)
    );

    @SuppressWarnings("unchecked")
    public static <D> BytesCodec<D> resolve(Class<D> dClass) {
        return predefinedCodecs.get(dClass);
    }

    public BytesCodec(Function<D, byte[]> encode, Function<byte[], D> decode) {
        super(encode, decode);
    }
}
