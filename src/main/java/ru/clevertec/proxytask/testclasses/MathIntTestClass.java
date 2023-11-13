package ru.clevertec.proxytask.testclasses;

import ru.clevertec.proxytask.annotation.Log;
import ru.clevertec.proxytask.testclasses.MathInteger;

import java.util.Arrays;

public class MathIntTestClass implements MathInteger {

    @Override
    @Log
    public int max(int x, int y) {
        return x > y ? x : y;
    }

    @Log
    @Override
    public int min(int x, int y) {
        return x > y ? y : x;
    }

    @Override
    public int sum(int[] ar) {
        return Arrays.stream(ar).sum();
    }

}
