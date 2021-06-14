/*
 * Copyright 2021 ICON Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.client;

import com.squareup.javapoet.*;
import foundation.icon.icx.IconService;
import foundation.icon.icx.Wallet;
import foundation.icon.icx.data.Address;
import score.annotation.External;
import score.annotation.Payable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ScoreClientProcessor extends AbstractProcessor {
    static final String METHOD_DEPLOY = "_deploy";
    static final String PARAM_ICON_SERVICE = "iconService";
    static final String PARAM_NID = "nid";
    static final String PARAM_WALLET = "wallet";
    static final String PARAM_ADDRESS = "address";
    static final String PARAM_CLIENT = "client";
    static final String PARAM_SCORE_FILE_PATH = "scoreFilePath";
    static final String PARAM_PARAMS = "params";
    //
    static final String PARAM_PAYABLE_VALUE = "valueForPayable";
    static final String PARAM_STEP_LIMIT = "stepLimit";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add(ScoreClient.class.getCanonicalName());
        return s;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean ret = false;
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotationElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : annotationElements) {
                if (element.getKind().isInterface() || element.getKind().isClass() || element.getKind().isField()) {
                    noteMessage("process %s %s", element.getKind(), element.asType(), element.getSimpleName());
                    generateImplementClass(processingEnv.getFiler(), element);
                    ret = true;
                } else {
                    throw new RuntimeException("not support");
                }
            }
        }
        return ret;
    }

    private void generateImplementClass(Filer filer, Element element) {
        TypeElement typeElement;
        if (element instanceof TypeElement) {
            typeElement = (TypeElement) element;
        } else if (element instanceof VariableElement) {
            typeElement = getTypeElement(element.asType());
        } else {
            throw new RuntimeException("not support");
        }

        ClassName elementClassName = ClassName.get(typeElement);
        ScoreClient ann = element.getAnnotation(ScoreClient.class);
        ClassName className = ClassName.get(elementClassName.packageName(), elementClassName.simpleName() + ann.suffix());
        TypeSpec typeSpec = typeSpec(className, typeElement);
        JavaFile javaFile = JavaFile.builder(className.packageName(), typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec typeSpec(ClassName className, TypeElement element) {
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(DefaultScoreClient.class)
                .addSuperinterfaces(getSuperinterfaces(element));

        if (element.getKind().isInterface()) {
            builder.addSuperinterface(element.asType());
            builder.addMethod(deployMethodSpec(className, null));
        }

        //Constructor
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(IconService.class, PARAM_ICON_SERVICE).build())
                .addParameter(ParameterSpec.builder(BigInteger.class, PARAM_NID).build())
                .addParameter(ParameterSpec.builder(Wallet.class, PARAM_WALLET).build())
                .addParameter(ParameterSpec.builder(Address.class, PARAM_ADDRESS).build())
                .addStatement("super($L, $L, $L, $L)",
                        PARAM_ICON_SERVICE, PARAM_NID, PARAM_WALLET, PARAM_ADDRESS).build());
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(DefaultScoreClient.class, PARAM_CLIENT).build())
                .addStatement("super($L)", PARAM_CLIENT).build());

        builder.addMethods(overrideMethods(element));
        builder.addMethods(deployMethods(className, element));
        return builder.build();
    }

    private List<MethodSpec> overrideMethods(TypeElement element) {
        List<MethodSpec> methods = new ArrayList<>();
        TypeMirror superClass = element.getSuperclass();
        if (!superClass.getKind().equals(TypeKind.NONE) && !superClass.toString().equals(Object.class.getName())) {
            noteMessage("superClass[kind:%s, name:%s]", superClass.getKind().name(), superClass.toString());
            List<MethodSpec> superMethods = overrideMethods(getTypeElement(element.getSuperclass()));
            methods.addAll(superMethods);
        }

        for (TypeMirror inf : element.getInterfaces()) {
            TypeElement infElement = getTypeElement(inf);
            List<MethodSpec> infMethods = overrideMethods(infElement);
            methods.addAll(infMethods);
        }

        boolean mustGenerate = element.getKind().isInterface();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (ElementKind.METHOD.equals(enclosedElement.getKind()) &&
                    hasModifier(enclosedElement, Modifier.PUBLIC) &&
                    !hasModifier(enclosedElement, Modifier.STATIC)) {
                ExecutableElement ee = (ExecutableElement) enclosedElement;
                MethodSpec methodSpec = methodSpec(ee, mustGenerate);
                addMethod(methods, methodSpec, element);
                addMethod(methods, payableMethodSpec(ee, methodSpec), element);
            }
        }
        return methods;
    }

    private void addMethod(List<MethodSpec> methods, MethodSpec methodSpec, TypeElement element) {
        if (methodSpec != null) {
            MethodSpec conflictMethod = getConflictMethod(methods, methodSpec);
            if (conflictMethod != null) {
                methods.remove(conflictMethod);
                if (element.getKind().isInterface()) {
                    warningMessage(
                            "Redeclare '%s %s(%s)' in %s",
                            conflictMethod.returnType.toString(),
                            conflictMethod.name,
                            parameterSpecToString(conflictMethod.parameters),
                            element.getQualifiedName());
                }
            }
            methods.add(methodSpec);
        }
    }

    private CodeBlock paramsCodeblock(ExecutableElement element) {
        if (element == null || element.getParameters() == null || element.getParameters().size() == 0) {
            return null;
        }
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$T<$T,$T> $L = new $T<>()",
                Map.class, String.class, Object.class, PARAM_PARAMS, HashMap.class);
        for (VariableElement ve : element.getParameters()) {
            ParameterSpec ps = ParameterSpec.get(ve);
            builder.addStatement("$L.put(\"$L\",$L)", PARAM_PARAMS, ps.name, ps.name);
        }
        return builder.build();
    }

    private MethodSpec methodSpec(ExecutableElement ee, boolean mustGenerate) {
        String methodName = ee.getSimpleName().toString();
        TypeName returnTypeName = TypeName.get(ee.getReturnType());
        External external = ee.getAnnotation(External.class);
        if (external == null && !mustGenerate) {
            return null;
        }

        MethodSpec.Builder builder = MethodSpec
                .methodBuilder(methodName)
                .addModifiers(getModifiers(ee, Modifier.ABSTRACT))
                .addParameters(getParameterSpecs(ee))
                .returns(returnTypeName);
//                .addAnnotation(Override.class);

        String params = PARAM_PARAMS;
        CodeBlock paramsCodeblock = paramsCodeblock(ee);
        if (paramsCodeblock != null) {
            builder.addCode(paramsCodeblock);
        } else {
            params = "null";
        }

        if (returnTypeName.equals(TypeName.VOID)) {
            if (external != null && external.readonly()) {
                return notSupportedMethod(ee, "not supported, void of readonly method in ScoreClient");
            } else {
                builder.addStatement("super._send(\"$L\", $L)", methodName, params);

                if (ee.getAnnotation(Payable.class) != null) {
//                builder.parameters.stream().map(p -> p.type).collect(Collectors.toList())
                    builder.addJavadoc("To payable, use $L($T $L, ...)", methodName, BigInteger.class, PARAM_PAYABLE_VALUE);
                }
            }
        } else {
            if (external == null || external.readonly()) {
                builder.addStatement("return super._call($T.class, \"$L\", $L)", returnTypeName, methodName, params);
            } else {
                return notSupportedMethod(ee, "not supported response of writable method in ScoreClient");
            }
        }
        return builder.build();
    }

    private MethodSpec payableMethodSpec(ExecutableElement ee, MethodSpec methodSpec) {
        if (methodSpec != null && ee.getAnnotation(Payable.class) != null) {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodSpec.name)
                    .addModifiers(methodSpec.modifiers)
                    .addParameter(BigInteger.class, PARAM_PAYABLE_VALUE)
                    .addParameters(methodSpec.parameters)
                    .returns(methodSpec.returnType);

            CodeBlock paramsCodeblock = paramsCodeblock(ee);
            if (paramsCodeblock != null) {
                builder.addCode(paramsCodeblock);
            }

            if (methodSpec.returnType.equals(TypeName.VOID)) {
                builder.addStatement("super._send($L, \"$L\", $L)",
                        PARAM_PAYABLE_VALUE, methodSpec.name, paramsCodeblock != null ? PARAM_PARAMS : "null");
            } else {
                return notSupportedMethod(ee, "not supported response of payable method in ScoreClient");
            }
            return builder.build();
        } else {
            return null;
        }
    }

    private MethodSpec notSupportedMethod(ExecutableElement ee, String msg) {
        String methodName = ee.getSimpleName().toString();
        TypeName returnTypeName = TypeName.get(ee.getReturnType());
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(getModifiers(ee, Modifier.ABSTRACT))
                .addParameters(getParameterSpecs(ee))
                .returns(returnTypeName)
                .addStatement("throw new $T(\"$L\")", RuntimeException.class, msg)
                .addJavadoc("@deprecated Do not use this method, this is generated only for preventing compile error.\n $L\n", msg)
                .addJavadoc("@throws $L", RuntimeException.class.getName())
                .addAnnotation(Deprecated.class)
                .build();
    }

    private List<MethodSpec> deployMethods(ClassName className, TypeElement element) {
        List<MethodSpec> methods = new ArrayList<>();
        TypeMirror superClass = element.getSuperclass();
        if (!superClass.getKind().equals(TypeKind.NONE) && !superClass.toString().equals(Object.class.getName())) {
            noteMessage("superClass[kind:%s, name:%s]", superClass.getKind().name(), superClass.toString());
            List<MethodSpec> superMethods = deployMethods(className, getTypeElement(element.getSuperclass()));
            methods.addAll(superMethods);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (ElementKind.CONSTRUCTOR.equals(enclosedElement.getKind()) &&
                    hasModifier(enclosedElement, Modifier.PUBLIC)) {
                methods.add(deployMethodSpec(className, (ExecutableElement) enclosedElement));
            }
        }
        return methods;
    }

    private MethodSpec deployMethodSpec(ClassName className, ExecutableElement element) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(METHOD_DEPLOY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(IconService.class, PARAM_ICON_SERVICE).build())
                .addParameter(ParameterSpec.builder(BigInteger.class, PARAM_NID).build())
                .addParameter(ParameterSpec.builder(Wallet.class, PARAM_WALLET).build())
                .addParameter(ParameterSpec.builder(String.class, PARAM_SCORE_FILE_PATH).build())
                .returns(className);

        if (element != null) {
            builder.addParameters(getParameterSpecs(element));
        } else {
            builder.addParameter(ParameterSpec.builder(
                    ParameterizedTypeName.get(Map.class, String.class, Object.class), PARAM_PARAMS).build());
        }

        CodeBlock paramsCodeblock = paramsCodeblock(element);
        if (paramsCodeblock != null) {
            builder.addCode(paramsCodeblock);
        }
        builder
                .addStatement("return new $T($T._deploy($L,$L,$L,$L,$L))",
                        className, DefaultScoreClient.class,
                        PARAM_ICON_SERVICE, PARAM_NID, PARAM_WALLET, PARAM_SCORE_FILE_PATH,
                        paramsCodeblock != null || element == null ? PARAM_PARAMS : "null")
                .build();
        return builder.build();
    }

    //
    public void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        processingEnv.getMessager().printMessage(
                kind, String.format("[%s]",getClass().getSimpleName()) + String.format(format, args));
    }

    public void noteMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, format, args);
    }

    public void warningMessage(String format, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, format, args);
    }

    public TypeElement getTypeElement(TypeMirror type) {
        return (TypeElement)processingEnv.getTypeUtils().asElement(type);
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

    public static MethodSpec getConflictMethod(Iterable<MethodSpec> methodSpecs, MethodSpec target) {
        for (MethodSpec methodSpec : methodSpecs) {
            if (methodSpec.name.equals(target.name) &&
                    compareParameterSpecs(methodSpec.parameters, target.parameters)) {
                return methodSpec;
            }
        }
        return null;
    }

    public static boolean hasModifier(Element element, Modifier... modifiers) {
        for (Modifier modifier : modifiers) {
            if (!element.getModifiers().contains(modifier)) {
                return false;
            }
        }
        return true;
    }

    public static String parameterSpecToString(List<ParameterSpec> parameterSpecs) {
        StringJoiner joiner = new StringJoiner(", ");
        for(ParameterSpec parameterSpec : parameterSpecs) {
            joiner.add(parameterSpec.type.toString());
        }
        return joiner.toString();
    }

    public static List<TypeName> getSuperinterfaces(TypeElement element) {
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        List<TypeName> typeNames = new ArrayList<>();
        if (interfaces != null) {
            for(TypeMirror tm : interfaces) {
                typeNames.add(TypeName.get(tm));
            }
        }
        return typeNames;
    }

    public static Modifier[] getModifiers(Element element, Modifier ... excludes) {
        Set<Modifier> modifierSet = element.getModifiers();
        if (modifierSet == null) {
            return new Modifier[]{};
        } else {
            if (excludes != null && excludes.length > 0) {
                List<Modifier> modifiers = new ArrayList<>();
                List<Modifier> excludeList = Arrays.asList(excludes);
                for(Modifier modifier : modifierSet) {
                    if (!excludeList.contains(modifier)) {
                        modifiers.add(modifier);
                    }
                }
                return modifiers.toArray(new Modifier[0]);
            } else {
                return modifierSet.toArray(new Modifier[0]);
            }
        }
    }

    public static List<ParameterSpec> getParameterSpecs(ExecutableElement element) {
        List<? extends VariableElement> parameters = element.getParameters();
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        if (parameters != null) {
            for(VariableElement ve : parameters) {
                parameterSpecs.add(ParameterSpec.get(ve));
            }
        }
        return parameterSpecs;
    }
}
