package de.mirkosertic.proxytator;

import com.sun.deploy.net.proxy.ProxyType;

/**
 * Created by mirkosertic on 11.08.2015.
 */
public class Perf {

    public static class BeanWithoutInterface {

        private final String value;

        public BeanWithoutInterface(String aValue) {
            value = aValue;
        }

        public String getValue() {
            return value;
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Proxytator theProxytator = new Proxytator();
        while(true) {
            BeanWithoutInterface theBean = theProxytator.createLazyInitProxyFor(BeanWithoutInterface.class, new BeanFactory<BeanWithoutInterface>() {
                @Override
                public BeanWithoutInterface createInstance() {
                    return new BeanWithoutInterface("lala");
                }
            });
            System.out.println(System.currentTimeMillis());
        }
    }
}
