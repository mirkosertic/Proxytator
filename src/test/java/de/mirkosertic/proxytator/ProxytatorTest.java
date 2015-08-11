package de.mirkosertic.proxytator;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ProxytatorTest {

    public static class BeanWithoutInterface {

        private final String value;

        public BeanWithoutInterface(String aValue) {
            value = aValue;
        }

        public String getValue() {
            return value;
        }
    }

    public static class BeanWithInterface implements Serializable {

        private final String value;

        public BeanWithInterface(String aValue) {
            value = aValue;
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void testBeanWithoutInterface() throws InstantiationException, IllegalAccessException {
        Proxytator theProxytator = new Proxytator();

        BeanFactory<BeanWithoutInterface> theFactory = mock(BeanFactory.class);
        when(theFactory.createInstance()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new BeanWithoutInterface("lala");
            }
        });

        BeanWithoutInterface theBean = theProxytator.createLazyInitProxyFor(BeanWithoutInterface.class, theFactory);

        assertEquals("lala", theBean.getValue());
        assertEquals("lala", theBean.getValue());

        assertTrue(theProxytator.isLazyInitProxy(theBean));

        verify(theFactory, times(1)).createInstance();
    }

    @Test
    public void testBeanWithInterface() throws InstantiationException, IllegalAccessException {
        Proxytator theProxytator = new Proxytator();

        BeanFactory<BeanWithInterface> theFactory = mock(BeanFactory.class);
        when(theFactory.createInstance()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new BeanWithInterface("lala");
            }
        });

        BeanWithInterface theBean = theProxytator.createLazyInitProxyFor(BeanWithInterface.class, theFactory);

        assertEquals("lala", theBean.getValue());
        assertEquals("lala", theBean.getValue());

        verify(theFactory, times(1)).createInstance();
    }

    @Test
    public void testThreadSafety() throws ExecutionException, InterruptedException {
        final Proxytator theProxytator = new Proxytator();

        int theSize = 20;

        List<Future<Boolean>> theResults = new ArrayList<Future<Boolean>>();

        ExecutorService theExecutorService = Executors.newFixedThreadPool(theSize);
        for (int i=0;i<theSize;i++) {
            theResults.add(theExecutorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    BeanWithInterface theBean = theProxytator.createLazyInitProxyFor(BeanWithInterface.class, new BeanFactory<BeanWithInterface>() {
                        @Override
                        public BeanWithInterface createInstance() {
                            System.out.println("Running test in " + Thread.currentThread().getName());
                            return new BeanWithInterface(Thread.currentThread().getName());
                        }
                    });

                    return Thread.currentThread().getName().equals(theBean.getValue());
                }
            }));
        }

        assertEquals(theSize, theResults.size(), 0);
        for (Future<Boolean> theFuture : theResults) {
            assertTrue(theFuture.get());
        }
    }

    @Test
    public void testDisabled() throws InstantiationException, IllegalAccessException {
        Proxytator theProxytator = new Proxytator(false, Thread.currentThread().getContextClassLoader());

        BeanFactory<BeanWithoutInterface> theFactory = mock(BeanFactory.class);
        when(theFactory.createInstance()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new BeanWithoutInterface("lala");
            }
        });

        BeanWithoutInterface theBean = theProxytator.createLazyInitProxyFor(BeanWithoutInterface.class, theFactory);

        assertEquals("lala", theBean.getValue());
        assertEquals("lala", theBean.getValue());

        assertFalse(theProxytator.isLazyInitProxy(theBean));
        verify(theFactory, times(1)).createInstance();
    }
}