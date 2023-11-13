package ru.clevertec.proxytask;

import ru.clevertec.proxytask.annotation.Log;
import ru.clevertec.proxytask.testclasses.MathIntTestClass;
import ru.clevertec.proxytask.testclasses.MathInteger;
import ru.clevertec.proxytask.testclasses.UserCRUDRepository;
import ru.clevertec.proxytask.testclasses.UserRepository;
import ru.clevertec.proxytask.util.ProxyFabric;

public class Main {
    public static void main(String[] args) {
        MathInteger testClass = new MathIntTestClass();


        ProxyFabric proxy = new ProxyFabric();
        MathInteger proxyObj = (MathInteger) proxy.proxyCreator(testClass, Log.class);
        System.out.println("min = " + proxyObj.min(9, 15));
        System.out.println("max = " + proxyObj.max(9, 15));
        int[] ar = {8, 5, 3, 11};
        System.out.println("sum = " + proxyObj.sum(ar));

        UserCRUDRepository repo = new UserRepository();
        UserCRUDRepository proxyRepo = (UserCRUDRepository) proxy.proxyCreator(repo, Log.class);

        System.out.println(proxyRepo.getById(1L));
        System.out.println(proxyRepo.getUserByName("Andre"));

    }
}


