package com.mysite.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public interface ResourceUtils {
    Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);

    static <T> T getSinglePropertyValue(Node node, String propertyName, Class<T> t){
        String path = null;
        try {
            path = node.getPath();
            if(node.hasProperty(propertyName)){
                Property property = node.getProperty(propertyName);
                return getSingleValue(property.getValue(), t);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Fail to access property {} of node {}", propertyName, path);
        }
        return null;
    }

    static <T> T[] getMultiPropertyValue(Node node, String propertyName, Class<T> t){
        String path = null;
        try {
            path = node.getPath();
            if(node.hasProperty(propertyName)){
                Property property = node.getProperty(propertyName);
                return Stream.of(property.getValues()).map(v -> getSingleValue(v, t))
                    .toArray(new IntFunction<T []>() {
                    @Override
                    public T[] apply(int size) {
                        return (T[]) java.lang.reflect.Array.newInstance(t, size);
                    }
                });
            }
        } catch (RepositoryException e) {
            LOGGER.error("Fail to access property {} of node {}", propertyName, path);
        }
        return null;
    }

    static <T> T getSingleValue(Value v, Class<T> t){
        try {
            if (t.isAssignableFrom(String.class)) {
                return (T) v.getString();
            } else if(t.isAssignableFrom(Boolean.class)){
                return (T) new Boolean(v.getBoolean());
            } else if(t.isAssignableFrom(BigDecimal.class)){
                return (T) v.getDecimal();
            } else if(t.isAssignableFrom(Double.class)){
                return (T) new Double(v.getDouble());
            } else if(t.isAssignableFrom(Long.class)){
                return (T) new Long(v.getLong());
            } else if(t.isAssignableFrom(Binary.class)){
                return (T) v.getBinary();
            } else if(t.isAssignableFrom(Calendar.class)){
                return (T) v.getDate();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }
}
