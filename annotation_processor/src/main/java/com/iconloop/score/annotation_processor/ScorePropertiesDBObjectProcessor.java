package com.iconloop.score.annotation_processor;

import com.iconloop.score.lib.BytesCodec;
import com.iconloop.score.lib.Immutables;
import com.iconloop.score.lib.PropertiesDB;
import com.iconloop.score.lib.ProxyDictDB;
import com.squareup.javapoet.*;
import score.*;
import scorex.util.ArrayList;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.io.IOException;
import java.util.*;

public class ScorePropertiesDBObjectProcessor extends AbstractProcessor {
    static final boolean ENABLE_SUPPORT_ARRAY = true;
    private ProcessorUtil util;

    static final String METHOD_REQUIRE_INITIALIZED = "requireInitialized";
    static final String METHOD_REQUIRE_NOT_INITIALIZED = "requireNotInitialized";
    static final String FIELD_DB = "db";
    static final String METHOD_INITIALIZE = "initialize";
    static final String PARAM_ID = "id";
    static final String METHOD_VALUE_AS_SETTER = "value";
    static final String PARAM_OBJECT = "obj";
    static final String METHOD_CLOSE = "close";
    static final String METHOD_FLUSH = "flush";
    static final String METHOD_VALUE_AS_GETTER = "value";
    static final String METHOD_TO_MAP = "toMap";

    static final String LOCAL_ENTRIES = "entries";

    static final String METHOD_READ = "readObject";
    static final String PARAM_READER = "reader";
    static final String METHOD_WRITE = "writeObject";
    static final String PARAM_WRITER = "writer";

    private Map<Class<?>, TypeMirror> bytesCodecSupportedTypes;
    private List<TypeMirror> listTypes;
    private Map<TypeMirror, String> dbConstructors;
    private TypeMirror bytesType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        util = new ProcessorUtil(processingEnv, ScorePropertiesDBObjectProcessor.class.getSimpleName());
        bytesType = util.getTypeMirror(byte[].class);
        bytesCodecSupportedTypes = new HashMap<>();
        for (Class<?> clazz : BytesCodec.predefinedCodecs.keySet()) {
            bytesCodecSupportedTypes.put(clazz, util.getTypeMirror(clazz));
        }
        listTypes = new java.util.ArrayList<>();
        listTypes.add(util.getTypeMirror(List.class));
        listTypes.add(util.getTypeMirror(scorex.util.ArrayList.class));

        dbConstructors = new java.util.HashMap<>();
        dbConstructors.put(util.getTypeMirror(VarDB.class), "newVarDB");
        dbConstructors.put(util.getTypeMirror(ArrayDB.class), "newArrayDB");
        dbConstructors.put(util.getTypeMirror(DictDB.class), "newDictDB");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add(ScorePropertiesDBObject.class.getCanonicalName());
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

    private static ClassName getScorePropertiesDBObjectClassName(AnnotatedTypeElement<ScorePropertiesDBObject> annotated) {
        TypeElement element = annotated.getElement();
        ScorePropertiesDBObject ann = annotated.getAnnotation();
        return ClassName.get(
                ClassName.get(element).packageName(),
                element.getSimpleName() + ann.suffix());
    }

    private TypeSpec typeSpec(TypeElement element) {
        ClassName parentClassName = ClassName.get(element);
        ScorePropertiesDBObject annClass = element.getAnnotation(ScorePropertiesDBObject.class);
        ClassName className = ClassName.get(parentClassName.packageName(), parentClassName.simpleName() + annClass.suffix());
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(ClassName.get(parentClassName.packageName(), className.simpleName()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(element.asType())
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(PropertiesDB.class), TypeName.get(element.asType())));

        builder.addMethod(MethodSpec.methodBuilder(METHOD_READ)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ObjectReader.class, PARAM_READER)
                .returns(className)
                .addStatement("$T $L = new $T()", className, PARAM_OBJECT, className)
                .addStatement("$L.$L($T.$L($L))",
                        PARAM_OBJECT, METHOD_INITIALIZE, PropertiesDB.class, METHOD_READ, PARAM_READER)
                .addStatement("return $L", PARAM_OBJECT)
                .build());
        builder.addMethod(MethodSpec.methodBuilder(METHOD_WRITE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ObjectWriter.class, PARAM_WRITER)
                .addParameter(className, PARAM_OBJECT)
                .addStatement("$T.$L($L, $L)", PropertiesDB.class, METHOD_WRITE, PARAM_WRITER, PARAM_OBJECT)
                .build());

