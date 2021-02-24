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

package foundation.icon.ee.annotation_processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public class Util {

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
}
