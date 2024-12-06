package com.thatsoulyguy.invasion2.util;

import org.jetbrains.annotations.NotNull;

public interface ManagerLinkedClass
{
    @NotNull Class<?> getManagingClass();
    @NotNull String getManagedItem();
}
