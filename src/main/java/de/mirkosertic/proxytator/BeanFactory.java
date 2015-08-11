package de.mirkosertic.proxytator;

public interface BeanFactory<T> {
    T createInstance();
}
