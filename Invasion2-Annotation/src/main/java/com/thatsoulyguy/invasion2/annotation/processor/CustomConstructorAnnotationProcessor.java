package com.thatsoulyguy.invasion2.annotation.processor;

import com.thatsoulyguy.invasion2.annotation.CustomConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_23)
@SupportedAnnotationTypes("com.thatsoulyguy.invasion2.annotation.CustomConstructor")
public class CustomConstructorAnnotationProcessor extends AbstractProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment)
    {
        for (Element element : environment.getElementsAnnotatedWith(CustomConstructor.class))
        {
            if (element.getKind() != ElementKind.CLASS)
            {
                processingEnv.getMessager().printMessage
                (
                    Diagnostic.Kind.ERROR,
                    "@CustomConstructor can only be applied to classes",
                    element
                );

                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            CustomConstructor customConstructor = typeElement.getAnnotation(CustomConstructor.class);

            boolean hasNonPrivateConstructor = false;
            boolean hasCustomConstructor = false;

            for (Element enclosed : typeElement.getEnclosedElements())
            {
                if (enclosed.getKind() == ElementKind.CONSTRUCTOR)
                {
                    ExecutableElement constructor = (ExecutableElement) enclosed;

                    if (!constructor.getModifiers().contains(Modifier.PRIVATE) || !constructor.getParameters().isEmpty()) {
                        hasNonPrivateConstructor = true;

                        processingEnv.getMessager().printMessage
                        (
                            Diagnostic.Kind.ERROR,
                            "A @CustomConstructor class cannot have public or protected constructors with parameters.",
                            constructor
                        );
                    }
                }

                if (enclosed.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement method = (ExecutableElement) enclosed;

                    if (method.getModifiers().contains(Modifier.STATIC)
                            && method.getModifiers().contains(Modifier.PUBLIC)
                            && method.getReturnType().toString().contains(typeElement.getSimpleName().toString())
                            && method.getSimpleName().toString().equals(customConstructor.value())
                            && method.getAnnotation(NotNull.class) != null)
                        hasCustomConstructor = true;
                }
            }

            if (!hasCustomConstructor)
            {
                processingEnv.getMessager().printMessage
                (
                    Diagnostic.Kind.ERROR,
                    "A @CustomConstructor class must have a static method named "
                            + customConstructor.value()
                            + " that returns the class type. The method must also be annotated with @org.jetbrains.annotations.NotNull. ",
                    typeElement
                );
            }

            if (hasNonPrivateConstructor || !hasCustomConstructor)
            {
                processingEnv.getMessager().printMessage
                (
                    Diagnostic.Kind.ERROR,
                    "The class does not conform to @CustomConstructor class rules.",
                    typeElement
                );
            }
        }

        return true;
    }
}