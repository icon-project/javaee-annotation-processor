package com.iconloop.score.annotation_processor;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.squareup.javapoet.*;
import score.Address;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class JsonObjectProcessor extends AbstractProcessor {
    private ProcessorUtil util;

    static final String PARAM_OBJECT = "obj";
    static final String LOCAL_JSON_OBJECT = "jsonObject";

    static final String DEFAULT_FORMAT_TO = "Json.value(%s)";
    static final String NULLCHECK_FORMAT = "%s == null ? Json.NULL : %s";

    private Map<TypeMirror, Format> formats;
    private List<TypeMirror> listTypes;

    class Format {
        private String parse;
        private String to;

        public Format(String parse, String to) {
            this.parse = parse;
            this.to = to;
        }

        public String getParse() {
            return parse;
        }

        public String getTo() {
            return to;
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        util = new ProcessorUtil(processingEnv, JsonObjectProcessor.class.getSimpleName());
        Elements elements = processingEnv.getElementUtils();
        listTypes = new ArrayList<>();
        listTypes.add(elements.getTypeElement(List.class.getName()).asType());
        listTypes.add(elements.getTypeElement(scorex.util.ArrayList.class.getName()).asType());

        formats = new HashMap<>();
        formats.put(elements.getTypeElement(Boolean.class.getName()).asType(),
                new Format("%s.asBoolean()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Character.class.getName()).asType(),
                new Format("%s.asString().charAt(0)", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Byte.class.getName()).asType(),
                new Format("(byte)%s.asInt()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Short.class.getName()).asType(),
                new Format("(short)%s.asInt()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Integer.class.getName()).asType(),
                new Format("%s.asInt()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Long.class.getName()).asType(),
                new Format("%s.asLong()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Float.class.getName()).asType(),
                new Format("%s.asFloat()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(Double.class.getName()).asType(),
                new Format("%s.asDouble()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(String.class.getName()).asType(),
                new Format("%s.asString()", DEFAULT_FORMAT_TO));
        formats.put(elements.getTypeElement(BigInteger.class.getName()).asType(),
                new Format("new BigInteger(%s.asString())", "Json.value(%s.toString())"));
        formats.put(elements.getTypeElement(Address.class.getName()).asType(),
                new Format("Address.fromString(%s.asString())", "Json.value(%s.toString())"));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add(JsonObject.class.getCanonicalName());
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
                if (element.getKind().isClass()) {
                    util.noteMessage("%s", element.toString());
                    generateExtendsClass(processingEnv.getFiler(), (TypeElement) element);
                    ret = true;
                } else {
                    throw new RuntimeException("not support");
                }
            }
        }
        return ret;
    }

    private void generateExtendsClass(Filer filer, TypeElement element) {
        ClassName parentClassName = ClassName.get(element);
        TypeSpec typeSpec = typeSpec(element);
        JavaFile javaFile = JavaFile.builder(parentClassName.packageName(), typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getParseStatement(TypeMirror variableType, String jsonValue, JsonProperty annProperty) {
        if (annProperty != null && !annProperty.parser().isEmpty()) {
            return String.format("%s(%s.asObject())", annProperty.parser(), jsonValue);
        } else {
            Map.Entry<TypeMirror, Format> entry = getFormat(variableType);
            if (entry != null) {
                return String.format(entry.getValue().getParse(), jsonValue);
            } else {
                AnnotatedTypeElement<JsonObject> annotated = util.getAnnotatedTypeElement(variableType, JsonObject.class);
                if (annotated != null) {
                    TypeElement fieldElement = annotated.getElement();
                    JsonObject annFieldClass = annotated.getAnnotation();
                    ClassName fieldClassName = ClassName.get(ClassName.get(fieldElement).packageName(), fieldElement.getSimpleName() + annFieldClass.suffix());
                    return String.format("%s.%s(%s.asObject())", fieldClassName.toString(), annFieldClass.parse(), jsonValue);
                } else {
                    throw new RuntimeException(String.format("%s class is not JsonObject convertible, refer %s", variableType, jsonValue));
                }
            }
        }
    }

    private String getToJsonStatement(TypeMirror variableType, String variableName, JsonProperty annProperty) {
        if (annProperty != null && !annProperty.toJson().isEmpty()) {
            return String.format("%s(%s)", annProperty.toJson(), variableName);
        } else {
            Map.Entry<TypeMirror, Format> entry = getFormat(variableType);
            if (entry != null) {
                String toJson = String.format(entry.getValue().getTo(), variableName);
                if (!variableType.getKind().isPrimitive()) {
                    toJson = String.format(NULLCHECK_FORMAT, variableName, toJson);
                }
                return toJson;
            } else {
                AnnotatedTypeElement<JsonObject> annotated = util.getAnnotatedTypeElement(variableType, JsonObject.class);
                if (annotated != null) {
                    TypeElement fieldElement = annotated.getElement();
                    JsonObject annFieldClass = annotated.getAnnotation();
                    ClassName fieldClassName = ClassName.get(ClassName.get(fieldElement).packageName(), fieldElement.getSimpleName() + annFieldClass.suffix());
                    String toJson = String.format("%s.%s(%s)",
                            fieldClassName.toString(),
                            annFieldClass.toJsonObject(),
                            variableName);
                    return String.format(NULLCHECK_FORMAT, variableName, toJson);
                } else {
                    throw new RuntimeException(String.format("%s class is not JsonObject convertible, refer %s", variableType, variableName));
                }
            }
        }
    }

    private Map.Entry<TypeMirror, Format> getFormat(TypeMirror variableType) {
        for (Map.Entry<TypeMirror, Format> entry : formats.entrySet()) {
            if (util.isAssignable(variableType, entry.getKey())) {
                return entry;
            }
        }
        return null;
    }

    private TypeSpec typeSpec(TypeElement element) {
        ClassName parentClassName = ClassName.get(element);
        JsonObject annClass = element.getAnnotation(JsonObject.class);
        ClassName className = ClassName.get(parentClassName.packageName(), parentClassName.simpleName() + annClass.suffix());
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(ClassName.get(parentClassName.packageName(), className.simpleName()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(element.asType());

        builder.addMethod(MethodSpec.constructorBuilder()
                .addStatement("super()")
                .build());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .addStatement("super()");

        builder.addMethod(MethodSpec.methodBuilder(annClass.parse())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "jsonString")
                .returns(className)
                .addStatement("$T jsonValue = $T.parse(jsonString)", JsonValue.class, Json.class)
                .beginControlFlow("if (jsonValue.isNull())")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return $L.$L(jsonValue.asObject())",
                        className.simpleName(),
                        annClass.parse())
                .build());
        MethodSpec.Builder parseMethod = MethodSpec.methodBuilder(annClass.parse())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(com.eclipsesource.json.JsonObject.class, LOCAL_JSON_OBJECT)
                .returns(className)
                .addStatement("$L obj = new $L()", className.simpleName(), className.simpleName());
        builder.addMethod(MethodSpec.methodBuilder(annClass.toJsonObject())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .returns(JsonValue.class)
                .beginControlFlow("if ($L == null)", PARAM_OBJECT)
                .addStatement("return $T.NULL", JsonValue.class)
                .endControlFlow()
                .addStatement("return new $L($L).$L()", className.simpleName(), PARAM_OBJECT, annClass.toJsonObject())
                .build());
        MethodSpec.Builder toJsonMethod = MethodSpec.methodBuilder(annClass.toJsonObject())
                .addModifiers(Modifier.PUBLIC)
                .returns(com.eclipsesource.json.JsonObject.class)
                .addStatement("$T $L = $T.object()",
                        com.eclipsesource.json.JsonObject.class,
                        LOCAL_JSON_OBJECT,
                        com.eclipsesource.json.Json.class);

        processMethod(element, constructor, parseMethod, toJsonMethod);

        builder.addMethod(constructor.build());
        builder.addMethod(parseMethod
                .addStatement("return obj")
                .build());
        builder.addMethod(toJsonMethod
                .addStatement("return $L", LOCAL_JSON_OBJECT)
                .build());
        return builder.build();
    }

    private void processMethod(
            TypeElement element,
            MethodSpec.Builder constructor,
            MethodSpec.Builder parseMethod, MethodSpec.Builder toJsonMethod) {
        TypeMirror superClass = element.getSuperclass();
        TypeElement superElement = util.getTypeElement(superClass);
        if (superElement != null) {
            processMethod(superElement, constructor, parseMethod, toJsonMethod);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.FIELD) &&
                    !ProcessorUtil.hasModifier(enclosedElement, Modifier.STATIC)) {
                VariableElement variableElement = (VariableElement) enclosedElement;
                JsonProperty annField = variableElement.getAnnotation(JsonProperty.class);
                if (annField != null && annField.ignore()) {
                    continue;
                }

                TypeMirror fieldType = variableElement.asType();
                String field = variableElement.getSimpleName().toString();
                String property = field;
                String capitalized = field.substring(0, 1).toUpperCase() + field.substring(1);
                String getter = (fieldType.getKind() == TypeKind.BOOLEAN ? "is" : "get") + capitalized;
                String setter = "set" + capitalized;

                boolean direct = false;
                if (annField != null) {
                    direct = annField.direct();
                    if (!annField.value().isEmpty()) {
                        property = annField.value();
                    }
                    if (!annField.getter().isEmpty()) {
                        getter = annField.getter();
                    }
                    if (!annField.setter().isEmpty()) {
                        setter = annField.setter();
                    }
                }

                if (direct) {
                    constructor.addStatement("this.$L = $L.$L", field, PARAM_OBJECT, field);
                    toJsonMethod.addStatement("$T $L = this.$L", fieldType, field, field);
                } else {
                    constructor.addStatement("this.$L($L.$L())", setter, PARAM_OBJECT, getter);
                    toJsonMethod.addStatement("$T $L = this.$L()", fieldType, field, getter);
                }

                String jsonValue = field + "JsonValue";
                parseMethod.addStatement("$T $L = $L.get(\"$L\")", JsonValue.class, jsonValue, LOCAL_JSON_OBJECT, property);
                parseMethod.beginControlFlow("if ($L != null && !$L.isNull())", jsonValue, jsonValue);

                String setterValue;
                boolean isList = util.containsDeclaredType(listTypes, fieldType);
                if (isList || fieldType.getKind() == TypeKind.ARRAY) {
                    TypeMirror componentType;
                    if (isList) {
                        componentType = ((DeclaredType) fieldType).getTypeArguments().get(0);
                    } else {
                        componentType = ((ArrayType) fieldType).getComponentType();
                    }
                    String jsonArrayName = field + "JsonArray";
                    toJsonMethod
                            .addStatement("$T $L = $T.array()", JsonArray.class, jsonArrayName, com.eclipsesource.json.Json.class)
                            .beginControlFlow("for($T v : $L)", componentType, field)
                            .addStatement("$L.add($L)", jsonArrayName, getToJsonStatement(componentType, "v", annField))
                            .endControlFlow();

                    parseMethod.addStatement("$T $L = $L.asArray()", JsonArray.class, jsonArrayName, jsonValue);
                    if (isList) {
                        parseMethod.addStatement("$T $L = new $T<>()", fieldType, field, scorex.util.ArrayList.class);
                    } else {
                        parseMethod.addStatement("$T[] $L = new $T[$L.size()]", componentType, field, componentType, jsonArrayName);
                    }

                    parseMethod.beginControlFlow("for(int i=0; i<$L.size(); i++)", jsonArrayName);
                    setterValue = getParseStatement(componentType, jsonArrayName + ".get(i)", annField);
                    if (isList) {
                        parseMethod.addStatement("$L.add($L)", field, setterValue);
                    } else {
                        parseMethod.addStatement("$L[i] = $L", field, setterValue);
                    }
                    parseMethod.endControlFlow();
                    setterValue = field;
                    jsonValue = jsonArrayName;
                } else {
                    setterValue = getParseStatement(fieldType, jsonValue, annField);
                    toJsonMethod.addStatement("$T $L = $L",
                            JsonValue.class, jsonValue, getToJsonStatement(fieldType, field, annField));
                }

                toJsonMethod.addStatement("$L.add(\"$L\", $L)", LOCAL_JSON_OBJECT, property, jsonValue);
                if (direct) {
                    parseMethod.addStatement("obj.$L = $L", field, setterValue);
                } else {
                    parseMethod.addStatement("obj.$L($L)", setter, setterValue);
                }
                parseMethod.endControlFlow();
            }
        }
    }
}
