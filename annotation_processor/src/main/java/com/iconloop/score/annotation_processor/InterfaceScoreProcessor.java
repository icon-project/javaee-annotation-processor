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

package com.iconloop.score.annotation_processor;

import com.squareup.javapoet.*;
import score.Address;
import score.Context;
import score.annotation.Payable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class InterfaceScoreProcessor extends AbstractProcessor {
    private ProcessorUtil util;

    static final String ADDRESS_MEMBER = "address";
    static final String PAYABLE_VALUE_MEMBER = "valueForPayable";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        util = new ProcessorUtil(processingEnv, InterfaceScoreProcessor.class.getSimpleName());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add(InterfaceScore.class.getCanonicalName());
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
                if (element.getKind().isInterface()) {
                    util.noteMessage("%s", element.toString());
                    generateImplementClass(processingEnv.getFiler(), (TypeElement) element);
                    ret = true;
                } else {
                    throw new RuntimeException("not support");
                }
            }
        }
        return ret;
    }

    private void generateImplementClass(Filer filer, TypeElement element) {
        ClassName interfaceClassName = ClassName.get(element);
        TypeSpec typeSpec = typeSpec(element);
        JavaFile javaFile = JavaFile.builder(interfaceClassName.packageName(), typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec typeSpec(TypeElement element) {
        ClassName interfaceClassName = ClassName.get(element);
        InterfaceScore interfaceScore = element.getAnnotation(InterfaceScore.class);
        ClassName className = ClassName.get(interfaceClassName.packageName(), interfaceClassName.simpleName() + interfaceScore.suffix());
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(ClassName.get(interfaceClassName.packageName(), className.simpleName()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface((TypeName.get(element.asType())));

        //Fields
        builder.addField(Address.class, ADDRESS_MEMBER, Modifier.PROTECTED, Modifier.FINAL);
        builder.addField(BigInteger.class, PAYABLE_VALUE_MEMBER, Modifier.PROTECTED, Modifier.FINAL);

        //Constructor
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Address.class, ADDRESS_MEMBER).build())
                .addStatement("this.$L = $L", ADDRESS_MEMBER, ADDRESS_MEMBER)
                .addStatement("this.$L = null", PAYABLE_VALUE_MEMBER).build());
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Address.class, ADDRESS_MEMBER).build())
                .addParameter(ParameterSpec.builder(BigInteger.class, PAYABLE_VALUE_MEMBER).build())
                .addStatement("this.$L = $L", ADDRESS_MEMBER, ADDRESS_MEMBER)
                .addStatement("this.$L = $L", PAYABLE_VALUE_MEMBER, PAYABLE_VALUE_MEMBER).build());

        //addressGetter
        builder.addMethod(MethodSpec.methodBuilder(interfaceScore.addressGetter())
                .addModifiers(Modifier.PUBLIC)
                .returns(Address.class)
                .addStatement("return this.$L", ADDRESS_MEMBER).build());

        //payableGetter
        builder.addMethod(MethodSpec.methodBuilder(interfaceScore.payableGetter())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(BigInteger.class, PAYABLE_VALUE_MEMBER).build())
                .returns(className)
                .addStatement("return new $L($L,$L)", className.simpleName(), ADDRESS_MEMBER, PAYABLE_VALUE_MEMBER)
                .build());
        builder.addMethod(MethodSpec.methodBuilder(interfaceScore.payableGetter())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(long.class, PAYABLE_VALUE_MEMBER).build())
                .returns(className)
                .addStatement("return this.$L($T.valueOf($L))", interfaceScore.payableGetter(), BigInteger.class, PAYABLE_VALUE_MEMBER)
                .build());
        //icxGetter
        builder.addMethod(MethodSpec.methodBuilder(interfaceScore.icxGetter())
                .addModifiers(Modifier.PUBLIC)
                .returns(BigInteger.class)
                .addStatement("return this.$L", PAYABLE_VALUE_MEMBER).build());

        List<MethodSpec> methods = overrideMethods(element);
        builder.addMethods(methods);
        return builder.build();
    }

    private List<MethodSpec> overrideMethods(TypeElement element) {
        List<MethodSpec> methods = new ArrayList<>();
        for (TypeMirror inf : element.getInterfaces()) {
            TypeElement infElement = util.getTypeElement(inf);
            List<MethodSpec> infMethods = overrideMethods(infElement);
            methods.addAll(infMethods);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                if (!ProcessorUtil.hasModifier(enclosedElement, Modifier.STATIC)) {
                    MethodSpec methodSpec = methodSpec((ExecutableElement) enclosedElement);
                    MethodSpec conflictMethod = ProcessorUtil.getConflictMethod(methods, methodSpec);
                    if (conflictMethod != null) {
                        methods.remove(conflictMethod);
                        util.warningMessage(
                                "Redeclare '%s %s(%s)' in %s",
                                conflictMethod.returnType.toString(),
                                conflictMethod.name,
                                ProcessorUtil.parameterSpecToString(conflictMethod.parameters),
                                element.getQualifiedName());
                    }
                    methods.add(methodSpec);
                }
            }
        }
        return methods;
    }

    private MethodSpec methodSpec(ExecutableElement element) {
        String methodName = element.getSimpleName().toString();
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder(methodName)
                .addAnnotation(Override.class);
        for (Modifier modifier : element.getModifiers()) {
            if (!modifier.equals(Modifier.ABSTRACT)) {
                builder.addModifiers(modifier);
            }
        }
        StringJoiner variables = new StringJoiner(", ");
        variables.add(String.format("this.%s", ADDRESS_MEMBER));
        variables.add(String.format("\"%s\"", methodName));
        for (VariableElement variableElement : element.getParameters()) {
            builder.addParameter(ParameterSpec.get(variableElement));
            variables.add(variableElement.getSimpleName().toString());
        }
        String callParameters = variables.toString();
        TypeName returnTypeName = TypeName.get(element.getReturnType());
        builder.returns(returnTypeName);

        Payable payable = element.getAnnotation(Payable.class);
        if (payable != null) {
            builder.addAnnotation(AnnotationSpec.get(payable));
            if (returnTypeName.equals(TypeName.VOID)) {
                builder.addCode(CodeBlock.builder()
                        .beginControlFlow("if (this.$L != null)", PAYABLE_VALUE_MEMBER)
                        .addStatement("$T.call(this.$L, $L)", Context.class, PAYABLE_VALUE_MEMBER, callParameters)
                        .nextControlFlow("else")
                        .addStatement("$T.call($L)", Context.class, callParameters)
                        .endControlFlow().build());
            } else {
                builder.addCode(CodeBlock.builder()
                        .beginControlFlow("if (this.$L != null)", PAYABLE_VALUE_MEMBER)
                        .addStatement("return $T.call($T.class, this.$L, $L)",
                                Context.class, element.getReturnType(), PAYABLE_VALUE_MEMBER, callParameters)
                        .nextControlFlow("else")
                        .addStatement("return $T.call($T.class, $L)", Context.class, element.getReturnType(), callParameters)
                        .endControlFlow().build());
            }
        } else {
            if (returnTypeName.equals(TypeName.VOID)) {
                builder.addCode(CodeBlock.builder()
                        .addStatement("$T.call($L)", Context.class, callParameters)
                        .build());
            } else {
                builder.addCode(CodeBlock.builder()
                        .addStatement("return $T.call($T.class, $L)", Context.class, element.getReturnType(), callParameters)
                        .build());
            }
        }
        return builder.build();
    }

}
