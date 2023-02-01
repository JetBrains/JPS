// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether.dependencyView;

import com.sun.org.apache.bcel.internal.generic.PUTFIELD;
import org.jetbrains.ether.Pair;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 31.01.11
 * Time: 2:00
 * To change this template use File | Settings | File Templates.
 */

public class ClassfileAnalyzer {
    private static class Holder<T> {
        private T x = null;

        public void set(final T x) {
            this.x = x;
        }

        public T get() {
            return x;
        }
    }

    private static class ClassCrawler extends EmptyVisitor {
        private class AnnotationRetentionPolicyCrawler implements AnnotationVisitor {
            public void visit(String name, Object value) {
            }

            public void visitEnum(String name, String desc, String value) {
                policy = RetentionPolicy.valueOf(value);
            }

            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return null;
            }

            public AnnotationVisitor visitArray(String name) {
                return null;
            }

            public void visitEnd() {
            }
        }

        private class AnnotationTargetCrawler implements AnnotationVisitor {
            public void visit(String name, Object value) {
            }

            public void visitEnum(final String name, String desc, final String value) {
                targets.add(ElementType.valueOf(value));
            }

            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return this;
            }

            public AnnotationVisitor visitArray(String name) {
                return this;
            }

            public void visitEnd() {
            }
        }

        private class AnnotationCrawler implements AnnotationVisitor {
            private final TypeRepr.ClassType type;
            private final ElementType target;

            private final Set<StringCache.S> usedArguments = new HashSet<StringCache.S>();

            private AnnotationCrawler(final TypeRepr.ClassType type, final ElementType target) {
                this.type = type;
                this.target = target;
                annotationTargets.put(type, target);
                usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(type.className));
            }

            private String getMethodDescr(final Object value) {
                if (value instanceof Type) {
                    return "()Ljava/lang/Class;";
                }

                final String name = Type.getType(value.getClass()).getInternalName();

                if (name.equals("java/lang/Integer")) {
                    return "()I;";
                }

                if (name.equals("java/lang/Short")) {
                    return "()S;";
                }

                if (name.equals("java/lang/Long")) {
                    return "()J;";
                }

                if (name.equals("java/lang/Byte")) {
                    return "()B;";
                }

                if (name.equals("java/lang/Char")) {
                    return "()C;";
                }

                if (name.equals("java/lang/Boolean")) {
                    return "()Z;";
                }

                if (name.equals("java/lang/Float")) {
                    return "()F;";
                }

                if (name.equals("java/lang/Double")) {
                    return "()D;";
                }

                final String s = "()L" + name + ";";

                return s;
            }

            public void visit(String name, Object value) {
                usages.addUsage(classNameHolder.get(), UsageRepr.createMethodUsage(name, type.className.value, getMethodDescr(value)));
                usedArguments.add(StringCache.get(name));
            }

            public void visitEnum(String name, String desc, String value) {
                usages.addUsage(classNameHolder.get(), UsageRepr.createMethodUsage(name, type.className.value, "()" + desc));
                usedArguments.add(StringCache.get(name));
            }

            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return new AnnotationCrawler((TypeRepr.ClassType) TypeRepr.getType(desc), target);
            }

            public AnnotationVisitor visitArray(String name) {
                usedArguments.add(StringCache.get(name));
                return this;
            }

            public void visitEnd() {
                final Set<StringCache.S> s = annotationArguments.get(type);

                if (s == null) {
                    annotationArguments.put(type, usedArguments);
                } else {
                    s.retainAll(usedArguments);
                }
            }
        }

        private void processSignature(final String sig) {
            if (sig != null)
                new SignatureReader(sig).accept(signatureCrawler);
        }

        private final SignatureVisitor signatureCrawler = new SignatureVisitor() {
            public void visitFormalTypeParameter(String name) {
                return;
            }

            public SignatureVisitor visitClassBound() {
                return this;
            }

            public SignatureVisitor visitInterfaceBound() {
                return this;
            }

            public SignatureVisitor visitSuperclass() {
                return this;
            }

            public SignatureVisitor visitInterface() {
                return this;
            }

            public SignatureVisitor visitParameterType() {
                return this;
            }

            public SignatureVisitor visitReturnType() {
                return this;
            }

            public SignatureVisitor visitExceptionType() {
                return this;
            }

            public void visitBaseType(char descriptor) {
                return;
            }

            public void visitTypeVariable(String name) {
                return;
            }

            public SignatureVisitor visitArrayType() {
                return this;
            }

            public void visitInnerClassType(String name) {
                return;
            }

            public void visitTypeArgument() {
                return;
            }

            public SignatureVisitor visitTypeArgument(char wildcard) {
                return this;
            }

            public void visitEnd() {
            }

            public void visitClassType(String name) {
                usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(name));
            }
        };

        Boolean takeIntoAccount = false;

        final StringCache.S fileName;
        int access;
        StringCache.S name;
        String superClass;
        String[] interfaces;
        String signature;
        StringCache.S sourceFile;

        final Holder<String> classNameHolder = new Holder<String>();

        final Set<MethodRepr> methods = new HashSet<MethodRepr>();
        final Set<FieldRepr> fields = new HashSet<FieldRepr>();
        final List<String> nestedClasses = new ArrayList<String>();
        final UsageRepr.Cluster usages = new UsageRepr.Cluster();
        final Set<UsageRepr.Usage> annotationUsages = new HashSet<UsageRepr.Usage>();
        final Set<ElementType> targets = new HashSet<ElementType>();
        RetentionPolicy policy = null;

        private static FoxyMap.CollectionConstructor<ElementType> elementTypeSetConstructor = new FoxyMap.CollectionConstructor<ElementType>() {
            public Collection<ElementType> create() {
                return new HashSet<ElementType>();
            }
        };

        final Map<TypeRepr.ClassType, Set<StringCache.S>> annotationArguments = new HashMap<TypeRepr.ClassType, Set<StringCache.S>>();
        final FoxyMap<TypeRepr.ClassType, ElementType> annotationTargets = new FoxyMap<TypeRepr.ClassType, ElementType>(elementTypeSetConstructor);

        public ClassCrawler(final StringCache.S fn) {
            fileName = fn;
        }

        private boolean notPrivate(final int access) {
            return (access & Opcodes.ACC_PRIVATE) == 0;
        }

        public Pair<ClassRepr, Pair<UsageRepr.Cluster, Set<UsageRepr.Usage>>> getResult() {
            final ClassRepr repr = takeIntoAccount ?
                    new ClassRepr(access, sourceFile, fileName, name, signature, superClass, interfaces, nestedClasses, fields, methods, targets, policy) : null;

            if (repr != null) {
                repr.updateClassUsages(usages.getUsages());
            }

            return new Pair<ClassRepr, Pair<UsageRepr.Cluster, Set<UsageRepr.Usage>>>(repr, new Pair<UsageRepr.Cluster, Set<UsageRepr.Usage>>(usages, annotationUsages));
        }

        @Override
        public void visit(int version, int a, String n, String sig, String s, String[] i) {
            takeIntoAccount = notPrivate(a);

            access = a;
            name = StringCache.get(n);
            signature = sig;
            superClass = s;
            interfaces = i;

            classNameHolder.set(n);

            if (superClass != null) {
                usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(StringCache.get(superClass)));
                usages.addUsage(classNameHolder.get(), UsageRepr.createClassExtendsUsage(StringCache.get(superClass)));
            }

            if (interfaces != null) {
                for (String it : interfaces) {
                    usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(StringCache.get(it)));
                    usages.addUsage(classNameHolder.get(), UsageRepr.createClassExtendsUsage(StringCache.get(it)));
                }
            }

            processSignature(sig);
        }

        @Override
        public void visitEnd() {
            for (TypeRepr.ClassType type : annotationTargets.keySet()) {
                final Collection<ElementType> targets = annotationTargets.foxyGet(type);
                final Set<StringCache.S> usedArguments = annotationArguments.get(type);

                annotationUsages.add(UsageRepr.createAnnotationUsage(type, usedArguments, targets));
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            if (desc.equals("Ljava/lang/annotation/Target;")) {
                return new AnnotationTargetCrawler();
            }

            if (desc.equals("Ljava/lang/annotation/Retention;")) {
                return new AnnotationRetentionPolicyCrawler();
            }

            return new AnnotationCrawler((TypeRepr.ClassType) TypeRepr.getType(desc), (access & Opcodes.ACC_ANNOTATION) > 0 ? ElementType.ANNOTATION_TYPE : ElementType.TYPE);
        }

        @Override
        public void visitSource(String source, String debug) {
            sourceFile = StringCache.get(source);
        }

        @Override
        public FieldVisitor visitField(int access, String n, String desc, String signature, Object value) {
            processSignature(signature);

            if (notPrivate(access)) {
                fields.add(new FieldRepr(access, n, desc, signature, value));
            }

            return new EmptyVisitor() {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new AnnotationCrawler((TypeRepr.ClassType) TypeRepr.getType(desc), ElementType.FIELD);
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String n, final String desc, final String signature, final String[] exceptions) {
            final Holder<Object> defaultValue = new Holder<Object>();

            processSignature(signature);

            return new EmptyVisitor() {
                @Override
                public void visitEnd() {
                    if (notPrivate(access)) {
                        methods.add(new MethodRepr(access, n, signature, desc, exceptions, defaultValue.get()));
                    }
                }

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return new AnnotationCrawler((TypeRepr.ClassType) TypeRepr.getType(desc), n.equals("<init>") ? ElementType.CONSTRUCTOR : ElementType.METHOD);
                }

                @Override
                public AnnotationVisitor visitAnnotationDefault() {
                    return new EmptyVisitor() {
                        public void visit(String name, Object value) {
                            defaultValue.set(value);
                        }
                    };
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                    return new AnnotationCrawler((TypeRepr.ClassType) TypeRepr.getType(desc), ElementType.PARAMETER);
                }

                @Override
                public void visitMultiANewArrayInsn(String desc, int dims) {
                    final TypeRepr.ArrayType typ = (TypeRepr.ArrayType) TypeRepr.getType(desc);
                    final TypeRepr.AbstractType element = typ.getDeepElementType();

                    if (element instanceof TypeRepr.ClassType) {
                        usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(((TypeRepr.ClassType) element).className));
                        usages.addUsage(classNameHolder.get(), UsageRepr.createClassNewUsage(((TypeRepr.ClassType) element).className));
                    }

                    typ.updateClassUsages(usages.getUsages());

                    super.visitMultiANewArrayInsn(desc, dims);
                }

                @Override
                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                    processSignature(signature);
                    TypeRepr.getType(desc).updateClassUsages(usages.getUsages());
                    super.visitLocalVariable(name, desc, signature, start, end, index);
                }

                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    if (type != null) {
                        TypeRepr.createClassType(type).updateClassUsages(usages.getUsages());
                    }

                    super.visitTryCatchBlock(start, end, handler, type);
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    final TypeRepr.AbstractType typ = type.startsWith("[") ? TypeRepr.getType(type) : TypeRepr.createClassType(type);

                    if (opcode == Opcodes.NEW) {
                        usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(((TypeRepr.ClassType) typ).className));
                        usages.addUsage(classNameHolder.get(), UsageRepr.createClassNewUsage(((TypeRepr.ClassType) typ).className));
                    } else if (opcode == Opcodes.ANEWARRAY) {
                        if (typ instanceof TypeRepr.ClassType) {
                            usages.addUsage(classNameHolder.get(), UsageRepr.createClassUsage(((TypeRepr.ClassType) typ).className));
                            usages.addUsage(classNameHolder.get(), UsageRepr.createClassNewUsage(((TypeRepr.ClassType) typ).className));
                        }
                    }

                    typ.updateClassUsages(usages.getUsages());

                    super.visitTypeInsn(opcode, type);
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) {
                        usages.addUsage(classNameHolder.get(), UsageRepr.createFieldAssignUsage(name, owner, desc));
                    }
                    usages.addUsage(classNameHolder.get(), UsageRepr.createFieldUsage(name, owner, desc));
                    super.visitFieldInsn(opcode, owner, name, desc);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                    usages.addUsage(classNameHolder.get(), UsageRepr.createMethodUsage(name, owner, desc));
                    super.visitMethodInsn(opcode, owner, name, desc);
                }
            };
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if (outerName != null && outerName.equals(name) && notPrivate(access)) {
                nestedClasses.add(innerName);
            }
        }
    }

    public static Pair<ClassRepr, Pair<UsageRepr.Cluster, Set<UsageRepr.Usage>>> analyze(final StringCache.S fileName, final ClassReader cr) {
        final ClassCrawler visitor = new ClassCrawler(fileName);

        cr.accept(visitor, 0);

        return visitor.getResult();
    }
}