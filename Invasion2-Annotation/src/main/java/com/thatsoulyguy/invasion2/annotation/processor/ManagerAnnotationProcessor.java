package com.thatsoulyguy.invasion2.annotation.processor;

import com.thatsoulyguy.invasion2.annotation.Manager;
import com.thatsoulyguy.invasion2.annotation.Static;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_23)
@SupportedAnnotationTypes("com.thatsoulyguy.invasion2.annotation.Manager")
public class ManagerAnnotationProcessor extends AbstractProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        for (Element element : roundEnv.getElementsAnnotatedWith(Manager.class))
        {
            if (element.getKind() != ElementKind.CLASS)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Manager can only be applied to classes", element);
                continue;
            }

            TypeElement classElement = (TypeElement) element;

            if (classElement.getAnnotation(Static.class) == null)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class annotated with @Manager must also be annotated with @Static.", classElement);
                continue;
            }

            String managedClass = extractManagedClassFromAnnotation(classElement);

            if (managedClass == null)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to determine the managed class.", classElement);
                continue;
            }

            if (!validateMethod(classElement, "register", managedClass, true, true, null, false, false))
            {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method register must have one @NotNull parameter of type " + managedClass + " and must not return a value.",
                        classElement
                );
            }

            if (!validateMethod(classElement, "unregister", "java.lang.String", true, true, null, false, false))
            {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method unregister must have one @NotNull parameter of type String and must not return a value.",
                        classElement
                );
            }

            if (!validateMethod(classElement, "get", "java.lang.String", true, false, managedClass, true, true))
            {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method get must have one @NotNull parameter of type String and must return a @NotNull value of type " + managedClass + ".",
                        classElement
                );
            }

            if (!validateMethod(classElement, "getAll", null, false, true, "java.util.List", true, false))
            {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method getAll must return a @NotNull List<" + managedClass + "> and must not have parameters.",
                        classElement
                );
            }

            if (!validateMethod(classElement, "uninitialize", null, false, false, null, false, false))
            {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Method uninitialize must have no parameters and no return value.",
                        classElement
                );
            }
        }

        return true;
    }

    private String extractManagedClassFromAnnotation(TypeElement classElement)
    {
        for (AnnotationMirror annotation : classElement.getAnnotationMirrors())
        {
            if (annotation.getAnnotationType().toString().equals(Manager.class.getCanonicalName()))
            {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet())
                {
                    if (entry.getKey().getSimpleName().toString().equals("value"))
                        return entry.getValue().getValue().toString();
                }
            }
        }
        return null;
    }

    private boolean validateMethod(@NotNull Element element, @NotNull String name, @Nullable String parameterTypeName, boolean shouldParameterBeNotNull, boolean shouldBeNotNull, @Nullable String returnTypeName, boolean shouldHaveReturnValue, boolean shouldReturnValueBeNullable)
    {
        boolean foundCorrectSignature = false;

        for (Element enclosed : element.getEnclosedElements())
        {
            if (enclosed.getKind() == ElementKind.METHOD && enclosed.getSimpleName().toString().equals(name))
            {
                if (foundCorrectSignature)
                    continue;

                ExecutableElement method = (ExecutableElement) enclosed;

                boolean hasCorrectSignature = true;

                if (!method.getModifiers().contains(Modifier.STATIC))
                {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Method " + name + " must be static.",
                            method
                    );
                    hasCorrectSignature = false;
                }

                List<? extends VariableElement> parameters = method.getParameters();

                if (parameterTypeName != null)
                {
                    if (parameters.size() != 1)
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must have exactly one parameter.",
                                method
                        );
                        hasCorrectSignature = false;
                    }
                    else
                    {
                        VariableElement parameter = parameters.get(0);

                        if (!processingEnv.getTypeUtils().erasure(parameter.asType()).toString().equals(parameterTypeName))
                        {
                            processingEnv.getMessager().printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Parameter in method " + name + " must be of type " + parameterTypeName + ".",
                                    parameter
                            );
                            hasCorrectSignature = false;
                        }

                        if (shouldParameterBeNotNull && parameter.getAnnotation(NotNull.class) == null)
                        {
                            processingEnv.getMessager().printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Parameter in method " + name + " must be annotated with @NotNull.",
                                    parameter
                            );
                            hasCorrectSignature = false;
                        }
                    }
                }
                else
                {
                    if (!parameters.isEmpty())
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must not have any parameters.",
                                method
                        );
                        hasCorrectSignature = false;
                    }
                }

                TypeMirror returnType = method.getReturnType();

                if (shouldHaveReturnValue)
                {
                    if (returnType.getKind() == TypeKind.VOID)
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must return a value.",
                                method
                        );

                        hasCorrectSignature = false;
                    }
                    else if (returnTypeName != null && !processingEnv.getTypeUtils().erasure(returnType).toString().equals(returnTypeName))
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Return type of method " + name + " must be " + returnTypeName + ".",
                                method
                        );

                        hasCorrectSignature = false;
                    }

                    if (shouldBeNotNull && method.getAnnotation(NotNull.class) == null)
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must be annotated with @NotNull.",
                                method
                        );

                        hasCorrectSignature = false;
                    }

                    if (shouldReturnValueBeNullable && method.getAnnotation(Nullable.class) == null)
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must be annotated with @Nullable.",
                                method
                        );

                        hasCorrectSignature = false;
                    }
                }
                else
                {
                    if (returnType.getKind() != TypeKind.VOID)
                    {
                        processingEnv.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                "Method " + name + " must not return a value.",
                                method
                        );
                        hasCorrectSignature = false;
                    }
                }

                if (hasCorrectSignature)
                    foundCorrectSignature = true;
            }
        }

        if (!foundCorrectSignature)
        {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Method " + name + " is missing or does not match the expected signature.",
                    element
            );
        }

        return foundCorrectSignature;
    }
}