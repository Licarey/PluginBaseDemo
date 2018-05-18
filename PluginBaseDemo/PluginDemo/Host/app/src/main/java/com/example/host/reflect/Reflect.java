/*
 * Copyright (c) 2014, 张涛, lody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.host.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 反射工具类 能够轻松实现反射 并使代码具有强可读性。
 * Created by liming on 2018/5/16.
 */
public class Reflect {

    /**
     * @param name
     *            完整类名
     * @return 工具类自身
     */
    public static Reflect on(String name) throws ReflectException {
        return on(forName(name));
    }

    /**
     * @param clazz
     *            类
     * @return 工具类自身
     */
    public static Reflect on(Class<?> clazz) {
        return new Reflect(clazz);
    }

    /**
     * 包装起一个对象
     *
     * @param object
     *            需要被包装的对象
     */
    public static Reflect on(Object object) {
        return new Reflect(object);
    }

    /**
     * 使受访问权限限制的对象转为不受限制。 一般情况下， 一个类的私有字段和方法是无法获取和调用的， 原因在于调用前Java会检查是否具有可访问权限，
     * 当调用此方法后， 访问权限检查机制将被关闭。
     * 
     * @param accessible
     *            受访问限制的对象
     * @return 不受访问限制的对象
     */
    public static <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        }

        if (accessible instanceof Member) {
            Member member = (Member) accessible;

            if (Modifier.isPublic(member.getModifiers())
                    && Modifier.isPublic(member.getDeclaringClass()
                            .getModifiers())) {

                return accessible;
            }
        }

        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }

        return accessible;
    }

    /**
     * 被包装的对象
     */
    private final Object object;

    /**
     * 反射的是一个Class还是一个Object实例?
     */
    private final boolean isClass;

    private Reflect(Class<?> type) {
        this.object = type;
        this.isClass = true;
    }

    private Reflect(Object object) {
        this.object = object;
        this.isClass = false;
    }

    /**
     * 得到当前包装的对象
     */
    public <T> T get() {
        return (T) object;
    }

    /**
     * 修改一个字段的值
     * @param name
     *            字段名
     * @param value
     *            字段的值
     * @return 完事后的工具类
     * @throws ReflectException
     */
    public Reflect set(String name, Object value) throws ReflectException {
        try {
            Field field = field0(name);
            field.set(object, unwrap(value));
            return this;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 得到字段对值
     * 
     * @param name
     *            字段名
     * @return The field value
     * @throws ReflectException
     * @see #field(String)
     */
    public <T> T get(String name) throws ReflectException {
        return field(name).<T> get();
    }

    /**
     * 取得字段
     * 
     * @param name
     *            字段名
     * @return 字段
     * @throws ReflectException
     */
    public Reflect field(String name) throws ReflectException {
        try {
            Field field = field0(name);
            return on(field.get(object));
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Field field0(String name) throws ReflectException {
        Class<?> type = type();

        try {
            return type.getField(name);
        }

        catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                } catch (NoSuchFieldException ignore) {
                }

                type = type.getSuperclass();
            } while (type != null);

            throw new ReflectException(e);
        }
    }

    /**
     * 将一个对象的所有对象映射到一个Map中,key为字段名。
     * 
     * @return 包含所有字段的map
     */
    public Map<String, Reflect> fields() {
        Map<String, Reflect> result = new LinkedHashMap<String, Reflect>();
        Class<?> type = type();

        do {
            for (Field field : type.getDeclaredFields()) {
                if (!isClass ^ Modifier.isStatic(field.getModifiers())) {
                    String name = field.getName();

                    if (!result.containsKey(name))
                        result.put(name, field(name));
                }
            }

            type = type.getSuperclass();
        } while (type != null);

        return result;
    }

    /**
     * 给定方法名称，调用无参方法
     * <p/>
     *
     * @param name
     *            方法名
     * @return 工具类自身
     * @throws ReflectException
     */
    public Reflect call(String name) throws ReflectException {
        return call(name, new Object[0]);
    }

    /**
     * 给定方法名和参数，调用一个方法。
     *
     * @param name
     *            方法名
     * @param args
     *            方法参数
     * @return 工具类自身
     * @throws ReflectException
     */
    public Reflect call(String name, Object... args) throws ReflectException {
        Class<?>[] types = types(args);

        try {
            Method method = exactMethod(name, types);
            return on(method, object, args);
        }

        catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(name, types);
                return on(method, object, args);
            } catch (NoSuchMethodException e1) {

                throw new ReflectException(e1);
            }
        }
    }

    /**
     * 根据方法名和方法参数得到该方法。
     */
    private Method exactMethod(String name, Class<?>[] types)
            throws NoSuchMethodException {
        Class<?> type = type();

        try {
            return type.getMethod(name, types);
        }

        catch (NoSuchMethodException e) {
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignore) {
                }

                type = type.getSuperclass();
            } while (type != null);

            throw new NoSuchMethodException();
        }
    }

    /**
     * 给定方法名和参数，匹配一个最接近的方法
     */
    private Method similarMethod(String name, Class<?>[] types)
            throws NoSuchMethodException {
        Class<?> type = type();

        // 对于公有方法:
        for (Method method : type.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                return method;
            }
        }

        // 对于私有方法：
        do {
            for (Method method : type.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    return method;
                }
            }

            type = type.getSuperclass();
        } while (type != null);

        throw new NoSuchMethodException("No similar method " + name
                + " with params " + Arrays.toString(types)
                + " could be found on type " + type() + ".");
    }

    /**
     * 再次确认方法签名与实际是否匹配， 将基本类型转换成对应的对象类型， 如int转换成Int
     */
    private boolean isSimilarSignature(Method possiblyMatchingMethod,
            String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName)
                && match(possiblyMatchingMethod.getParameterTypes(),
                        desiredParamTypes);
    }

    /**
     * 为包装的对象创建一个代理。
     * 
     * @param proxyType
     *            代理类型
     * @return 包装对象的代理者。
     */
    @SuppressWarnings("unchecked")
    public <P> P as(Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final InvocationHandler handler = new InvocationHandler() {
            @SuppressWarnings("null")
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                String name = method.getName();

                try {
                    return on(object).call(name, args).get();
                } catch (ReflectException e) {
                    if (isMap) {
                        Map<String, Object> map = (Map<String, Object>) object;
                        int length = (args == null ? 0 : args.length);

                        if (length == 0 && name.startsWith("get")) {
                            return map.get(property(name.substring(3)));
                        } else if (length == 0 && name.startsWith("is")) {
                            return map.get(property(name.substring(2)));
                        } else if (length == 1 && name.startsWith("set")) {
                            map.put(property(name.substring(3)), args[0]);
                            return null;
                        }
                    }

                    throw e;
                }
            }
        };

        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(),
                new Class[] { proxyType }, handler);
    }

    private static String property(String string) {
        int length = string.length();

        if (length == 0) {
            return "";
        } else if (length == 1) {
            return string.toLowerCase();
        } else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class)
                    continue;

                if (wrapper(declaredTypes[i]).isAssignableFrom(
                        wrapper(actualTypes[i])))
                    continue;

                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reflect) {
            return object.equals(((Reflect) obj).get());
        }

        return false;
    }

    @Override
    public String toString() {
        return object.toString();
    }

    private static Reflect on(Method method, Object object, Object... args)
            throws ReflectException {
        try {
            accessible(method);

            if (method.getReturnType() == void.class) {
                method.invoke(object, args);
                return on(object);
            } else {
                return on(method.invoke(object, args));
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 内部类，使一个对象脱离包装
     */
    private static Object unwrap(Object object) {
        if (object instanceof Reflect) {
            return ((Reflect) object).get();
        }

        return object;
    }

    /**
     * 内部类， 给定一系列参数，返回它们的类型
     * 
     * @see Object#getClass()
     */
    private static Class<?>[] types(Object... values) {
        if (values == null) {
            // 空
            return new Class[0];
        }

        Class<?>[] result = new Class[values.length];

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }

        return result;
    }

    /**
     * 加载一个类
     * 
     * @see Class#forName(String)
     */
    private static Class<?> forName(String name) throws ReflectException {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取包装的对象的类型
     * 
     * @see Object#getClass()
     */
    public Class<?> type() {
        if (isClass) {
            return (Class<?>) object;
        } else {
            return object.getClass();
        }
    }

    /**
     * 得到包装的对象的类型， 如果是基本类型,像int,float,boolean这种, 那么将被转换成相应的对象类型。
     */
    public static Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }

        return type;
    }

    /**
     * 定义了一个null类型
     */
    private static class NULL {
    }
}
