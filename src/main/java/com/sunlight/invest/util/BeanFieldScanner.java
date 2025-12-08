package com.sunlight.invest.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//@SpringBootApplication
public class BeanFieldScanner {

    // JDK 1.8 兼容：使用 Arrays.asList + HashSet
    private static final Set<String> BASIC_TYPES = new HashSet<String>(Arrays.asList(
            "byte", "short", "int", "long", "float", "double", "char", "boolean",
            "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
            "java.lang.Float", "java.lang.Double", "java.lang.Character", "java.lang.Boolean",
            "java.lang.String"
    ));
    public static final String PREFIX = "com.sunlight.invest";

    public static void main(String[] args) {
        // 启动 Spring Boot 应用并获取上下文
        ApplicationContext context = SpringApplication.run(BeanFieldScanner.class, args);

        // JDK 1.8 兼容：String.repeat() 替换为循环
        System.out.println("\n" + repeatString("=", 100));
        System.out.println("🔍 开始扫描 Spring Bean 中的基础类型成员变量...");
        System.out.println("（扫描范围：@Component, @Service, @Repository, @Controller）");
        System.out.println(repeatString("=", 100) + "\n");

        int beansWithBasicFields = 0;
        int totalBasicFields = 0;

        // 获取所有 Bean 名称
        String[] beanNames = context.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> beanClass = getTargetClass(bean);  // 处理代理类

            // 过滤非用户 Bean 和非 com.sunlight.invest 包下的 Bean
            if (shouldSkip(beanClass)) {
                continue;
            }

            // 查找基础类型字段
            Set<FieldInfo> basicFields = findBasicFields(beanClass);

            if (!basicFields.isEmpty()) {
                beansWithBasicFields++;
                totalBasicFields += basicFields.size();

                System.out.println("📦 Bean 名称: " + beanName);
                System.out.println("   类名: " + beanClass.getName());
                System.out.println("   基础类型字段:");

                for (FieldInfo field : basicFields) {
                    System.out.printf("     ⚠️  %s %s %s%n",
                            field.modifiers,
                            field.typeName,
                            field.fieldName);
                }
                System.out.println();
            }
        }

        System.out.println(repeatString("=", 100));
        System.out.println("✅ 扫描完成！");
        System.out.println("   包含基础类型字段的 Bean 数量: " + beansWithBasicFields);
        System.out.println("   总的基础类型字段数量: " + totalBasicFields);
        System.out.println("   ⚠️ 注意：基本类型成员变量在单例 Bean 中线程不安全！");
        System.out.println(repeatString("=", 100));

        // 退出应用
        System.exit(0);
    }

    /**
     * JDK 1.8 兼容：替代 String.repeat()
     */
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 获取目标类（处理 AOP 代理类）
     */
    private static Class<?> getTargetClass(Object bean) {
        Class<?> clazz = bean.getClass();
        // 如果是 CGLIB 代理类，返回父类
        if (clazz.getName().contains("$$EnhancerBySpringCGLIB$$")) {
            return clazz.getSuperclass();
        }
        // JDK 1.8 兼容：不使用 AopProxyUtils.ultimateTargetClass()
        // 简单处理 JDK 动态代理（返回第一个接口的实现类）
        if (clazz.getName().contains("$$Proxy")) {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                return interfaces[0];
            }
        }
        return clazz;
    }

    /**
     * 判断是否跳过扫描（过滤 Spring 内部类、JDK 类等，并且只保留 com.sunlight.invest 包下的类）
     */
    private static boolean shouldSkip(Class<?> clazz) {
        String className = clazz.getName();
        // 只扫描 com.sunlight.invest 包下的类
        return !className.startsWith(PREFIX) ||
                className.startsWith("com.sunlight.invest.util.BeanFieldScanner");  // 跳过自身
    }

    /**
     * 查找类中的所有基础类型字段（排除static变量和包含@Value注解的变量）
     */
    private static Set<FieldInfo> findBasicFields(Class<?> clazz) {
        Set<FieldInfo> basicFields = new HashSet<>();

        // 获取当前类所有声明的字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 跳过static变量
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // 跳过包含@Value注解的变量
            if (field.isAnnotationPresent(Value.class)) {
                continue;
            }

            Class<?> type = field.getType();

            // 检查是否是基础类型
            if (isBasicType(type)) {
                String modifiers = Modifier.toString(field.getModifiers());
                String typeName = type.getSimpleName();
                String fieldName = field.getName();

                basicFields.add(new FieldInfo(modifiers, typeName, fieldName));
            }
        }

        return basicFields;
    }

    /**
     * 判断是否为基础类型
     */
    private static boolean isBasicType(Class<?> type) {
        return type.isPrimitive() || BASIC_TYPES.contains(type.getName());
    }

    /**
     * 字段信息封装
     */
    private static class FieldInfo {
        String modifiers;
        String typeName;
        String fieldName;

        FieldInfo(String modifiers, String typeName, String fieldName) {
            this.modifiers = modifiers;
            this.typeName = typeName;
            this.fieldName = fieldName;
        }

        // 用于去重
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldInfo fieldInfo = (FieldInfo) o;
            return fieldName.equals(fieldInfo.fieldName);
        }

        @Override
        public int hashCode() {
            return fieldName.hashCode();
        }
    }
}