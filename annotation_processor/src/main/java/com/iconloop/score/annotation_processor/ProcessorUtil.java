package com.iconloop.score.annotation_processor;

import com.iconloop.score.lib.PropertiesDB;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProcessorUtil {
    private final ProcessingEnvironment processingEnv;
    private final String messagePrefix;

    public ProcessorUtil(ProcessingEnvironment processingEnv, String messagePrefix) {
        this.processingEnv = processingEnv;
        this.messagePrefix = String.format("[%s]",messagePrefix);
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public static boolean hasModifier(Element element, Modifier... modifiers) {
        for (Modifier modifier : modifiers) {
            if (!element.getModifiers().contains(modifier)) {
                return false;
            }
        }
        return true;
    }

    public static <A extends Annotation> boolean hasMethodAnnotation(TypeElement element, Class<A> annotationType) {
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                if (!hasModifier(enclosedElement, Modifier.STATIC)) {
                    A annotation = element.getAnnotation(annotationType);
                    if (annotation != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasInterface(TypeElement element, Class<?> clazz) {
        if (!clazz.isInterface()) {
            throw new RuntimeException(String.format("%s is not interface class", clazz.getName()));
        }
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        for (TypeMirror inf : interfaces) {
            if (clazz.getName().equals(inf.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasInterface(TypeElement element, TypeMirror infType) {
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        for (TypeMirror inf : interfaces) {
            if (inf.toString().equals(infType.toString())) {
                return true;
            }
        }
        return false;
    }

    public static MethodSpec getConflictMethod(Iterable<MethodSpec> methodSpecs, MethodSpec target) {
        for (MethodSpec methodSpec : methodSpecs) {
            if (methodSpec.name.equals(target.name) &&
                    compareParameterSpecs(methodSpec.parameters, target.parameters)) {
                return methodSpec;
            }
        }
        return null;
    }

    public static boolean compareParameterSpecs(List<ParameterSpec> o1, List<ParameterSpec> o2) {
        if (o1.size() == o2.size()) {
            for (int i = 0; i < o1.size(); i++) {
                ParameterSpec p1 = o1.get(i);
                ParameterSpec p2 = o2.get(i);
                if (!p1.type.toString().equals(p2.type.toString())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String parameterSpecToString(List<ParameterSpec> parameterSpecs) {
        StringJoiner joiner = new StringJoiner(", ");
        for(ParameterSpec parameterSpec : parameterSpecs) {
            joiner.add(parameterSpec.type.toString());
        }
        return joiner.toString();
    }

    public void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        processingEnv.getMessager().printMessage(
                kind, String.format(messagePrefix + format, args));
    }

    public void noteMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, format, args);
    }

    public void warningMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, format, args);
    }

    public void mandatoryMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.MANDATORY_WARNING, format, args);
    }

    public void errorMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, format, args);
    }

    public void otherMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.OTHER, format, args);
    }

    public <T extends Annotation> AnnotatedTypeElement<T> getAnnotatedTypeElement(TypeMirror type, Class<T> annotationType) {
        if (!type.getKind().isPrimitive()) {
            TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(type);
            if (element != null) {
                T ann = element.getAnnotation(annotationType);
                if (ann != null){
                    return new AnnotatedTypeElement<>(element, ann);
                }
            } else {
                printMessage(Diagnostic.Kind.WARNING, "%s is not found", type.toString());
//                element = processingEnv.getElementUtils().getTypeElement(type.toString());
            }
        }
        return null;
    }

    public TypeElement getTypeElement(TypeMirror type) {
        return (TypeElement)processingEnv.getTypeUtils().asElement(type);
    }

    public boolean containsDeclaredType(Collection<TypeMirror> list, TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
            TypeMirror varType = element.asType();
            for (TypeMirror e : list) {
                if (processingEnv.getTypeUtils().isSameType(varType, e)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Wrapped method of {@link javax.lang.model.util.Types::isSameType}
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the two types are the same
     */
    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return processingEnv.getTypeUtils().isSameType(t1, t2);
    }


    /**
     * Wrapped method of {@link javax.lang.model.util.Types::isAssignable}
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return {@code true} if and only if the first type is assignable
     *          to the second
     * @throws IllegalArgumentException if given a type for an executable, package, or module
     */
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return processingEnv.getTypeUtils().isAssignable(t1, t2);
    }

    public boolean hasMethod(TypeMirror type, String methodName, Modifier[] modifiers, Class<?> ... parameters) {
        TypeElement element = getTypeElement(type);
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD) &&
                    ProcessorUtil.hasModifier(enclosedElement, modifiers)) {
                ExecutableElement method = (ExecutableElement)enclosedElement;
                List<? extends VariableElement> methodParameters = method.getParameters();
                if (methodParameters.size() == parameters.length) {
                    boolean isEqual = true;
                    for(int i = 0; i< parameters.length; i++) {
                        String methodParameter = methodParameters.get(i).asType().toString();
                        String parameter = parameters[i].getName();
                        if (Object.class.getName().equals(parameter)) {
                            continue;
                        }
                        if (!methodParameter.equals(parameter)) {
                            isEqual = false;
                            break;
                        }
                    }
                    if (isEqual) {
                        noteMessage("found '%s %s(%s)' in %s",
                                Arrays.toString(modifiers),
                                methodName,
                                Arrays.toString(parameters),
                                type);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public TypeMirror getTypeMirror(Class<?> clazz) {
        if (clazz.isArray()) {
            return getArrayType(getTypeMirror(clazz.getComponentType()));
        } else if (clazz.isPrimitive()) {
            return getPrimitiveType(clazz);
        } else {
            return processingEnv.getElementUtils().getTypeElement(clazz.getName()).asType();
        }
    }

    public TypeMirror getArrayType(TypeMirror componentType) {
        return processingEnv.getTypeUtils().getArrayType(componentType);
    }

    public TypeMirror getPrimitiveType(Class<?> clazz) {
        return processingEnv.getTypeUtils().getPrimitiveType(TypeKind.valueOf(clazz.getTypeName().toUpperCase()));
    }

    public TypeMirror getBoxedType(TypeMirror type) {
        return processingEnv.getTypeUtils().boxedClass((PrimitiveType) type).asType();
    }

    public String getDefaultValueAsString(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
                return "false";
            case FLOAT: //return "0.0f";
            case DOUBLE: //return "0.0d";
            case LONG: //return "0L";
            case INT:
            case SHORT:
            case BYTE:
            case CHAR: //return "'\u0000'";
                return "0";
            default:
                return "null";
        }
    }

    public TypeMirror getTypeMirrorFromAnnotation(Supplier<Class<?>> supplier) {
        try {
            return getTypeMirror(supplier.get());
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
}
