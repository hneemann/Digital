/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
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

/**
 * XStream instance which does not create an illegal access warnings.
 */
public class LegalXStream extends XStream {

    /**
     * Creates a new instance.
     */
    public LegalXStream() {
        super(new StaxDriver());
    }

    @Override
    protected void setupConverters() {
        this.registerConverter((Converter) (new ReflectionConverter(getMapper(), getReflectionProvider())), -20);
        this.registerConverter((Converter) (new SerializableConverter(getMapper(), getReflectionProvider(), getClassLoaderReference())), -10);
        this.registerConverter((Converter) (new ExternalizableConverter(getMapper(), getClassLoaderReference())), -10);
        this.registerConverter((Converter) (new NullConverter()), 10000);
        this.registerConverter((SingleValueConverter) (new IntConverter()), 0);
        this.registerConverter((SingleValueConverter) (new FloatConverter()), 0);
        this.registerConverter((SingleValueConverter) (new DoubleConverter()), 0);
        this.registerConverter((SingleValueConverter) (new LongConverter()), 0);
        this.registerConverter((SingleValueConverter) (new ShortConverter()), 0);
        this.registerConverter((Converter) (new CharConverter()), 0);
        this.registerConverter((SingleValueConverter) (new BooleanConverter()), 0);
        this.registerConverter((SingleValueConverter) (new ByteConverter()), 0);
        this.registerConverter((SingleValueConverter) (new StringConverter()), 0);
        this.registerConverter((SingleValueConverter) (new StringBufferConverter()), 0);
        this.registerConverter((SingleValueConverter) (new DateConverter()), 0);
        this.registerConverter((Converter) (new BitSetConverter()), 0);
        this.registerConverter((SingleValueConverter) (new URIConverter()), 0);
        this.registerConverter((SingleValueConverter) (new URLConverter()), 0);
        this.registerConverter((SingleValueConverter) (new BigIntegerConverter()), 0);
        this.registerConverter((SingleValueConverter) (new BigDecimalConverter()), 0);
        this.registerConverter((Converter) (new ArrayConverter(getMapper())), 0);
        this.registerConverter((Converter) (new CharArrayConverter()), 0);
        this.registerConverter((Converter) (new CollectionConverter(getMapper())), 0);
        this.registerConverter((Converter) (new MapConverter(getMapper())), 0);
//        this.registerConverter((Converter)(new TreeMapConverter(getMapper())), 0);
//        this.registerConverter((Converter)(new TreeSetConverter(getMapper())), 0);
        this.registerConverter((Converter) (new SingletonCollectionConverter(getMapper())), 0);
        this.registerConverter((Converter) (new SingletonMapConverter(getMapper())), 0);
//        this.registerConverter((Converter)(new PropertiesConverter()), 0);
        this.registerConverter((Converter) (new EncodedByteArrayConverter()), 0);
        this.registerConverter((SingleValueConverter) (new FileConverter()), 0);
        if (JVM.isSQLAvailable()) {
            this.registerConverter((SingleValueConverter) (new SqlTimestampConverter()), 0);
            this.registerConverter((SingleValueConverter) (new SqlTimeConverter()), 0);
            this.registerConverter((SingleValueConverter) (new SqlDateConverter()), 0);
        }

//        this.registerConverter((Converter)(new DynamicProxyConverter(getMapper(), getClassLoaderReference())), 0);
        this.registerConverter((SingleValueConverter) (new JavaClassConverter(getClassLoaderReference())), 0);
        this.registerConverter((Converter) (new JavaMethodConverter(getClassLoaderReference())), 0);
        this.registerConverter((Converter) (new JavaFieldConverter(getClassLoaderReference())), 0);
        if (JVM.isAWTAvailable()) {
//            this.registerConverter((Converter)(new FontConverter(getMapper())), 0);
            this.registerConverter((Converter) (new ColorConverter()), 0);
//            this.registerConverter((SingleValueConverter)(new TextAttributeConverter()), 0);
        }

        if (JVM.isSwingAvailable()) {
            this.registerConverter((Converter) (new LookAndFeelConverter(getMapper(), getReflectionProvider())), 0);
        }

        this.registerConverter((SingleValueConverter) (new LocaleConverter()), 0);
        this.registerConverter((Converter) (new GregorianCalendarConverter()), 0);

        this.registerConverter(new EnumConverter(), 0);

        this.registerConverter((Converter) (new SelfStreamingInstanceChecker(getConverterLookup(), this)), 0);
    }

}