        builder.addField(
                ParameterizedTypeName.get(
                        ProxyDictDB.class,
                        String.class,
                        byte[].class),
                FIELD_DB,
                Modifier.PRIVATE);

//        builder.addMethod(MethodSpec.constructorBuilder()
//                .addStatement("super()")
//                .build());
//        builder.addMethod(MethodSpec.constructorBuilder()
//                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
//                .addStatement("super()")
//                .addStatement("$L($L)", METHOD_OVERWRITE, PARAM_OBJECT)
//                .build());

        builder.addMethod(MethodSpec.methodBuilder("id")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB)
                .addStatement("return $L.id()", FIELD_DB)
                .build());

        builder.addMethod(MethodSpec.methodBuilder(METHOD_INITIALIZE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, PARAM_ID)
                .addStatement("$L($L)", METHOD_REQUIRE_NOT_INITIALIZED, FIELD_DB)
                .addStatement("$L = new $T<>($L, $T.class)",
                        FIELD_DB,
                        ProxyDictDB.class,
                        PARAM_ID,
                        byte[].class)
                .build());

        MethodSpec.Builder overwriteMethod = MethodSpec.methodBuilder(METHOD_VALUE_AS_SETTER)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB);

        MethodSpec.Builder closeMethod = MethodSpec.methodBuilder(METHOD_CLOSE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB);

        MethodSpec.Builder flushMethod = MethodSpec.methodBuilder(METHOD_FLUSH)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB);

        MethodSpec.Builder valueMethod = MethodSpec.methodBuilder(METHOD_VALUE_AS_GETTER)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(parentClassName)
                .addStatement("$T $L = new $T()",
                        parentClassName, PARAM_OBJECT, parentClassName);

        MethodSpec.Builder toMapMethod = MethodSpec.methodBuilder(METHOD_TO_MAP)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Map.class, String.class, Object.class))
                .addStatement("$T<$T<$T,$T>> $L = new $T<>()",
                        List.class, Map.Entry.class, String.class, Object.class, LOCAL_ENTRIES, ArrayList.class);

        CodeBlock.Builder overwrite = CodeBlock.builder();
        CodeBlock.Builder overwriteNull = CodeBlock.builder();

        processMethod(element, overwriteNull, overwrite, closeMethod, flushMethod, valueMethod, toMapMethod, builder);

        builder.addMethod(overwriteMethod
                .beginControlFlow("if ($L == null)", PARAM_OBJECT)
                .addCode(overwriteNull.build())
                .nextControlFlow("else")
                .addCode(overwrite.build())
                .endControlFlow()
                .build());
        builder.addMethod(closeMethod.addStatement("$L.$L()", FIELD_DB, METHOD_CLOSE).build());
        builder.addMethod(flushMethod.addStatement("$L.$L()", FIELD_DB, METHOD_FLUSH).build());
        builder.addMethod(valueMethod.addStatement("return $L", PARAM_OBJECT).build());
        builder.addMethod(toMapMethod
                .addStatement("return $T.ofEntries($L)", Immutables.class, LOCAL_ENTRIES)
                .build());
        return builder.build();
    }

    private void processMethod(
            TypeElement element,
            CodeBlock.Builder overwriteNull,
            CodeBlock.Builder overwrite,
            MethodSpec.Builder closeMethod,
            MethodSpec.Builder flushMethod,
            MethodSpec.Builder valueMethod,
            MethodSpec.Builder toMapMethod,
            TypeSpec.Builder builder) {
        TypeMirror superClass = element.getSuperclass();
        TypeElement superElement = util.getTypeElement(superClass);
        if (superElement != null) {
            processMethod(superElement, overwriteNull, overwrite, closeMethod, flushMethod, valueMethod, toMapMethod, builder);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.FIELD) &&
                    !ProcessorUtil.hasModifier(enclosedElement, Modifier.STATIC)) {
                VariableElement variableElement = (VariableElement) enclosedElement;
                TypeMirror fieldType = variableElement.asType();
                ScorePropertiesDBProperty annField = variableElement.getAnnotation(ScorePropertiesDBProperty.class);
                if (annField != null && annField.ignore()) {
                    continue;
                }

                String field = variableElement.getSimpleName().toString();
                String key = field;
                String capitalized = field.substring(0, 1).toUpperCase() + field.substring(1);
                String getter = (fieldType.getKind() == TypeKind.BOOLEAN ? "is" : "get") + capitalized;
                String setter = "set" + capitalized;
                TypeMirror dbClass = null;
                if (annField != null) {
                    if (!annField.value().isEmpty()) {
                        key = annField.value();
                    }
                    if (!annField.getter().isEmpty()) {
                        getter = annField.getter();
                    }
                    if (!annField.setter().isEmpty()) {
                        setter = annField.setter();
                    }
                    TypeMirror t = util.getTypeMirrorFromAnnotation(annField::db);
                    if (!PropertiesDB.class.getName().equals(t.toString())) {
                        dbClass = t;
                    }
                }
                if (fieldType.getKind().isPrimitive()) {
                    overwriteNull.addStatement("$L.set(\"$L\", null)", FIELD_DB, key);
                } else {
                    overwriteNull.addStatement("$L(null)", setter);
                }
                overwrite.addStatement("$L($L.$L())", setter, PARAM_OBJECT, getter);

                TypeName fieldClassName = ClassName.get(fieldType);
                CodeBlock.Builder getterCodeBlock = CodeBlock.builder();
                CodeBlock.Builder setterCodeBlock = CodeBlock.builder();
                if (util.containsDeclaredType(dbConstructors.keySet(), fieldType)) {
                    List<? extends TypeMirror> types = ((DeclaredType) fieldType).getTypeArguments();
                    TypeMirror componentType = types.get(types.size()-1);
                    String dbConstructor = util.getDeclaredType(dbConstructors, fieldType);
                    closeMethod.addStatement("super.$L(null)", setter);
                    valueMethod.addStatement("$L.$L($L())", PARAM_OBJECT, setter, getter);
                    //toMapMethod
                    ////VarDB : componentType
                    ////ArrayDB : componentType[]
                    ////DictDB : not support
                    getterCodeBlock
                            .addStatement("$T $L = super.$L()",fieldType, field, getter)
                            .beginControlFlow("if ($L == null)", field)
                            .addStatement("$L = $T.$L($L.concatID(\"$L\"),$T.class)",
                                    field, Context.class, dbConstructor, FIELD_DB, key, componentType)
                            .addStatement("super.$L($L)", setter, field)
                            .endControlFlow()
                            .addStatement("return $L", field);

                    setterCodeBlock.addStatement("super.$L($L)",setter, field);
                } else if (isBytesCodecSupported(fieldType)) {
                    String localKey = "key";
                    TypeMirror codecType = fieldType;
                    String setterVal = field;
                    String defaultVal = util.getDefaultValueAsString(fieldType);
                    if (fieldType.getKind().isPrimitive()) {
                        codecType = util.getBoxedType(fieldType);
                        setterVal = String.format("%s != null ? %s : %s",
                                field, field, defaultVal);
                    }
                    closeMethod.addStatement("super.$L($L)", setter, defaultVal);

                    valueMethod.addStatement("$L.$L($L())", PARAM_OBJECT, setter, getter);
                    toMapMethod.addStatement("$L.add($T.entry(\"$L\", $L()))", LOCAL_ENTRIES, Immutables.class, key, getter);

                    if (util.isSameType(fieldType, bytesType)) {
                        getterCodeBlock
                                .addStatement("$T $L = \"$L\"", String.class, localKey, key)
                                .beginControlFlow(" if (!$L.isModified($L) && !$L.isLoaded($L))",
                                        FIELD_DB, localKey, FIELD_DB, localKey)
                                .addStatement("$T $L = $L.get($L)",
                                        codecType, field, FIELD_DB, localKey)
                                .addStatement("super.$L($L)", setter, setterVal)
                                .endControlFlow()
                                .addStatement("return super.$L()", getter);
                        setterCodeBlock
                                .addStatement("$L.set(\"$L\", $L)",
                                        FIELD_DB, key, field)
                                .addStatement("super.$L($L)", setter, field);
                    } else {
                        getterCodeBlock
                                .addStatement("$T $L = \"$L\"", String.class, localKey, key)
                                .beginControlFlow(" if (!$L.isModified($L) && !$L.isLoaded($L))",
                                        FIELD_DB, localKey, FIELD_DB, localKey)
                                .addStatement("$T $L = $T.resolve($T.class).decode($L.get($L))",
                                        codecType, field, BytesCodec.class, codecType, FIELD_DB, localKey)
                                .addStatement("super.$L($L)", setter, setterVal)
                                .endControlFlow()
                                .addStatement("return super.$L()", getter);
                        setterCodeBlock
                                .addStatement("$L.set(\"$L\", $T.resolve($T.class).encode($L))",
                                        FIELD_DB, key, BytesCodec.class, codecType, field)
                                .addStatement("super.$L($L)", setter, field);
                    }

                } else if ((fieldType.getKind() == TypeKind.ARRAY || util.containsDeclaredType(listTypes, fieldType))){
                    if (!ENABLE_SUPPORT_ARRAY) {
                        throw new RuntimeException(String.format("%s class is not ScorePropertiesDBObject convertible", fieldType));
                    }
                    String localKey = "key";
                    String localArrayDB = "arrayDB";
                    TypeMirror componentType;
                    String sizeGetter;
                    String componentGetter;
                    String componentAdder;
                    CodeBlock fieldConstructor;
                    if (fieldType.getKind() == TypeKind.ARRAY ) {
                        componentType = ((ArrayType) fieldType).getComponentType();
                        sizeGetter = "length";
                        componentGetter = "[i]";
                        componentAdder = String.format("%s[i] = %s.get(i)", field, localArrayDB);
                        fieldConstructor = CodeBlock.builder().addStatement("$T $L = new $T[$L.size()]",
                                fieldType, field, componentType,localArrayDB).build();
                    } else {
                        componentType = ((DeclaredType) fieldType).getTypeArguments().get(0);
                        sizeGetter = "size()";
                        componentGetter = ".get(i)";
                        componentAdder = String.format("%s.add(%s.get(i))", field, localArrayDB);
                        fieldConstructor = CodeBlock.builder().addStatement("$T $L = new $T()",
                                fieldType, field, ArrayList.class).build();
                    }
                    TypeMirror arrayDBValueType = componentType;
                    if (arrayDBValueType.getKind().isPrimitive()) {
                        arrayDBValueType = util.getBoxedType(arrayDBValueType);
                    }
                    closeMethod.addStatement("super.$L(null)", setter);

                    CodeBlock arrayDBCode = CodeBlock.builder()
                            .addStatement("$T<$T> $L = $T.newArrayDB($L.concatID(\"$L\"),$T.class)",
                                    ArrayDB.class, arrayDBValueType, localArrayDB, Context.class, FIELD_DB, key, arrayDBValueType)
                            .build();
                    flushMethod
                            .addStatement("$T $L = super.$L()", fieldType, field, getter)
                            .beginControlFlow("if ($L != null)", field)
                                .addCode(arrayDBCode)
                                .beginControlFlow("for (int i = 0; i < $L.$L; i++)", field, sizeGetter)
                                    .beginControlFlow("if (i < $L.size())", localArrayDB)
                                        .addStatement("$L.set(i, $L$L)", localArrayDB, field, componentGetter)
                                    .nextControlFlow("else")
                                        .addStatement("$L.add($L$L)", localArrayDB, field, componentGetter)
                                    .endControlFlow()
                                .endControlFlow()
                                .beginControlFlow("for (int i = 0; i < $L.$L; i++)", field, sizeGetter)
                                    .addStatement("$L.pop()", localArrayDB)
                                .endControlFlow()
                            .nextControlFlow("else if ($L.isModified(\"$L\"))", FIELD_DB, key)
                                .addCode(arrayDBCode)
                                .beginControlFlow("for (int i = 0; i < $L.size(); i++)", localArrayDB)
                                    .addStatement("$L.pop()", localArrayDB)
                                .endControlFlow()
                            .endControlFlow();

                    valueMethod.addStatement("$L.$L($L())", PARAM_OBJECT, setter, getter);
                    toMapMethod.addStatement("$L.add($T.entry(\"$L\", $L()))", LOCAL_ENTRIES, Immutables.class, key, getter);

                    getterCodeBlock
                            .addStatement("$T $L = \"$L\"", String.class, localKey, key)
                            .beginControlFlow(" if (!$L.isModified($L) && !$L.isLoaded($L))",
                                    FIELD_DB, localKey, FIELD_DB, localKey)
                            .add(arrayDBCode)
                            .add(fieldConstructor)
                                .beginControlFlow("for (int i = 0; i < $L.size(); i++)", localArrayDB)
                                .addStatement("$L", componentAdder)
                                .endControlFlow()
                            .addStatement("super.$L($L)", setter, field)
                            .endControlFlow()
                            .addStatement("return super.$L()", getter);
                    setterCodeBlock
                            .addStatement("$L.set(\"$L\", $L == null ? null : new $T{})",
                                    FIELD_DB, key, field, byte[].class)
                            .addStatement("super.$L($L)", setter, field);
                } else {
                    if (dbClass == null) {
                        AnnotatedTypeElement<ScorePropertiesDBObject> annotated =
                                util.getAnnotatedTypeElement(fieldType, ScorePropertiesDBObject.class);
                        if (annotated != null) {
                            fieldClassName = getScorePropertiesDBObjectClassName(annotated);
                        } else {
                            fieldClassName = null;
                        }
                    } else {
                        fieldClassName = ClassName.get(dbClass);
                    }

                    if (fieldClassName != null) {
                        CodeBlock getterStatement = CodeBlock.builder()
                                .addStatement("$T $L = $L()", fieldClassName, field, getter)
                                .build();

                        closeMethod
                                .addCode(getterStatement)
                                .beginControlFlow("if ($L != null)", field)
                                .addStatement("$L.$L()", field, METHOD_CLOSE)
                                .endControlFlow();

                        flushMethod
                                .addCode(getterStatement)
                                .beginControlFlow("if ($L == null && $L.isModified(\"$L\"))",
                                        field, FIELD_DB, key)
                                .addStatement("$L = new $T()", field, fieldClassName)
                                .addStatement("$L.$L($L.concatID(\"$L\"))",
                                        field, METHOD_INITIALIZE, FIELD_DB, key)
                                .addStatement("$L.$L(null)", field, METHOD_VALUE_AS_SETTER)
                                .endControlFlow()
                                .beginControlFlow("if ($L != null)", field)
                                .addStatement("$L.$L()", field, METHOD_FLUSH)
                                .endControlFlow();

                        valueMethod
                                .addCode(getterStatement)
                                .beginControlFlow("if ($L != null)", field)
                                .addStatement("$L.$L($L.$L())", PARAM_OBJECT, setter, field, METHOD_VALUE_AS_GETTER)
                                .endControlFlow();

                        toMapMethod
                                .addCode(getterStatement)
                                .addStatement("$L.add($T.entry(\"$L\", $L != null ? $L.$L() : null))",
                                        LOCAL_ENTRIES, Immutables.class, key, field, field, METHOD_TO_MAP);

                        String localField = field+"Spo";
                        getterCodeBlock
                                .addStatement("$T $L = super.$L()", fieldType, field, getter)
                                .beginControlFlow("if ($L == null && $L.get(\"$L\") == null)",
                                        field, FIELD_DB, key)
                                .addStatement("return null")
                                .nextControlFlow("else")
                                .beginControlFlow("if (!($L instanceof $T))",
                                        field, PropertiesDB.class)
                                .addStatement("$T $L = new $T()",fieldClassName, localField, fieldClassName)
                                .addStatement("$L.$L($L.concatID(\"$L\"))",
                                        localField, METHOD_INITIALIZE, FIELD_DB, key)
                                .addStatement("$L.$L($L)", localField, METHOD_VALUE_AS_SETTER, field)
                                .addStatement("super.$L($L)", setter, localField)
                                .addStatement("$L = $L", field, localField)
                                .endControlFlow()
                                .addStatement("return ($T)$L", fieldClassName, field)
                                .endControlFlow();

                        setterCodeBlock
                                .addStatement("$T $L = $L()", fieldClassName, localField, getter)
                                .beginControlFlow("if (!($L == null && $L == null))", localField, field)
                                .beginControlFlow("if ($L == null)", localField)
                                .addStatement("$L = new $T()",localField, fieldClassName)
                                .addStatement("$L.$L($L.concatID(\"$L\"))",
                                        localField, METHOD_INITIALIZE, FIELD_DB, key)
                                .addStatement("super.$L($L)", setter, localField)
                                .addStatement("$L.set(\"$L\", new $T{})", FIELD_DB, key, byte[].class)
                                .nextControlFlow("else if ($L == null)", field)
                                .addStatement("super.$L(null)", setter)
                                .addStatement("$L.set(\"$L\", null)", FIELD_DB, key)
                                .nextControlFlow("else")
                                .addStatement("$L.$L($L)", localField, METHOD_VALUE_AS_SETTER, field)
                                .endControlFlow()
                                .endControlFlow();
                    } else {
                        String readMethod = ScoreDataObjectProcessor.findReadMethod(util, fieldType);
                        String writeMethod = ScoreDataObjectProcessor.findWriteMethod(util, fieldType);
                        if (readMethod != null && writeMethod != null) {
                            fieldClassName = ClassName.get(fieldType);
                            String localKey = "key";
                            closeMethod.addStatement("super.$L(null)", setter);

                            valueMethod.addStatement("$L.$L($L())", PARAM_OBJECT, setter, getter);
                            toMapMethod.addStatement("$L.add($T.entry(\"$L\", $L()))", LOCAL_ENTRIES, Immutables.class, key, getter);

                            String localBytes = "bytes";
                            getterCodeBlock
                                    .addStatement("$T $L = \"$L\"", String.class, localKey, key)
                                    .beginControlFlow(" if (!$L.isModified($L) && !$L.isLoaded($L))",
                                            FIELD_DB, localKey, FIELD_DB, localKey)
                                    .addStatement("$T $L = $L.get($L)",
                                            byte[].class, localBytes, FIELD_DB, localKey)
                                    .beginControlFlow("if ($L != null)", localBytes)
                                    .addStatement("super.$L($T.$L($T.newByteArrayObjectReader(\"RLPn\", $L)))",
                                            setter, fieldType, readMethod, Context.class, localBytes)
                                    .endControlFlow()
                                    .endControlFlow()
                                    .addStatement("return super.$L()", getter);
                            String localWriter = "writer";
                            setterCodeBlock
                                    .beginControlFlow("if ($L == null)", field)
                                    .addStatement("$L.set(\"$L\", null)",FIELD_DB, key)
                                    .nextControlFlow("else")
                                    .addStatement("$T $L = $T.newByteArrayObjectWriter(\"RLPn\")",
                                            ByteArrayObjectWriter.class, localWriter, Context.class)
                                    .addStatement("$T.$L($L, $L)", fieldType, writeMethod, localWriter, field)
                                    .addStatement("$L.set(\"$L\", $L.toByteArray())", FIELD_DB, key, localWriter)
                                    .endControlFlow()
                                    .addStatement("super.$L($L)", setter, field);
                        } else {
                            throw new RuntimeException(String.format("%s class is not ScorePropertiesDBObject convertible", fieldType));
                        }
                    }
                }

                builder.addMethod(MethodSpec.methodBuilder(getter)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(fieldClassName)
                        .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB)
                        .addCode(getterCodeBlock.build())
                        .build());

                builder.addMethod(MethodSpec.methodBuilder(setter)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.get(fieldType), field)
                        .addStatement("$L($L)", METHOD_REQUIRE_INITIALIZED, FIELD_DB)
                        .addCode(setterCodeBlock.build())
                        .build());
            }
        }
    }

    private boolean isBytesCodecSupported(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            type = processingEnv.getTypeUtils().boxedClass((PrimitiveType) type).asType();
        }
        return bytesCodecSupportedTypes.containsValue(type);
    }
}
