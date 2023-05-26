package nl.sanderhautvast.contiguous;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContiguousListTest {

    @Test
    public void testAddAndGetString() {
        List<String> list = new ContiguousList<>(String.class);
        assertTrue(list.isEmpty());

        list.add("hitchhikersguide to the galaxy");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());

        String title = list.get(0);
        assertEquals("hitchhikersguide to the galaxy", title);
    }

    @Test
    public void testStringBean() {
        ContiguousList<StringBean> beanList = new ContiguousList<>(StringBean.class);
        beanList.add(new StringBean("Douglas Adams"));

        assertArrayEquals(new byte[]{39, 68, 111, 117, 103, 108, 97, 115, 32, 65, 100, 97, 109, 115}, beanList.getData());

        StringBean douglas = beanList.get(0);
        assertEquals("Douglas Adams", douglas.getName());

        // now add new data to see if existing data remains intact
        beanList.add(new StringBean("Ford Prefect"));

        assertEquals("Douglas Adams", beanList.get(0).getName());
        assertEquals("Ford Prefect", beanList.get(1).getName());

        assertEquals(2, beanList.size());
    }

    @Test
    public void testIntBean() {
        ContiguousList<IntBean> beanList = new ContiguousList<>(IntBean.class);
        beanList.add(new IntBean(42));

        assertArrayEquals(new byte[]{1, 42},
                beanList.getData());
        assertEquals(42, beanList.get(0).getValue());
    }

    @Test
    public void testLong() {
        ContiguousList<LongBean> beanList = new ContiguousList<>(LongBean.class);
        beanList.add(new LongBean(42));

        assertArrayEquals(new byte[]{1, 42},
                beanList.getData());
        assertEquals(42, beanList.get(0).getValue());
    }

    @Test
    public void testShort() {
        ContiguousList<ShortBean> beanList = new ContiguousList<>(ShortBean.class);
        beanList.add(new ShortBean((short) 42));

        assertArrayEquals(new byte[]{1, 42},
                beanList.getData());
        assertArrayEquals(new int[]{0, 2}, beanList.getElementIndices());
    }

    @Test
    public void testByte() {
        ContiguousList<ByteBean> beanList = new ContiguousList<>(ByteBean.class);
        beanList.add(new ByteBean((byte) -42));

        assertArrayEquals(new byte[]{1, -42},
                beanList.getData());
        assertArrayEquals(new int[]{0, 2}, beanList.getElementIndices());
    }

    @Test
    public void testNestedBean() {
        ContiguousList<NestedBean> beanList = new ContiguousList<>(NestedBean.class);
        beanList.add(new NestedBean(new StringBean("vogon constructor fleet"), new IntBean(42)));

        NestedBean expected = new NestedBean(new StringBean("vogon constructor fleet"), new IntBean(42));
        assertEquals(expected, beanList.get(0));
    }

    @Test
    public void testFloat() {
        ContiguousList<FloatBean> beanList = new ContiguousList<>(FloatBean.class);
        beanList.add(new FloatBean(1.1F));


        assertEquals(1.1F, beanList.get(0).getValue());
    }

    @Test
    public void testDouble() {
        ContiguousList<DoubleBean> beanList = new ContiguousList<>(DoubleBean.class);
        beanList.add(new DoubleBean(1.1));

        assertEquals(1.1, beanList.get(0).getValue());
    }

    @Test
    public void testNullFloat() {
        ContiguousList<FloatBean> beanList = new ContiguousList<>(FloatBean.class);
        beanList.add(new FloatBean(null));

        assertNull(beanList.get(0).getValue());
    }

    @Test
    public void testNullString() {
        ContiguousList<StringBean> beanList = new ContiguousList<>(StringBean.class);
        beanList.add(new StringBean(null));

        assertNull(beanList.get(0).getName());
    }

    @Test
    public void test100Elements() {
        ContiguousList<StringBean> beanList = new ContiguousList<>(StringBean.class);
        for (int i = 0; i < 100; i++) {
            beanList.add(new StringBean(null));
        }
        assertEquals(100, beanList.size());
        for (int i = 0; i < 100; i++) {
            assertNull(beanList.get(i).getName());
        }
    }

    @Test
    public void testBigInteger() {
        ContiguousList<BigInteger> bigIntegers = new ContiguousList<>(BigInteger.class);
        bigIntegers.add(new BigInteger("1000000000"));
        assertEquals(1_000_000_000L, bigIntegers.get(0).longValue());
    }

    @Test
    public void testIterator() {
        ContiguousList<Integer> integers = new ContiguousList<>(Integer.class);
        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }
        int prevValue = -1;
        for (int value : integers) {
            assertEquals(prevValue, value - 1);
            prevValue = value;
        }

        integers.add(100);

        assertEquals(100, integers.get(100));
    }

    @Test
    public void testPrimitiveIterator() {
        ContiguousList<Integer> integers = new ContiguousList<>(Integer.class);
        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }
        long prevValue = -1;
        for (Iterator<?> intIter = integers.valueIterator(); intIter.hasNext(); ) {
            int value = (int) intIter.next();
            assertEquals(prevValue, value - 1);
            prevValue = value;
        }


        integers.add(100);

        assertEquals(100, integers.get(100));
    }

    @Test
    public void testCompoundValueIterator() {
        ContiguousList<IntBean> integers = new ContiguousList<>(IntBean.class);
        for (int i = 0; i < 100; i++) {
            integers.add(new IntBean(i));
        }
        long prevValue = -1;
        for (Iterator<?> intIter = integers.valueIterator(); intIter.hasNext(); ) {
            int value = (int) intIter.next();
            assertEquals(prevValue, value - 1);
            prevValue = value;
        }


        integers.add(new IntBean(100));

        assertEquals(new IntBean(100), integers.get(100)); // here an instance
    }

    @Test
    public void testGetTypesWhenCompound() {
        ContiguousList<NestedBean> integers = new ContiguousList<>(NestedBean.class);
        Iterator<Class<?>> typeIterator = integers.getTypes().iterator();
        assertTrue(typeIterator.hasNext());
        assertEquals(NestedBean.class, typeIterator.next());
        assertTrue(typeIterator.hasNext());
        assertEquals(StringBean.class, typeIterator.next());
        assertTrue(typeIterator.hasNext());
        assertEquals(String.class, typeIterator.next());
        assertTrue(typeIterator.hasNext());
        assertEquals(IntBean.class, typeIterator.next());
        assertTrue(typeIterator.hasNext());
        assertEquals(Integer.class, typeIterator.next());

    }

    @Test
    public void testGetStoredTypesDeepCompound() {
        ContiguousList<DeepBean> beans = new ContiguousList<>(DeepBean.class);
        Iterator<Class<?>> typeIterator = beans.getTypes().iterator();
        assertTrue(typeIterator.hasNext());
        assertEquals(DeepBean.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(NestedBean.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(StringBean.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(String.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(IntBean.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(Integer.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(Long.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(StringBean.class, typeIterator.next());

        assertTrue(typeIterator.hasNext());
        assertEquals(String.class, typeIterator.next());

    }

    @Test
    public void testTypeIteratorBuiltin() {
        ContiguousList<Integer> integers = new ContiguousList<>(Integer.class);
        Iterator<Class<?>> typeIterator = integers.getTypes().iterator();
        if (typeIterator.hasNext()) {
            assertEquals(Integer.class, typeIterator.next());
        }
    }
}
