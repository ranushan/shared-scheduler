package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonRegEx {
    public static final String JAVA_PACKAGE_NAME = "^[a-z]+(\\.[a-z0-9]+)*$";
}
