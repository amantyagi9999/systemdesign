package com.demo.util;

public final class Base62IdEncoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();

    private Base62IdEncoder() {
    }

    public static String encode(long id){
        if(id < 0){
            throw new IllegalArgumentException("ID must be non-negative");
        }
        if(id == 0){
            return String.valueOf(BASE62_CHARS.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        long value = id;
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public long decode(String shortCode){
        long result = 0;
        for(char ch : shortCode.toCharArray()){
            result = result * BASE + BASE62_CHARS.indexOf(ch);
        }
        return result;
    }


}
