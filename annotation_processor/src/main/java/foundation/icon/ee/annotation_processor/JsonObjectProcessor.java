package foundation.icon.ee.annotation_processor;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import com.squareup.javapoet.*;

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
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

public class JsonObjectProcessor extends AbstractProcessor {
    static final String MESSAGE_PREFIX = "[" + JsonObjectProcessor.class.getSimpleName() + "]";

    static final String PARAM_OBJECT = "obj";
    static final String LOCAL_JSON_OBJECT = "jsonObject";

    private Map<TypeMirror, String> jsonConvertibles;
    private List<TypeMirror> listTypes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Elements elements = processingEnv.getElementUtils();
        listTypes = new ArrayList<>();
        listTypes.add(elements.getTypeElement(List.class.getName()).asType());
        listTypes.add(elements.getTypeElement(ArrayList.class.getName()).asType());

        jsonConvertibles = new HashMap<>();
        jsonConvertibles.put(elements.getTypeElement(Boolean.class.getName()).asType(), "%s.asBoolean()");
        jsonConvertibles.put(elements.getTypeElement(Character.class.getName()).asType(), "%s.asString().charAt(0)");
        jsonConvertibles.put(elements.getTypeElement(Byte.class.getName()).asType(), "(byte)%s.asInt()");
        jsonConvertibles.put(elements.getTypeElement(Short.class.getName()).asType(), "(short)%s.asInt()");
        jsonConvertibles.put(elements.getTypeElement(Integer.class.getName()).asType(), "%s.asInt()");
        jsonConvertibles.put(elements.getTypeElement(Long.class.getName()).asType(), "%s.asLong()");
        jsonConvertibles.put(elements.getTypeElement(Float.class.getName()).asType(), "%s.asFloat()");
        jsonConvertibles.put(elements.getTypeElement(Double.class.getName()).asType(), "%s.asDouble()");
        jsonConvertibles.put(elements.getTypeElement(String.class.getName()).asType(), "%s.asString()");
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
                    printMessage(Diagnostic.Kind.NOTE, "%s", element.toString());
                    generateExtendsClass(processingEnv.getFiler(), (TypeElement) element);
                    ret = true;
                } else {
                    throw new RuntimeException("not support");
                }
            }
        }
        return ret;
    }

    private void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        processingEnv.getMessager().printMessage(
                kind, String.format(MESSAGE_PREFIX + format, args));
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
        if (annProperty != null && !annProperty.parser().isEmpty()){
            return String.format("%s(%s.asObject())", annProperty.parser(), jsonValue);
        } else {
            TypeMirror convertible = getJsonConvertible(variableType);
            if (convertible != null) {
                return String.format(jsonConvertibles.get(convertible), jsonValue);
            } else {
                TypeElement fieldElement = (TypeElement)processingEnv.getTypeUtils().asElement(variableType);
                JsonObject annFieldClass = fieldElement.getAnnotation(JsonObject.class);
                if (annFieldClass != null) {
                    ClassName fieldClassName = ClassName.get(ClassName.get(fieldElement).packageName(), fieldElement.getSimpleName() + annFieldClass.suffix());
                    return String.format("%s.%s(%s.asObject())",fieldClassName.toString(), annFieldClass.parse(), jsonValue);
                } else {
                    throw new RuntimeException(String.format("%s class is not JsonObject convertible, refer %s", variableType, jsonValue));
                }
            }
        }
    }

    private String getToJsonStatement(TypeMirror variableType, String variableName, JsonProperty annProperty) {
        if (annProperty != null && !annProperty.toJson().isEmpty()){
            return String.format("%s(%s)", annProperty.toJson(), variableName);
        } else {
            TypeMirror convertible = getJsonConvertible(variableType);
            if (convertible != null) {
                return String.format("%s.value(%s)",Json.class.getSimpleName(), variableName);
            } else {
                TypeElement fieldElement = (TypeElement)processingEnv.getTypeUtils().asElement(variableType);
                JsonObject annFieldClass = fieldElement.getAnnotation(JsonObject.class);
                if (annFieldClass != null) {
                    ClassName fieldClassName = ClassName.get(ClassName.get(fieldElement).packageName(), fieldElement.getSimpleName() + annFieldClass.suffix());
                    return String.format("%s.%s(%s)",
                            fieldClassName.toString(),
                            annFieldClass.toJsonObject(),
                            variableName);
                } else {
                    throw new RuntimeException(String.format("%s class is not JsonObject convertible, refer %s", variableType, variableName));
                }
            }
        }
    }

    private TypeMirror getJsonConvertible(TypeMirror variableType) {
        for (TypeMirror key : jsonConvertibles.keySet()) {
            if (processingEnv.getTypeUtils().isAssignable(variableType, key)) {
                return key;
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
                .returns(TypeName.get(element.asType()))
                .addStatement("return $L.$L($T.parse(jsonString).asObject())",
                        className.simpleName(),
                        annClass.parse(),
                        com.eclipsesource.json.Json.class)
                .build());
        MethodSpec.Builder fromJsonMethod = MethodSpec.methodBuilder(annClass.parse())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(com.eclipsesource.json.JsonObject.class, LOCAL_JSON_OBJECT)
                .returns(TypeName.get(element.asType()))
                .addStatement("$T obj = new $T()", element.asType(), element.asType());
        builder.addMethod(MethodSpec.methodBuilder(annClass.toJsonObject())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .returns(com.eclipsesource.json.JsonObject.class)
                .addStatement("return new $L($L).$L()", className.simpleName(), PARAM_OBJECT, annClass.toJsonObject())
                .build());
        MethodSpec.Builder toJsonMethod = MethodSpec.methodBuilder(annClass.toJsonObject())
                .addModifiers(Modifier.PUBLIC)
                .returns(com.eclipsesource.json.JsonObject.class)
                .addStatement("$T $L = $T.object()",
                        com.eclipsesource.json.JsonObject.class,
                        LOCAL_JSON_OBJECT,
                        com.eclipsesource.json.Json.class);

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.FIELD) &&
                    !Util.hasModifier(enclosedElement, Modifier.STATIC)) {
                VariableElement variableElement = (VariableElement) enclosedElement;
                JsonProperty annField = variableElement.getAnnotation(JsonProperty.class);
                if (annField != null && annField.ignore()) {
                    continue;
                }

                TypeMirror variableType = variableElement.asType();
                String field = variableElement.getSimpleName().toString();
                String property = field;
                String capitalized = field.substring(0, 1).toUpperCase() + field.substring(1);
                String getter = "get" + capitalized;
                String setter = "set" + capitalized;
                String setterValue;
                boolean direct = false;

                String jsonValue = field + "JsonValue";
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
                    toJsonMethod.addStatement("$T $L = this.$L", variableType, field, field);
                } else {
                    constructor.addStatement("this.$L($L.$L())", setter, PARAM_OBJECT, getter);
                    toJsonMethod.addStatement("$T $L = this.$L()", variableType, field, getter);
                }

                fromJsonMethod.addStatement("$T $L = $L.get(\"$L\")", JsonValue.class, jsonValue, LOCAL_JSON_OBJECT, property);
                fromJsonMethod.beginControlFlow("if ($L != null)", jsonValue);

                boolean isList = isListType(variableType);
                if (isList || variableType.getKind() == TypeKind.ARRAY) {
                    TypeMirror componentType;
                    if (isList) {
                        componentType = ((DeclaredType)variableType).getTypeArguments().get(0);
                    } else {
                        componentType = ((ArrayType)variableType).getComponentType();
                    }
                    String jsonArrayName = field + "JsonArray";
                    toJsonMethod
                            .addStatement("$T $L = $T.array()", JsonArray.class, jsonArrayName, com.eclipsesource.json.Json.class)
                            .beginControlFlow("for($T v : $L)", componentType, field)
                            .addStatement("$L.add($L)", jsonArrayName, getToJsonStatement(componentType, "v", annField))
                            .endControlFlow();

                    fromJsonMethod.addStatement("$T $L = $L.asArray()", JsonArray.class, jsonArrayName, jsonValue);
                    if (isList) {
                        fromJsonMethod.addStatement("$T $L = new $T<>()", variableType, field, ArrayList.class);
                    } else {
                        fromJsonMethod.addStatement("$T[] $L = new $T[$L.size()]", componentType, field, componentType, jsonArrayName);
                    }

                    fromJsonMethod.beginControlFlow("for(int i=0; i<$L.size(); i++)", jsonArrayName);
                    setterValue = getParseStatement(componentType, jsonArrayName + ".get(i)", annField);
                    if (isList){
                        fromJsonMethod.addStatement("$L.add($L)",field, setterValue);
                    } else {
                        fromJsonMethod.addStatement("$L[i] = $L",field, setterValue);
                    }
                    fromJsonMethod.endControlFlow();
                    setterValue = field;
                    jsonValue = jsonArrayName;
                } else {
                    setterValue = getParseStatement(variableType, jsonValue, annField);
                    toJsonMethod.addStatement("$T $L = $L",
                            JsonValue.class, jsonValue, getToJsonStatement(variableType, field, annField));
                }

                toJsonMethod.addStatement("$L.add(\"$L\", $L)", LOCAL_JSON_OBJECT, property, jsonValue);
                if (direct) {
                    fromJsonMethod.addStatement("obj.$L = $L", field, setterValue);
                } else {
                    fromJsonMethod.addStatement("obj.$L($L)", setter, setterValue);
                }
                fromJsonMethod.endControlFlow();
            }
        }

        builder.addMethod(constructor.build());
        builder.addMethod(fromJsonMethod
                .addStatement("return obj")
                .build());
        builder.addMethod(toJsonMethod
                .addStatement("return $L", LOCAL_JSON_OBJECT)
                .build());
        return builder.build();
    }

    private boolean isListType(TypeMirror variableType) {
        if (variableType.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement)((DeclaredType) variableType).asElement();
            TypeMirror varType = element.asType();
            for (TypeMirror listType : listTypes) {
                if (processingEnv.getTypeUtils().isSameType(varType, listType)) {
                    return true;
                }
            }
        }
        return false;
    }
}
