package com.roger.common.core.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.DataType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @Author: Roger
 * @description:
 * @date: 2022/1/9 10:48 下午
 */
@SuppressWarnings("unchecked")
public class CustomAuthorityDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        String value = defaultJSONParser.parseObject(String.class);
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(value);
        return (T)simpleGrantedAuthority;

    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
