package com.netease.easyml.common.util;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linjiuning on 2020/07/14.
 */
public class JinjaUtil {
    public static final Pattern VARIABLE = Pattern.compile("\\{\\{.*?}}");
    private static final Lazy<Jinjava> INSTANCE = new Lazy<>(Jinjava::new);

    public static Jinjava getInstance() {
        return INSTANCE.getOrCompute();
    }

    public static String render(String template, Map<String, ?> bindings) {
        try {
            return getInstance().render(template, bindings);
        } catch (Exception ex) {
            System.out.println();
            return "";
        }
    }

    public static void registerFunction(String namespace, String localName,
                                        Class<?> methodClass, String methodName, Class<?>... parameterTypes) {
        Jinjava jinjava = getInstance();
        jinjava.getGlobalContext()
                .registerFunction(new ELFunctionDefinition(namespace, localName, methodClass, methodName, parameterTypes));
    }

    public static void registerFunction(ELFunctionDefinition f) {
        Jinjava jinjava = getInstance();
        jinjava.getGlobalContext()
                .registerFunction(f);
    }

    public static void registerClasses(Class<? extends Importable>... classes) {
        Jinjava jinjava = getInstance();
        jinjava.getGlobalContext().registerClasses(classes);
    }

    public static List<String> variables(String template) {
        List<String> vars = new ArrayList<>();
        Matcher m = VARIABLE.matcher(template);
        while (m.find()) {
            String var = m.group();
            var = StringUtil.strip(var, Pattern.quote("{{}}")).trim();
            vars.add(var);
        }
        return vars;
    }
}
