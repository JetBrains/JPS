// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether.dependencyView;

import org.jetbrains.ether.RW;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 14.02.11
 * Time: 3:54
 * To change this template use File | Settings | File Templates.
 */
public class TypeRepr {

    public static abstract class AbstractType implements RW.Writable {
        public abstract void updateClassUsages(Set<UsageRepr.Usage> s);

        public abstract String getDescr();
    }

    public static class PrimitiveType extends AbstractType {
        public final StringCache.S type;

        @Override
        public String getDescr() {
            return type.value;
        }

        @Override
        public void updateClassUsages(Set<UsageRepr.Usage> s) {

        }

        public void write(final BufferedWriter w) {
            RW.writeln(w, "primitive");
            RW.writeln(w, type.value);
        }

        PrimitiveType(final String type) {
            this.type = StringCache.get(type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PrimitiveType that = (PrimitiveType) o;

            return type.equals(that.type);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }

    public static class ArrayType extends AbstractType {
        public final AbstractType elementType;

        public AbstractType getDeepElementType() {
            AbstractType current = this;

            while (current instanceof ArrayType) {
                current = ((ArrayType) current).elementType;
            }

            return current;
        }

        @Override
        public String getDescr() {
            return "[" + elementType.getDescr();
        }

        @Override
        public void updateClassUsages(Set<UsageRepr.Usage> s) {
            elementType.updateClassUsages(s);
        }

        ArrayType(final AbstractType elementType) {
            this.elementType = elementType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ArrayType arrayType = (ArrayType) o;

            return elementType.equals(arrayType.elementType);
        }

        @Override
        public int hashCode() {
            return elementType.hashCode();
        }

        public void write(BufferedWriter w) {
            RW.writeln(w, "array");
            elementType.write(w);
        }
    }

    public static class ClassType extends AbstractType {
        public final StringCache.S className;
        public final AbstractType[] typeArgs;

        @Override
        public String getDescr() {
            return "L" + className.value + ";";
        }

        @Override
        public void updateClassUsages(Set<UsageRepr.Usage> s) {
            s.add(UsageRepr.createClassUsage(className));
        }

        ClassType(final BufferedReader r){
            className = StringCache.get(RW.readString(r));
            final Collection<AbstractType> args = RW.readMany(r, reader, new LinkedList<AbstractType>());
            typeArgs = args.toArray(new AbstractType[args.size()]);
        }

        ClassType(final String className) {
            this.className = StringCache.get(className);
            typeArgs = new AbstractType[0];
        }

        ClassType(final StringCache.S className) {
            this.className = className;
            typeArgs = new AbstractType[0];
        }

        ClassType(final String className, final AbstractType[] typeArgs) {
            this.className = StringCache.get(className);
            this.typeArgs = typeArgs;
        }

        ClassType(final StringCache.S className, final AbstractType[] typeArgs) {
            this.className = className;
            this.typeArgs = typeArgs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ClassType classType = (ClassType) o;

            if (className != null ? !className.equals(classType.className) : classType.className != null) return false;
            if (!Arrays.equals(typeArgs, classType.typeArgs)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (typeArgs != null ? Arrays.hashCode(typeArgs) : 0);
            return result;
        }

        public void write(BufferedWriter w) {
            RW.writeln(w, "class");
            RW.writeln(w, className.value);
            RW.writeln(w, typeArgs);
        }
    }

    public static ClassType createClassType(final String s) {
        return (ClassType) getType(new ClassType(s));
    }

    public static ClassType createClassType(final StringCache.S s) {
        return (ClassType) getType(new ClassType(s));
    }

    public static ClassType[] createClassType(final String[] s) {
        if (s == null) {
            return null;
        }

        final ClassType[] types = new ClassType[s.length];

        for (int i = 0; i < types.length; i++) {
            types[i] = createClassType(s[i]);
        }

        return types;
    }

    public static Collection<AbstractType> createClassType(final String[] s, final Collection<AbstractType> acc) {
        if (s != null) {
            for (String ss : s) {
                acc.add(createClassType(ss));
            }
        }

        return acc;
    }

    public static Collection<AbstractType> createClassType(final Collection<String> s, final Collection<AbstractType> acc) {
        if (s != null) {
            for (String ss : s) {
                acc.add(createClassType(ss));
            }
        }

        return acc;
    }

    private static final Map<AbstractType, AbstractType> map = new HashMap<AbstractType, AbstractType>();

    private static AbstractType getType(final AbstractType t) {
        final AbstractType r = map.get(t);

        if (r != null) {
            return r;
        }

        map.put(t, t);

        return t;
    }

    public static AbstractType getType(final String descr) {
        final Type t = Type.getType(descr);

        switch (t.getSort()) {
            case Type.OBJECT:
                return getType(new ClassType(t.getClassName()));

            case Type.ARRAY:
                return getType(new ArrayType(getType(t.getElementType())));

            default:
                return getType(new PrimitiveType(descr));
        }
    }

    public static AbstractType getType(final Type t) {
        return getType(t.getDescriptor());
    }

    public static AbstractType[] getType(final Type[] t) {
        final AbstractType[] r = new AbstractType[t.length];

        for (int i = 0; i < r.length; i++)
            r[i] = getType(t[i]);

        return r;
    }

    public static AbstractType[] getType(final String[] t) {
        if (t == null) {
            return null;
        }

        final AbstractType[] types = new AbstractType[t.length];

        for (int i = 0; i < types.length; i++) {
            types[i] = getType(Type.getType(t[i]));
        }

        return types;
    }

    public static RW.Reader<AbstractType> reader = new RW.Reader<AbstractType>() {
        public AbstractType read(final BufferedReader r) {
            AbstractType elementType = null;
            int level = 0;

            while (true) {
                final String tag = RW.readString(r);

                if (tag.equals("primitive")) {
                    elementType = getType(new PrimitiveType(RW.readString(r)));
                    break;
                }

                if (tag.equals("class")) {
                    elementType = getType(new ClassType(r));
                    break;
                }

                if (tag.equals("array")) {
                    level++;
                }
            }

            for (int i = 0; i < level; i++) {
                elementType = getType(new ArrayType(elementType));
            }

            return elementType;
        }
    };

    public static RW.ToWritable<AbstractType> fromAbstractType = new RW.ToWritable<AbstractType>() {
        public RW.Writable convert(final AbstractType x) {
            return new RW.Writable() {
                public void write(final BufferedWriter w) {
                    x.write(w);
                }
            };
        }
    };
}
