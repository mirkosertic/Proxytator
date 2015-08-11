package de.mirkosertic.proxytator;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import org.objenesis.ObjenesisHelper;

import java.util.HashMap;
import java.util.Map;

public class Proxytator {

    private final boolean enabled;
    private final ClassLoader classLoader;
    private final Map<Class, Class> classToEnhancedClass;

    public static class DelegatingLazyLoader<T> implements LazyLoader {

        private final BeanFactory<T> beanFactory;

        public DelegatingLazyLoader(BeanFactory<T> aFactory) {
            beanFactory = aFactory;
        }

        @Override
        public Object loadObject() throws Exception {
            return beanFactory.createInstance();
        }
    }

    public Proxytator() {
        this(true, Thread.currentThread().getContextClassLoader());
    }

    public Proxytator(boolean aEnabled, ClassLoader aClassLoader) {
        enabled = aEnabled;
        classLoader = aClassLoader;
        classToEnhancedClass = new HashMap<Class, Class>();
    }

    public boolean isLazyInitProxy(Object aObject) {
        return aObject instanceof LazyInitBean;
    }

    public <T> T createLazyInitProxyFor(Class<T> aClass, final BeanFactory<T> aFactory) throws IllegalAccessException, InstantiationException {
        if (!enabled) {
            return aFactory.createInstance();
        }

        Class theEnhancedClass;

        synchronized (classToEnhancedClass) {
            theEnhancedClass = classToEnhancedClass.get(aClass);
            if (theEnhancedClass == null) {
                Enhancer theEnhancer = new Enhancer();
                theEnhancer.setClassLoader(classLoader);
                theEnhancer.setInterfaces(new Class[] {LazyInitBean.class});
                theEnhancer.setSuperclass(aClass);
                theEnhancer.setCallbackType(DelegatingLazyLoader.class);
                theEnhancedClass = theEnhancer.createClass();
                classToEnhancedClass.put(aClass, theEnhancedClass);
            }
        }

        Enhancer.registerCallbacks(theEnhancedClass, new Callback[] {new DelegatingLazyLoader<T>(aFactory)});

        // And we use Objenesis to instantiate the class, perhaps it does not have a default
        // constructor, so plain cglib proxies won't work.
        return (T) ObjenesisHelper.newInstance(theEnhancedClass);
    }
}