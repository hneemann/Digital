/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.*;
import com.thoughtworks.xstream.converters.collections.*;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import com.thoughtworks.xstream.converters.extended.*;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.core.util.SelfStreamingInstanceChecker;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.*;

import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * XStream instance which does not create an illegal access warnings.
 */
public class XStreamValid extends XStream {

    /**
     * Creates a new instance.
     */
    public XStreamValid() {
        super(new StaxDriver());
        addPermission(NoTypePermission.NONE);
        addPermission(PrimitiveTypePermission.PRIMITIVES);
        addPermission(new ExplicitTypePermission(new Class[]{Map.class, Map.Entry.class, File.class, Color.class, String.class, java.util.List.class}));
        addPermission(aClass -> aClass.getName().startsWith("de.neemann."));
    }

    @Override
    protected void setupConverters() {
        this.registerConverter(new ReflectionConverter(getMapper(), getReflectionProvider()), -20);
        this.registerConverter(new SerializableConverter(getMapper(), getReflectionProvider(), getClassLoaderReference()), -10);
        this.registerConverter(new ExternalizableConverter(getMapper(), getClassLoaderReference()), -10);
        this.registerConverter(new NullConverter(), 10000);
        this.registerConverter(new IntConverter(), 0);
        this.registerConverter(new FloatConverter(), 0);
        this.registerConverter(new DoubleConverter(), 0);
        this.registerConverter(new LongConverter(), 0);
        this.registerConverter(new ShortConverter(), 0);
        this.registerConverter((Converter) (new CharConverter()), 0);
        this.registerConverter(new BooleanConverter(), 0);
        this.registerConverter(new ByteConverter(), 0);
        this.registerConverter(new StringConverter(), 0);
        this.registerConverter(new StringBufferConverter(), 0);
        this.registerConverter(new DateConverter(), 0);
        this.registerConverter(new BitSetConverter(), 0);
        this.registerConverter(new URIConverter(), 0);
        this.registerConverter(new URLConverter(), 0);
        this.registerConverter(new BigIntegerConverter(), 0);
        this.registerConverter(new BigDecimalConverter(), 0);
        this.registerConverter(new ArrayConverter(getMapper()), 0);
        this.registerConverter(new CharArrayConverter(), 0);
        this.registerConverter(new CollectionConverter(getMapper()), 0);
        this.registerConverter(new MapConverter(getMapper()), 0);
//        this.registerConverter((Converter)(new TreeMapConverter(getMapper())), 0);
//        this.registerConverter((Converter)(new TreeSetConverter(getMapper())), 0);
        this.registerConverter(new SingletonCollectionConverter(getMapper()), 0);
        this.registerConverter(new SingletonMapConverter(getMapper()), 0);
//        this.registerConverter((Converter)(new PropertiesConverter()), 0);
        this.registerConverter((Converter) (new EncodedByteArrayConverter()), 0);
        this.registerConverter(new FileConverter(), 0);
        if (JVM.isSQLAvailable()) {
            this.registerConverter(new SqlTimestampConverter(), 0);
            this.registerConverter(new SqlTimeConverter(), 0);
            this.registerConverter(new SqlDateConverter(), 0);
        }

//        this.registerConverter((Converter)(new DynamicProxyConverter(getMapper(), getClassLoaderReference())), 0);
        this.registerConverter(new JavaClassConverter(getClassLoaderReference()), 0);
        this.registerConverter(new JavaMethodConverter(getClassLoaderReference()), 0);
        this.registerConverter(new JavaFieldConverter(getClassLoaderReference()), 0);
        if (JVM.isAWTAvailable()) {
//            this.registerConverter((Converter)(new FontConverter(getMapper())), 0);
            this.registerConverter(new ColorConverter(), 0);
//            this.registerConverter((SingleValueConverter)(new TextAttributeConverter()), 0);
        }

        if (JVM.isSwingAvailable()) {
            this.registerConverter(new LookAndFeelConverter(getMapper(), getReflectionProvider()), 0);
        }

        this.registerConverter(new LocaleConverter(), 0);
        this.registerConverter(new GregorianCalendarConverter(), 0);

        this.registerConverter(new EnumConverter(), 0);

        this.registerConverter(new SelfStreamingInstanceChecker(getConverterLookup(), this), 0);
    }

}
