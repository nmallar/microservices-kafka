package com.microservices.demo;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class TestJasypt
{
    public static void main(String[] args) {

        StandardPBEStringEncryptor standardPBEStringEncryptor=new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setPassword("MyNewPassword1234$");
        standardPBEStringEncryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        standardPBEStringEncryptor.setIvGenerator(new RandomIvGenerator());
        String result=standardPBEStringEncryptor.encrypt("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");
        System.out.println(result);
        System.out.println(standardPBEStringEncryptor.decrypt(result));
    }
}
