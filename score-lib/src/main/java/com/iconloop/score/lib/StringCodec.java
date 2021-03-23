package com.iconloop.score.lib;

import score.Address;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

public class StringCodec<D> extends Codec<D, String> {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static StringCodec<byte[]> BYTES = new StringCodec<>(
            (d) -> {
                char[] hexChars = new char[d.length * 2];
                for (int i = 0; i < d.length; i++) {
                    int v = d[i] & 0xFF;
                    hexChars[i * 2] = HEX_ARRAY[v >>> 4];
                    hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
                }
                return new String(hexChars);
            },
            (e) -> {
                if (e.length() % 2 > 0) {
                    throw new IllegalArgumentException("hex cannot has odd length");
                }
                int l = e.length()/2;
                byte[] b = new byte[l];
                for (int i = 0; i < l; i++) {
                    b[i] = (byte)((Character.digit(e.charAt(i), 16) << 4) |
                            Character.digit(e.charAt(i+1), 16));
                }
                return b;
            });

    public static StringCodec<Address> ADDRESS = new StringCodec<>(
            Address::toString,
            Address::fromString);
    public static StringCodec<BigInteger> BIG_INTEGER = new StringCodec<>(
            BigInteger::toString,
            BigInteger::new);
    public static StringCodec<Byte> BYTE = new StringCodec<>(
            (d) -> Byte.toString(d),
            Byte::parseByte);
    public static StringCodec<Short> SHORT = new StringCodec<>(
            (d) -> Short.toString(d),
            Short::parseShort);
    public static StringCodec<Integer> INTEGER = new StringCodec<>(
            (d) -> Integer.toString(d),
            Integer::parseInt);
    public static StringCodec<Long> LONG = new StringCodec<>(
            (d) -> Long.toString(d),
            Long::parseLong);
    public static StringCodec<Character> CHARACTER = new StringCodec<>(
            (d) -> Character.toString(d),
            (e) -> e == null || e.isEmpty() ? null : e.charAt(0));

    public static Map<Class, Codec> predefinedCodecs = Map.of(
            String.class, PASS,
            byte[].class, BYTES,
            Address.class, ADDRESS,
            BigInteger.class, BIG_INTEGER,
            Byte.class, BYTE,
            Short.class, SHORT,
            Integer.class, INTEGER,
            Long.class, LONG,
            Character.class, CHARACTER
    );

    @SuppressWarnings("unchecked")
    public static <D> Codec<D, String> resolve(Class<D> dClass) {
        return predefinedCodecs.get(dClass);
    }

    public StringCodec(Function<D, String> encode, Function<String, D> decode) {
        super(encode, decode);
    }
}
