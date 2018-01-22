package com.xy.format.hbt212.core;

import com.xy.format.hbt212.core.cfger.T212Configurator;
import com.xy.format.hbt212.core.deser.*;
import com.xy.format.hbt212.core.feature.ParserFeature;
import com.xy.format.hbt212.core.feature.VerifyFeature;
import com.xy.format.hbt212.exception.T212FormatException;
import com.xy.format.hbt212.model.Data;
import com.xy.format.segment.base.cfger.Feature;
import com.xy.format.segment.core.feature.SegmentParserFeature;

import javax.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * T212映射器
 * Created by xiaoyao9184 on 2018/1/10.
 */
public class T212Mapper {

    private static T212Factory t212FactoryProtoType;

    static {
        try {
            t212FactoryProtoType = new T212Factory();
            //注册 反序列化器
            t212FactoryProtoType.deserializerRegister(CpDataLevelMapDeserializer.class);
            t212FactoryProtoType.deserializerRegister(DataLevelMapDeserializer.class);
            t212FactoryProtoType.deserializerRegister(PackLevelDeserializer.class);
            //默认 反序列化器
            t212FactoryProtoType.deserializerRegister(Map.class, CpDataLevelMapDeserializer.class);
            t212FactoryProtoType.deserializerRegister(Data.class, DataDeserializer.class);
            t212FactoryProtoType.deserializerRegister(Object.class, CpDataLevelMapDeserializer.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private int verifyFeatures;
    private int parserFeatures;
    private T212Factory factory;
    private T212Configurator configurator;
    private Validator validator;


    public T212Mapper(){
        this.factory = t212FactoryProtoType.copy();
        this.configurator = new T212Configurator();
        this.validator = factory.validator();
    }


    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    private static int SEGMENT_FEATURE_BIT_OFFSET = 8;

    public T212Mapper enableDefaultParserFeatures() {
        parserFeatures = Feature.collectFeatureDefaults(SegmentParserFeature.class);
        parserFeatures = parserFeatures << SEGMENT_FEATURE_BIT_OFFSET;
        parserFeatures = parserFeatures | Feature.collectFeatureDefaults(ParserFeature.class);
        return this;
    }

    public T212Mapper enableDefaultVerifyFeatures() {
        verifyFeatures = verifyFeatures | Feature.collectFeatureDefaults(VerifyFeature.class);
        return this;
    }


    public T212Mapper enable(SegmentParserFeature feature) {
        parserFeatures = parserFeatures | feature.getMask() << SEGMENT_FEATURE_BIT_OFFSET;
        return this;
    }

    public T212Mapper enable(ParserFeature feature) {
        parserFeatures = parserFeatures | feature.getMask();
        return this;
    }

    public T212Mapper enable(VerifyFeature feature) {
        verifyFeatures = verifyFeatures | feature.getMask();
        return this;
    }


    public T212Mapper disable(SegmentParserFeature feature) {
        parserFeatures = parserFeatures & ~(feature.getMask() << SEGMENT_FEATURE_BIT_OFFSET);
        return this;
    }

    public T212Mapper disable(ParserFeature feature) {
        parserFeatures = parserFeatures & ~feature.getMask();
        return this;
    }

    public T212Mapper disable(VerifyFeature feature) {
        verifyFeatures = verifyFeatures & ~feature.getMask();
        return this;
    }

    public T212Mapper configurator(T212Configurator configurator){
        this.configurator = configurator;
        return this;
    }

    private T212Mapper buildCfg(){
        configurator.setSegmentParserFeature(this.parserFeatures >> SEGMENT_FEATURE_BIT_OFFSET);
        configurator.setParserFeature(this.parserFeatures & 0x00FF);
        configurator.setVerifyFeature(this.verifyFeatures);
        configurator.setValidator(this.validator);
        factory.setConfigurator(configurator);
        return this;
    }

    /*
    /**********************************************************
    /* Public API (from ObjectCodec): deserialization
    /* (mapping from T212 to Java types);
    /* main methods
    /**********************************************************
     */



    public <T> T readValue(InputStream is, Class<T> value) throws IOException, T212FormatException {
        buildCfg();
        return _readValueAndClose(factory.parser(is),value);
    }

    public <T> T readValue(byte[] bytes, Class<T> value) throws IOException, T212FormatException {
        buildCfg();
        return _readValueAndClose(factory.parser(bytes),value);
    }

    public <T> T readValue(Reader reader, Class<T> value) throws IOException, T212FormatException {
        buildCfg();
        return _readValueAndClose(factory.parser(reader),value);
    }

    public <T> T readValue(String data, Class<T> value) throws IOException, T212FormatException {
        buildCfg();
        return _readValueAndClose(factory.parser(data),value);
    }

    private <T> T _readValueAndClose(T212Parser parser, Class<T> value) throws IOException, T212FormatException {
        T212Deserializer<T> deserializer = factory.deserializerFor(value);
        try(T212Parser p = parser){
            return deserializer.deserialize(p);
        }
    }

    private <T> T _readValueAndClose(T212Parser parser, Type type, Class<T> value) throws IOException, T212FormatException {
        T212Deserializer<T> deserializer = factory.deserializerFor(type,value);
        try(T212Parser p = parser){
            return deserializer.deserialize(p);
        }
    }


    private static Supplier<Type> getMapGenericType(){
        return () -> new Map<String,String>(){
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public String get(Object key) {
                return null;
            }

            @Override
            public String put(String key, String value) {
                return null;
            }

            @Override
            public String remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends String> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public Collection<String> values() {
                return null;
            }

            @Override
            public Set<Entry<String, String>> entrySet() {
                return null;
            }
        }.getClass().getGenericInterfaces()[0];
    }

    public Map<String,String> readMap(InputStream is) throws IOException, T212FormatException {
        buildCfg();
        //noinspection unchecked
        return _readValueAndClose(factory.parser(is),getMapGenericType().get(),Map.class);
    }

    public Map<String,String> readMap(byte[] bytes) throws IOException, T212FormatException {
        buildCfg();
        //noinspection unchecked
        return _readValueAndClose(factory.parser(bytes),getMapGenericType().get(),Map.class);
    }

    public Map<String,String> readMap(Reader reader) throws IOException, T212FormatException {
        buildCfg();
        //noinspection unchecked
        return _readValueAndClose(factory.parser(reader),getMapGenericType().get(),Map.class);
    }

    public Map<String,String> readMap(String data) throws IOException, T212FormatException {
        buildCfg();
        //noinspection unchecked
        return _readValueAndClose(factory.parser(data),getMapGenericType().get(),Map.class);
    }


    public Data readData(InputStream is) throws IOException, T212FormatException {
        //noinspection unchecked
        return readValue(is,Data.class);
    }

    public Data readData(byte[] bytes) throws IOException, T212FormatException {
        //noinspection unchecked
        return readValue(bytes,Data.class);
    }

    public Data readData(Reader reader) throws IOException, T212FormatException {
        //noinspection unchecked
        return readValue(reader,Data.class);
    }

    public Data readData(String data) throws IOException, T212FormatException {
        //noinspection unchecked
        return readValue(data,Data.class);
    }

}