package com.ranushan.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstructorUtils {

    public static <T> T invokeConstructor(Class<T> cls) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Constructor<T> ctor = getMatchingAccessibleConstructor(cls);
        if (ctor == null) {
            throw new NoSuchMethodException(
                    "No such accessible constructor on object: " + cls.getName());
        }
        return ctor.newInstance();
    }

    private static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> cls) throws NoSuchMethodException {
        return setAccessibleWorkaround(cls.getConstructor());
    }

    private static <T extends AccessibleObject> T setAccessibleWorkaround(T obj) {
        if (obj == null || obj.isAccessible()) {
            return obj;
        }
        final Member m = (Member) obj;
        if (!obj.isAccessible() && isPublic(m) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                obj.setAccessible(true);
                return obj;
            } catch (final SecurityException ignored) {
                // ignore in favor of subsequent IllegalAccessException
            }
        }
        return obj;
    }

    private static boolean isPublic(Member member) {
        return member != null && Modifier.isPublic(member.getModifiers());
    }

    private static boolean isPackageAccess(int modifiers) {
        return (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == 0;
    }
}
