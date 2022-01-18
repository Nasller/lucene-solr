package org.apache.solr.search.function.myfunc.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtil {
	private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private final static ObjectMapper mapper;

    private final static ObjectMapper customizeMapper;

    static {
        mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //反序列化时，不存在的属性不做处理
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);//特殊字符转义
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES,true);
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);//允许反斜杠

        customizeMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //反序列化时，不存在的属性不做处理
        customizeMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);//特殊字符转义
        customizeMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES,true);
        customizeMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);//允许反斜杠
    }

    public static String serialize(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("serialize error", e);
            return "";
        }
    }

    public static String serializeWithNull(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return customizeMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("serialize error", e);
            return "";
        }
    }

    public static <T> T deserialize(String s, Class<T> clazz) {
        if (s == null || s.trim().length() <= 0) {
            return null;
        }
        try {
            return mapper.readValue(s, clazz);
        } catch (IOException e) {
            log.error("deserialize error", e);
            return null;
        }
    }

	public static <T> T convertValue(Object fromValue, Class<T> clazz) {
		if (fromValue == null) {
			return null;
		}
		return mapper.convertValue(fromValue, clazz);
	}

	public static <T> List<T> convertValueList(List<Object> fromValue, Class<T> clazz) {
		if (CollectionUtils.isEmpty(fromValue)) {
			return null;
		}
		return fromValue.stream().map(o->mapper.convertValue(o, clazz)).collect(Collectors.toList());
	}

    private static TypeFactory getTypeFactory() {
        return mapper.getTypeFactory();
    }

    public static <T> T deserialize(String s, JavaType javaType) {
        if (s == null || s.trim().length() <= 0) {
            return null;
        }
        try {
            return mapper.readValue(s, javaType);
        } catch (IOException e) {
            log.error("deserialize error", e);
            return null;
        }
    }

    public static <T> T[] deserializeArray(String s, Class<T> elementType) {
	    ArrayType type = getTypeFactory().constructArrayType(elementType);
        return deserialize(s, type);
    }

    public static <T> Collection<T> deserializeCollection(String s, Class<T> elementType) {
	    CollectionType type = getTypeFactory().constructCollectionType(Collection.class, elementType);
        return deserialize(s, type);
    }

    public static <T> List<T> deserializeList(String s, Class<T> elementType) {
	    JavaType type = getTypeFactory().constructParametricType(List.class, elementType);
        return deserialize(s, type);
    }

    public static <T> Map<String, T> deserializeMap(String s, Class<T> valueType) {
	    MapType type = getTypeFactory().constructMapType(Map.class, String.class, valueType);
        return deserialize(s, type);
    }

    public static <K, T> Map<K, T> deserializeMap(String s, Class<K> keyType, Class<T> valueType) {
	    MapType type = getTypeFactory().constructMapType(Map.class, keyType, valueType);
        return deserialize(s, type);
    }

    public static ObjectMapper getMapper() {
        //val type = getTypeFactory().constructParametricType(Map.class,  allClass);
        return mapper;
    }

    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }


    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     */
    public static JavaType getJavaType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static JsonNode readJson(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(jsonStr);
        } catch (IOException e) {
            log.error("failed to readJson, the json String is :{}.", jsonStr);
            log.error("failed to readJson, the error is :", e);
        }
        return jsonNode;
    }
}