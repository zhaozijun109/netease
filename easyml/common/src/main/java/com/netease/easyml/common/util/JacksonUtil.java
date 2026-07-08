package com.netease.easyml.common.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linjiuning on 2020/7/14.
 */
public class JacksonUtil {
    private static final Logger log = LoggerFactory.getLogger(JacksonUtil.class);

    private static final Lazy<ObjectMapper> JSON_MAPPER = new Lazy<>(ObjectMapper::new);
    private static final Lazy<ObjectMapper> XML_MAPPER = new Lazy<>(() -> {
        try {
            // xml is not provided by spark, loading at runtime.
            Class<?> clazz = ReflectionUtil.classForName("com.fasterxml.jackson.dataformat.xml.XmlMapper");
            Object o = clazz.newInstance();
            return (ObjectMapper) o;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("Exception: " + e.getMessage());
            return null;
        }
    });
    private static final Lazy<ObjectMapper> YAML_MAPPER = new Lazy<>(() -> {
        try {
            // yaml is not provided by spark, loading at runtime.
            Class<?> clazz = ReflectionUtil.classForName("com.fasterxml.jackson.dataformat.yaml.YAMLFactory");
            Object o = clazz.newInstance();
            return new ObjectMapper((JsonFactory) o);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("Exception: " + e.getMessage());
            return null;
        }
    });

    /**
     * XML To Object
     */
    public static <T> T xmlToBean(String xmlPath, Class<T> cls) {
        try {
            return XML_MAPPER.getOrCompute().readValue(new File(xmlPath), cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> T xmlToBean(File xmlFile, Class<T> cls) {
        try {
            return XML_MAPPER.getOrCompute().readValue(xmlFile, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> T xmlToBean(InputStream xmlInputStream, Class<T> cls) {
        try {
            return XML_MAPPER.getOrCompute().readValue(xmlInputStream, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> String beanToXml(T bean) {
        try {
            return XML_MAPPER.getOrCompute().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: " + e.getMessage());
            return null;
        }
    }

    /**
     * YAML To Object
     */
    public static <T> T yamlToBean(String yamlPath, Class<T> cls) {
        try {
            return YAML_MAPPER.getOrCompute().readValue(new File(yamlPath), cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> T yamlToBean(File yamlFile, Class<T> cls) {
        try {
            return YAML_MAPPER.getOrCompute().readValue(yamlFile, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> T yamlToBean(InputStream yamlInputStream, Class<T> cls) {
        try {
            return YAML_MAPPER.getOrCompute().readValue(yamlInputStream, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> String beanToYaml(T bean) {
        try {
            return YAML_MAPPER.getOrCompute().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Json To Object
     */
    public static <T> T jsonToBean(String jsonData, Class<T> cls) {
        try {
            return JSON_MAPPER.getOrCompute().readValue(jsonData, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Json To Object
     */
    public static <T> T jsonToBean(File jsonFile, Class<T> cls) {
        try {
            return JSON_MAPPER.getOrCompute().readValue(jsonFile, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    public static <T> T jsonToBean(InputStream jsonInputStream, Class<T> cls) {
        try {
            return JSON_MAPPER.getOrCompute().readValue(jsonInputStream, cls);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Object To Json
     */
    public static <T> String beanToJson(T bean) {
        return beanToJson(bean, false);
    }

    public static <T> String beanToJson(T bean, boolean pretty) {
        try {
            ObjectMapper json = JSON_MAPPER.getOrCompute();
            if (pretty)
                return json.writerWithDefaultPrettyPrinter().writeValueAsString(bean);
            else
                return json.writeValueAsString(bean);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return null;
        }
    }
}
