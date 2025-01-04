package com.thatsoulyguy.invasion2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All types marked with {@code @Static} must have all
 * static fields, methods, subclasses, and a private
 * parameterless constructor.
 *
 * @author  ThatSoulyGuy
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Static { }