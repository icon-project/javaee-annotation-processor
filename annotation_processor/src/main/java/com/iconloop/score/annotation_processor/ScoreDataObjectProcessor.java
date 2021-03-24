package com.iconloop.score.annotation_processor;

import com.squareup.javapoet.*;
import score.Address;
import score.ObjectReader;
import score.ObjectWriter;

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
import javax.lang.model.util.Types;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ScoreDataObjectProcessor extends AbstractProcessor {
    private ProcessorUtil util;

    static final String METHOD_READ = "readObject";
    static final String METHOD_WRITE = "writeObject";
    static final String PARAM_READER = "reader";
    static final String PARAM_WRITER = "writer";
    static final String PARAM_OBJECT = "obj";
    static final String LOCAL_OBJECT = "obj";

    static final String TYPENAME_BATE_ARRAY = byte[].class.getTypeName();

    static final String DEFAULT_FORMAT_WRITE = "writeNullable(%s)";

    private Map<TypeMirror, Format> formats;
    private List<TypeMirror> listTypes;

    class Format {
        private String read;
        private String write;

        public Format(String read, String write) {
            this.read = read;
            this.write = write;
        }

        public String getRead() {
            return read;
        }

        public String getWrite() {
            return write;
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        util = new ProcessorUtil(processingEnv, ScoreDataObjectProcessor.class.getSimpleName());
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();
        listTypes = new ArrayList<>();
        listTypes.add(elements.getTypeElement(List.class.getName()).asType());
        listTypes.add(elements.getTypeElement(scorex.util.ArrayList.class.getName()).asType());

        formats = new HashMap<>();
        formats.put(elements.getTypeElement(Boolean.class.getName()).asType(),
                new Format("$L.readBoolean()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Character.class.getName()).asType(),
                new Format("$L.readChar()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Byte.class.getName()).asType(),
                new Format("$L.readByte()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Short.class.getName()).asType(),
                new Format("$L.readShort()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Integer.class.getName()).asType(),
                new Format("$L.readInt()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Long.class.getName()).asType(),
                new Format("$L.readLong()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Float.class.getName()).asType(),
                new Format("$L.readFloat()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Double.class.getName()).asType(),
                new Format("$L.readDouble()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(String.class.getName()).asType(),
                new Format("$L.readString()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(BigInteger.class.getName()).asType(),
                new Format("$L.readBigInteger()", DEFAULT_FORMAT_WRITE));
        formats.put(elements.getTypeElement(Address.class.getName()).asType(),
                new Format("$L.readAddress()", DEFAULT_FORMAT_WRITE));
        formats.put(types.getArrayType(types.getPrimitiveType(TypeKind.BYTE)),
                new Format("$L.readByteArray()", DEFAULT_FORMAT_WRITE));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add(ScoreDataObject.class.getCanonicalName());
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

    private static ClassName getScoreDataObjectClassName(AnnotatedTypeElement<ScoreDataObject> annotated) {
        TypeElement element = annotated.getElement();
        ScoreDataObject ann = annotated.getAnnotation();
        return ClassName.get(
                ClassName.get(element).packageName(),
                element.getSimpleName() + ann.suffix());
    }

    private CodeBlock getReadCodeBlock(TypeMirror variableType, ScoreDataProperty annProperty, String field, String setter) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        boolean nullable = !variableType.getKind().isPrimitive();
        String readObject = null;
        if (annProperty != null) {
            nullable = annProperty.nullable() && nullable;
            if (!annProperty.readObject().isEmpty()) {
                readObject = annProperty.readObject();
            }
        }
        if (readObject != null) {
            if (field.equals(setter) || !nullable) {
                codeBlock.addStatement(setter,
                        CodeBlock.builder().add("$L($L)", readObject, PARAM_READER).build());
            } else {
                codeBlock
                        .addStatement("$T $L = null", variableType, field)
                        .beginControlFlow("if ($L.readBoolean())", PARAM_READER)
                        .addStatement("$L = $L($L)", field, readObject, PARAM_READER)
                        .endControlFlow()
                        .addStatement(setter, field);
            }
        } else {
            CodeBlock.Builder formatCodeBlock = CodeBlock.builder();
            Map.Entry<TypeMirror, Format> entry = getFormat(variableType);
            if (entry != null) {
                if (field.equals(setter) || !nullable) {
                    formatCodeBlock.add(entry.getValue().getRead(), PARAM_READER);
                } else {
                    formatCodeBlock.add("$L.readNullable($T.class)", PARAM_READER, variableType);
                }
            } else {
                AnnotatedTypeElement<ScoreDataObject> annotated = util.getAnnotatedTypeElement(variableType, ScoreDataObject.class);
                if (annotated != null) {
                    ClassName fieldClassName = getScoreDataObjectClassName(annotated);
                    if (field.equals(setter) || !nullable) {
                        formatCodeBlock.add("$L.read($T.class)", PARAM_READER, fieldClassName);
                    } else {
                        formatCodeBlock.add("$L.readNullable($T.class)", PARAM_READER, fieldClassName);
                    }
                } else {
                    if (util.hasMethod(variableType, METHOD_READ, new Modifier[]{Modifier.PUBLIC, Modifier.STATIC}, ObjectReader.class)) {
                        if (field.equals(setter) || !nullable) {
                            formatCodeBlock.add("$L.read($T.class)", PARAM_READER, variableType);
                        } else {
                            formatCodeBlock.add("$L.readNullable($T.class)", PARAM_READER, variableType);
                        }
                    } else {
                        throw new RuntimeException(String.format("%s class is not ScoreDataObject convertible", variableType));
                    }
                }
            }
            codeBlock.addStatement(setter, formatCodeBlock.build());
        }
        return codeBlock.build();
    }

    private CodeBlock getWriteCodeBlock(TypeMirror variableType, ScoreDataProperty annProperty, String field, String getter) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        boolean nullable = !variableType.getKind().isPrimitive();
        String writeObject = null;
        if (annProperty != null) {
            nullable = annProperty.nullable() && nullable;
            if (!annProperty.writeObject().isEmpty()) {
                writeObject = annProperty.writeObject();
            }
        }

        if (writeObject != null) {
            if (field.equals(getter) || !nullable) {
                codeBlock.addStatement("$L($L, $L)", writeObject, PARAM_WRITER, getter);
            } else {
                codeBlock
                        .addStatement("$T $L = $L", variableType, field, getter)
                        .addStatement("$L.write($L != null)", PARAM_WRITER, field)
                        .beginControlFlow("if ($L != null)", field)
                        .addStatement("$L($L, $L)", writeObject, PARAM_WRITER, field)
                        .endControlFlow();
            }
        } else {
            Map.Entry<TypeMirror, Format> entry = getFormat(variableType);
            if (entry != null) {
                codeBlock.addStatement("$L.$L($L)",
                        PARAM_WRITER,
                        (field.equals(getter) || !nullable) ? "write" : "writeNullable",
                        getter);
            } else {
                AnnotatedTypeElement<ScoreDataObject> annotated = util.getAnnotatedTypeElement(variableType, ScoreDataObject.class);
                if (annotated != null) {
                    ClassName fieldClassName = getScoreDataObjectClassName(annotated);
                    if (field.equals(getter) || !nullable) {
                        //in array
                        codeBlock.addStatement("$L.write(new $T($L))", PARAM_WRITER, fieldClassName, field);
                    } else {
                        codeBlock
                                .addStatement("$T $L = $L", variableType, field, getter)
                                .addStatement("$L.$L($L != null ? new $T($L) : null)",
                                        PARAM_WRITER,
                                        nullable ? "writeNullable" : "write",
                                        field,
                                        fieldClassName,
                                        field);
                    }
                } else {
                    if (util.hasMethod(variableType, METHOD_WRITE, new Modifier[]{Modifier.PUBLIC, Modifier.STATIC}, ObjectWriter.class, Object.class)) {
                        codeBlock.addStatement("$L.$L($L)",
                                PARAM_WRITER,
                                (field.equals(getter) || !nullable) ? "write" : "writeNullable",
                                getter);
                    } else {
                        throw new RuntimeException(String.format("%s class is not ScoreDataObject convertible", variableType));
                    }
                }
            }
        }
        return codeBlock.build();
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
        ScoreDataObject annClass = element.getAnnotation(ScoreDataObject.class);
        ClassName className = ClassName.get(parentClassName.packageName(), parentClassName.simpleName() + annClass.suffix());
        TypeSpec.Builder builder = TypeSpec
                .classBuilder(ClassName.get(parentClassName.packageName(), className.simpleName()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(element.asType());

        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super()")
                .build());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .addStatement("super()");

        if (!METHOD_READ.equals(annClass.readObject())) {
            builder.addMethod(MethodSpec.methodBuilder(annClass.readObject())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ObjectReader.class, PARAM_READER)
                    .returns(className)
                    .addStatement("return $L.$L($L)", className.simpleName(), METHOD_READ, PARAM_READER)
                    .build());
        }
        MethodSpec.Builder readMethod = MethodSpec.methodBuilder(METHOD_READ)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ObjectReader.class, PARAM_READER)
                .returns(className)
                .addStatement("$L $L = new $L()", className.simpleName(), LOCAL_OBJECT, className.simpleName());

        builder.addMethod(MethodSpec.methodBuilder(METHOD_WRITE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ObjectWriter.class, PARAM_WRITER)
                .addParameter(TypeName.get(element.asType()), PARAM_OBJECT)
                .addStatement("$L.$L($L, $L instanceof $L ? ($L)$L : new $L($L))",
                        className.simpleName(), METHOD_WRITE, PARAM_WRITER,
                        PARAM_OBJECT, className.simpleName(),
                        className.simpleName(), PARAM_OBJECT,
                        className.simpleName(), PARAM_OBJECT)
                .build());
        builder.addMethod(MethodSpec.methodBuilder(METHOD_WRITE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ObjectWriter.class, PARAM_WRITER)
                .addParameter(className, PARAM_OBJECT)
                .addStatement("$L.$L($L)", PARAM_OBJECT, annClass.writeObject(), PARAM_WRITER)
                .build());
        MethodSpec.Builder writeMethod = MethodSpec.methodBuilder(annClass.writeObject())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ObjectWriter.class, PARAM_WRITER);

        processMethod(element, constructor, readMethod, writeMethod);

        builder.addMethod(constructor.build());
        builder.addMethod(readMethod
                .addStatement("return $L", LOCAL_OBJECT)
                .build());
        builder.addMethod(writeMethod.build());
        return builder.build();
    }

    private void processMethod(
            TypeElement element,
            MethodSpec.Builder constructor,
            MethodSpec.Builder readMethod, MethodSpec.Builder writeMethod) {
        TypeMirror superClass = element.getSuperclass();
        TypeElement superElement = util.getTypeElement(superClass);
        if (superElement != null) {
            processMethod(superElement, constructor, readMethod, writeMethod);
        }

        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.FIELD) &&
                    !ProcessorUtil.hasModifier(enclosedElement, Modifier.STATIC)) {
                VariableElement variableElement = (VariableElement) enclosedElement;
                ScoreDataProperty annField = variableElement.getAnnotation(ScoreDataProperty.class);
                if (annField != null && annField.ignore()) {
                    continue;
                }

                TypeMirror fieldType = variableElement.asType();
                String field = variableElement.getSimpleName().toString();
                String capitalized = field.substring(0, 1).toUpperCase() + field.substring(1);
                String getter = (fieldType.getKind() == TypeKind.BOOLEAN ? "is" : "get") + capitalized;
                String setter = "set" + capitalized;

                boolean direct = false;
                if (annField != null) {
                    direct = annField.direct();
                    if (!annField.getter().isEmpty()) {
                        getter = annField.getter();
                    }
                    if (!annField.setter().isEmpty()) {
                        setter = annField.setter();
                    }
                }

                if (direct) {
                    constructor.addStatement("this.$L = $L.$L", field, PARAM_OBJECT, field);
                    getter = String.format("this.%s", field);
                    setter = String.format("%s.%s = $L", LOCAL_OBJECT, field);
                } else {
                    constructor.addStatement("this.$L($L.$L())", setter, PARAM_OBJECT, getter);
                    getter = String.format("this.%s()", getter);
                    setter = String.format("%s.%s($L)", LOCAL_OBJECT, setter);
                }

                boolean isList = util.containsDeclaredType(listTypes, fieldType);
                if (!fieldType.toString().equals(TYPENAME_BATE_ARRAY) && (isList || fieldType.getKind() == TypeKind.ARRAY)) {
                    TypeMirror componentType;
                    if (isList) {
                        componentType = ((DeclaredType) fieldType).getTypeArguments().get(0);
                    } else {
                        componentType = ((ArrayType) fieldType).getComponentType();
                    }
                    writeMethod
                            .addStatement("$T $L = $L", fieldType, field, getter)
                            .beginControlFlow("if ($L != null)", field)
                            .addCode(CodeBlock.builder()
                                    .addStatement("$L.beginNullableList($L.$L)", PARAM_WRITER, field, isList ? "size()" : "length")
                                    .beginControlFlow("for($T v : $L)", componentType, field)
                                    .add(getWriteCodeBlock(componentType, annField, "v", "v"))
                                    .endControlFlow()
                                    .build())
                            .nextControlFlow("else")
                            .addStatement("$L.writeNull()", PARAM_WRITER)
                            .endControlFlow()
                            .addStatement("$L.end()", PARAM_WRITER);

                    readMethod
                            .beginControlFlow("if ($L.beginNullableList())", PARAM_READER)
                            .addCode("$T $L", fieldType, field);

                    String localList = field;
                    if (!isList) {
                        localList += "List";
                        readMethod.addStatement(" = null");
                        if (componentType.getKind().isPrimitive()) {
                            readMethod.addCode("$T $L", List.class, localList);
                        } else {
                            readMethod.addCode("$T<$T> $L", List.class, componentType, localList);
                        }
                    }
                    readMethod.addStatement(" = new $T<>()", scorex.util.ArrayList.class);
                    String setterOfList = localList + ".add($L)";
                    readMethod
                            .beginControlFlow("while($L.hasNext())", PARAM_READER)
                            .addCode(getReadCodeBlock(componentType, annField, setterOfList, setterOfList))
//                            .addStatement("$L.add($L)", localList, getReadStatement(componentType, annField))
                            .endControlFlow();

                    if (!isList) {
//                        if (componentType.getKind().isPrimitive()) {
                        readMethod
                                .addStatement("$L = new $T[$L.size()]", field, componentType, localList)
                                .beginControlFlow("for(int i=0; i<$L.size(); i++)", localList)
                                .addStatement("$L[i] = ($T)$L.get(i)", field, componentType, localList)
                                .endControlFlow();
//                        } else {
//                            readMethod.addStatement("$L = ($T)$L.toArray()", field, fieldType, localList);
//                        }
                    }

                    readMethod
                            .addStatement(setter, field)
                            .endControlFlow()
                            .addStatement("$L.end()", PARAM_READER);
                } else {
                    readMethod.addCode(getReadCodeBlock(fieldType, annField, field, setter));
                    writeMethod.addCode(getWriteCodeBlock(fieldType, annField, field, getter));
                }
            }
        }
    }
}
