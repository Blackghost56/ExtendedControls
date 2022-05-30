package com.blackghost.extendedcontrols;

public class Tools {

    public static <T extends Comparable<T>> boolean checkRange(T value, T min, T max){
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public static <T extends Comparable<T>> T checkRangeAndTrim(T value, T min, T max){
        if (value.compareTo(min) < 0){
            return min;
        } else if  (value.compareTo(max) > 0){
            return max;
        }

        return value;
    }

}
