/*
 * Copyright 2000-2016 Holon TDCN.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.core.test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Test;

import com.holonplatform.core.Context;
import com.holonplatform.core.beans.BeanIntrospector;
import com.holonplatform.core.beans.BeanPropertySet;
import com.holonplatform.core.i18n.Localizable;
import com.holonplatform.core.i18n.LocalizationContext;
import com.holonplatform.core.internal.beans.DefaultBeanIntrospector;
import com.holonplatform.core.internal.property.NumericBooleanConverter;
import com.holonplatform.core.internal.utils.TestUtils;
import com.holonplatform.core.internal.utils.TypeUtils;
import com.holonplatform.core.presentation.StringValuePresenter;
import com.holonplatform.core.property.PathProperty;
import com.holonplatform.core.property.Property;
import com.holonplatform.core.property.Property.PropertyNotFoundException;
import com.holonplatform.core.property.Property.PropertyReadException;
import com.holonplatform.core.property.Property.PropertyReadOnlyException;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.PropertyValueConverter;
import com.holonplatform.core.property.PropertyValueConverter.PropertyConversionException;
import com.holonplatform.core.property.PropertyValuePresenterRegistry;
import com.holonplatform.core.property.PropertyValueProvider;
import com.holonplatform.core.property.VirtualProperty;
import com.holonplatform.core.property.VirtualProperty.Builder;
import com.holonplatform.core.query.QueryFilter;
import com.holonplatform.core.query.QueryFilter.FilterOperator;
import com.holonplatform.core.query.QueryFilter.OperationQueryFilter;
import com.holonplatform.core.query.QuerySort;
import com.holonplatform.core.query.QuerySort.PathQuerySort;
import com.holonplatform.core.query.QuerySort.SortDirection;
import com.holonplatform.core.temporal.TemporalType;
import com.holonplatform.core.test.data.TestBean;
import com.holonplatform.core.test.data.TestBean2;
import com.holonplatform.core.test.data.TestNested;
import com.holonplatform.core.test.data.TestPropertySet;

/**
 * Property architecture test
 */
@SuppressWarnings("rawtypes")
public class TestProperty {

	@Test
	public void testPaths() {
		final PathProperty<String> PARENT1 = PathProperty.create("parent1", String.class);

		final PathProperty<Integer> PRP = PathProperty.create("prp", Integer.class).parent(PARENT1);

		assertEquals("parent1.prp", PRP.fullName());

		final PathProperty<String> PARENT2 = PathProperty.create("parent2", String.class).parent(PARENT1);

		final PathProperty<Integer> PRP2 = PathProperty.create("prp2", Integer.class).parent(PARENT2);

		assertEquals("parent1.parent2.prp2", PRP2.fullName());

	}

	@Test
	public void testBase() {

		Property<String> gp = VirtualProperty.create(String.class);
		assertEquals(String.class, gp.getType());

		Builder<String, ?> vp = VirtualProperty.create(String.class).message("Test caption")
				.messageCode("test.message");
		assertEquals(String.class, vp.getType());
		assertEquals("Test caption", ((Localizable) vp).getMessage());
		assertEquals("test.message", ((Localizable) vp).getMessageCode());

		PropertyValueProvider<String> valueProvider = p -> "PROVIDED VALUE";

		vp.valueProvider(valueProvider);

		assertNotNull(vp.getValueProvider());
	}

	@Test
	public void testPropertyConverters() {

		NumericBooleanConverter<Integer> pc = new NumericBooleanConverter<>(int.class);

		PathProperty<Boolean> p = PathProperty.create("test", boolean.class).converter(pc);

		assertNotNull(p.getConverter());

		Boolean cnv = pc.fromModel(null, p);
		assertNotNull(cnv);
		assertEquals(Boolean.FALSE, cnv);

		cnv = pc.fromModel(Integer.valueOf(0), p);
		assertNotNull(cnv);
		assertEquals(Boolean.FALSE, cnv);

		cnv = pc.fromModel(Integer.valueOf(1), p);
		assertNotNull(cnv);
		assertEquals(Boolean.TRUE, cnv);

		Integer mod = pc.toModel(Boolean.FALSE, p);
		assertNotNull(mod);
		assertEquals(new Integer(0), mod);

		mod = pc.toModel(Boolean.TRUE, p);
		assertNotNull(mod);
		assertEquals(new Integer(1), mod);

		NumericBooleanConverter<Long> pc2 = new NumericBooleanConverter<>(Long.class);

		PathProperty<Boolean> p2 = PathProperty.create("test", boolean.class).converter(pc2);

		Long lm = pc2.toModel(Boolean.FALSE, p2);
		assertNotNull(lm);
		assertEquals(new Long(0), lm);

		lm = pc2.toModel(Boolean.TRUE, p2);
		assertNotNull(lm);
		assertEquals(new Long(1), lm);

		p2 = PathProperty.create("test", boolean.class).converter(PropertyValueConverter.numericBoolean(Long.class));

		lm = pc2.toModel(Boolean.FALSE, p2);
		assertNotNull(lm);
		assertEquals(new Long(0), lm);

		lm = pc2.toModel(Boolean.TRUE, p2);
		assertNotNull(lm);
		assertEquals(new Long(1), lm);

		p = PathProperty.create("test", boolean.class).converter(Integer.class, v -> v != null && v > 0,
				v -> v ? 1 : 0);

		assertEquals(new Integer(0), p.getConvertedValue(false));
		assertEquals(new Integer(1), p.getConvertedValue(true));

		@SuppressWarnings("unchecked")
		PropertyBox box = PropertyBox.builder(p).set((Property) p, new Integer(1)).build();
		assertTrue(box.getValue(p));

		// Enums

		final PathProperty<TestEnum> ENMP = PathProperty.create("testenum", TestEnum.class)
				.converter(PropertyValueConverter.enumByOrdinal());

		@SuppressWarnings("unchecked")
		PropertyBox eb = PropertyBox.builder(ENMP).set((Property) ENMP, new Integer(1)).build();
		assertEquals(TestEnum.B, eb.getValue(ENMP));

		assertEquals(new Integer(0), ENMP.getConvertedValue(TestEnum.A));

		final PathProperty<TestEnum> ENMP2 = PathProperty.create("testenum", TestEnum.class)
				.converter(PropertyValueConverter.enumByName());

		@SuppressWarnings("unchecked")
		PropertyBox eb2 = PropertyBox.builder(ENMP2).set((Property) ENMP2, "C").build();
		assertEquals(TestEnum.C, eb2.getValue(ENMP2));

		assertEquals("A", ENMP2.getConvertedValue(TestEnum.A));
	}

	@Test
	public void testPropertyConverterErrors() {
		TestUtils.expectedException(PropertyConversionException.class, new Runnable() {

			@Override
			public void run() {
				NumericBooleanConverter<InvalidNumberClass> pc = new NumericBooleanConverter<>(
						InvalidNumberClass.class);
				pc.toModel(Boolean.TRUE, null);
			}
		});
	}

	private final static Property<String> P1 = PathProperty.create("test1", String.class);
	private final static Property<Integer> P2 = PathProperty.create("test2", Integer.class);
	private final static Property<Boolean> P3 = PathProperty.create("test3", boolean.class)
			.converter(PropertyValueConverter.numericBoolean(Long.class));
	private final static Property<Double> P4 = PathProperty.create("test4", Double.class)
			.configuration(StringValuePresenter.DECIMAL_POSITIONS, 2);
	private final static Property<TestEnum> P5 = PathProperty.create("test5", TestEnum.class);
	private final static Property<TestEnum2> P6 = PathProperty.create("test6", TestEnum2.class);
	private final static Property<Date> P7 = PathProperty.create("test7", Date.class)
			.temporalType(TemporalType.DATE_TIME);
	private final static Property<LocalDate> P8 = PathProperty.create("test8", LocalDate.class);
	private final static Property<LocalTime> P9 = PathProperty.create("test9", LocalTime.class);
	private final static Property<LocalDateTime> P10 = PathProperty.create("test10", LocalDateTime.class);
	private final static Property<String[]> P11 = PathProperty.create("test11", String[].class);
	private final static Property<Double[]> P12 = PathProperty.create("test12", Double[].class);
	private final static Property<String> P13 = VirtualProperty.create(String.class, p -> "VRT:" + p.getValue(P1));
	private final static Property<TestCaptionable> P14 = PathProperty.create("test14", TestCaptionable.class);
	private final static Property<Integer> P15 = PathProperty.create("test2", Integer.class)
			.configuration(StringValuePresenter.DISABLE_GROUPING, true);

	@Test
	public void testPropertyValuePresenter() {

		assertEquals("str", P1.present("str"));

		Context.get().executeThreadBound(LocalizationContext.CONTEXT_KEY,
				LocalizationContext.builder().withInitialLocale(Locale.ITALY)
						.withDefaultBooleanLocalization(Boolean.TRUE, Localizable.builder().message("isTrue").build())
						.withDefaultBooleanLocalization(Boolean.FALSE, Localizable.builder().message("isFalse").build())
						.build(),
				() -> {
					assertEquals("7", P2.present(7));
					assertEquals("1.300", P2.present(1300));

					assertEquals("3.500,00", P4.present(3500d));
					assertEquals("3.500,42", P4.present(3500.42d));
					assertEquals("3.500,01", P4.present(3500.007d));

					assertEquals("isTrue", P3.present(Boolean.TRUE));
					assertEquals("isFalse", P3.present(Boolean.FALSE));
					assertEquals("isFalse", P3.present(null));
				});

		assertEquals("B", P5.present(TestEnum.B));

		assertEquals("valueA", P6.present(TestEnum2.A));

		final Calendar c = Calendar.getInstance(Locale.ITALIAN);
		c.set(Calendar.DAY_OF_MONTH, 9);
		c.set(Calendar.MONTH, 2);
		c.set(Calendar.YEAR, 1979);
		c.set(Calendar.HOUR_OF_DAY, 18);
		c.set(Calendar.MINUTE, 30);
		c.set(Calendar.SECOND, 15);
		c.set(Calendar.MILLISECOND, 0);

		Context.get().executeThreadBound(LocalizationContext.CONTEXT_KEY,
				LocalizationContext.builder().withInitialLocale(Locale.ITALY).build(), () -> {

					assertEquals("09/03/79 18.30", P7.present(c.getTime()));

					final LocalDate date = LocalDate.of(1979, Month.MARCH, 9);
					assertEquals("09/03/79", P8.present(date));

					final LocalTime time = LocalTime.of(18, 30, 15);
					assertEquals("18.30", P9.present(time));

					final LocalDateTime dt = LocalDateTime.of(1979, Month.MARCH, 9, 18, 30, 15);
					assertEquals("09/03/79 18.30", P10.present(dt));

				});

		String[] sa = new String[] { "a", "b", "c" };

		assertEquals("a;b;c", P11.present(sa));

		assertEquals("TestCaptionableCaption", P14.present(new TestCaptionable()));

		PropertyBox box = PropertyBox.builder(P1, P2, P13).set(P1, "p1").set(P2, 2).build();

		assertEquals("VRT:p1", P13.present(box.getValue(P13)));

		assertEquals("1300", P15.present(1300));

		assertEquals("35;57,8", P12.present(new Double[] { 35d, 57.8d }));

		assertEquals("p1", box.present(P1));
	}

	@Test
	public void testPropertyValuePresenterRegistry() {

		PropertyValuePresenterRegistry registry = PropertyValuePresenterRegistry.create(true);

		registry.register(p -> p.getConfiguration().hasNotNullParameter("testpar"), (p, v) -> "TEST_PRS");

		final PathProperty<Integer> prp = PathProperty.create("test", Integer.class).configuration("testpar", "x");

		assertEquals("1", prp.present(1));

		assertEquals("TEST_PRS", Context.get().executeThreadBound(PropertyValuePresenterRegistry.CONTEXT_KEY, registry,
				() -> prp.present(1)));

	}

	@Test
	public void testPropertyMessages() {
		PathProperty<String> qp = PathProperty.create("test", String.class);
		assertEquals(String.class, qp.getType());
		assertEquals("test", qp.getName());

		qp = PathProperty.create("test", String.class).message("Test caption").messageCode("test.message");
		assertEquals(String.class, qp.getType());
		assertEquals("test", qp.getName());

		assertThat(qp, instanceOf(Localizable.class));
		assertEquals("Test caption", ((Localizable) qp).getMessage());
		assertEquals("test.message", ((Localizable) qp).getMessageCode());
	}

	@SuppressWarnings("serial")
	@Test
	public void testPropertyBox() {
		TestBean test = new TestBean();
		test.setName("Test");
		test.setSequence(1);

		PropertyBox box = BeanIntrospector.get()
				.read(PropertyBox.builder(TestPropertySet.NAME, TestPropertySet.SEQUENCE).build(), test);

		Object value = box.getValue(TestPropertySet.NAME);
		assertEquals("Test", value);
		assertEquals(TestPropertySet.NAME.getType(), value.getClass());

		Integer sv = box.getValue(TestPropertySet.SEQUENCE);
		assertEquals(new Integer(1), sv);
		assertEquals(TestPropertySet.SEQUENCE.getType(), sv.getClass());

		PropertyBox pb = PropertyBox
				.builder(TestPropertySet.NAME, TestPropertySet.SEQUENCE, TestPropertySet.NESTED_DATE).build();

		assertTrue(pb.contains(TestPropertySet.NAME));

		pb.setValue(TestPropertySet.NAME, "test");
		assertTrue(pb.containsValue(TestPropertySet.NAME));
		assertFalse(pb.containsValue(TestPropertySet.SEQUENCE));
		assertFalse(pb.containsValue(TestPropertySet.NESTED_DATE));
		assertEquals("test", pb.getValue(TestPropertySet.NAME));

		TestUtils.expectedException(IllegalArgumentException.class, new Runnable() {

			@Override
			public void run() {
				pb.containsValue(null);
			}
		});

		TestUtils.expectedException(IllegalArgumentException.class, new Runnable() {

			@Override
			public void run() {
				pb.getValue(null);
			}
		});

		final PathProperty<Integer> cp = PathProperty.create("testConv", Integer.class)
				.converter(new PropertyValueConverter<Integer, String>() {

					@SuppressWarnings("boxing")
					@Override
					public Integer fromModel(String value, Property<Integer> property)
							throws PropertyConversionException {
						if (value != null) {
							return Integer.parseInt(value);
						}
						return null;
					}

					@Override
					public String toModel(Integer value, Property<Integer> property)
							throws PropertyConversionException {
						if (value != null) {
							return value.toString();
						}
						return null;
					}

					/*
					 * (non-Javadoc)
					 * @see com.holonplatform.core.property.PropertyValueConverter#getPropertyType()
					 */
					@Override
					public Class<Integer> getPropertyType() {
						return Integer.class;
					}

					@Override
					public Class<String> getModelType() {
						return String.class;
					}
				});

		final VirtualProperty<String> vp = VirtualProperty.create(String.class)
				.valueProvider(p -> "PROVIDED:" + p.getValue(TestPropertySet.NAME));

		final VirtualProperty<String> evp = VirtualProperty.create(String.class).valueProvider(p -> {
			throw new PropertyReadException(null, "test error");
		});

		final PropertyBox pb2 = PropertyBox.builder(TestPropertySet.NAME, cp, vp, evp).set(TestPropertySet.NAME, "test")
				.set(cp, Integer.valueOf(1)).build();

		testSetValueUsingConverter(pb2, cp, "1");

		assertEquals("test", pb2.getValue(TestPropertySet.NAME));
		assertEquals(new Integer(1), pb2.getValue(cp));
		assertEquals("1", cp.getConvertedValue(pb2.getValue(cp)));
		assertEquals("PROVIDED:test", pb2.getValue(vp));
		assertTrue(pb2.containsValue(TestPropertySet.NAME));
		assertTrue(pb2.containsValue(vp));

		assertNotNull(pb2.toString());

		pb2.getValue(vp);

		TestUtils.expectedException(IllegalArgumentException.class, new Runnable() {

			@Override
			public void run() {
				pb2.setValue(null, "");
			}
		});
		TestUtils.expectedException(PropertyNotFoundException.class, new Runnable() {

			@Override
			public void run() {
				pb2.setValue(TestPropertySet.NESTED_ID, Long.valueOf(1L));
			}
		});

		Property<String> vrtChain = VirtualProperty.create(String.class).valueProvider(b -> chain(b));

		PropertyBox pb3 = PropertyBox
				.builder(TestPropertySet.NAME, TestPropertySet.SEQUENCE, TestPropertySet.NESTED_DATE, vrtChain).build();

		pb3.setValue(TestPropertySet.NAME, "test");

		String vv = pb3.getValue(vrtChain);
		assertNotNull(vv);
		assertEquals("test-1", vv);

		pb3.setValue(TestPropertySet.SEQUENCE, Integer.valueOf(3));
		vv = pb3.getValue(vrtChain);
		assertNotNull(vv);
		assertEquals("test 3", vv);

		PropertyBox pb4 = PropertyBox.builder(TestPropertySet.PROPERTIES).set(TestPropertySet.NAME, "test")
				.set(TestPropertySet.SEQUENCE, Integer.valueOf(1)).set(TestPropertySet.GENERIC, Double.valueOf(3d))
				.set(TestPropertySet.NESTED_ID, 5L).build();

		TestBean<Double> tb = BeanIntrospector.get().write(pb4, new TestBean<>());

		assertEquals("test", tb.getName());
		assertEquals(1, tb.getSequence());
		assertEquals(Double.valueOf(3d), tb.getGeneric());
		assertEquals(5L, tb.getNested().getNestedId());

		PropertyBox cloned = pb4.cloneBox();

		assertNotNull(cloned);
		assertEquals("test", cloned.getValue(TestPropertySet.NAME));
		assertEquals(Integer.valueOf(1), cloned.getValue(TestPropertySet.SEQUENCE));
		assertEquals(Double.valueOf(3d), cloned.getValue(TestPropertySet.GENERIC));
		assertEquals(Long.valueOf(5), cloned.getValue(TestPropertySet.NESTED_ID));

		cloned = pb4.cloneBox(PropertySet.of(TestPropertySet.NAME));
		assertNotNull(cloned);
		assertEquals("test", cloned.getValue(TestPropertySet.NAME));
		assertFalse(cloned.containsValue(TestPropertySet.SEQUENCE));

		PropertyBox pb5 = PropertyBox.builder(TestPropertySet.NAME).set(TestPropertySet.NAME, "tn").build();
		assertNotNull(pb5);
		assertEquals("tn", pb5.getValue(TestPropertySet.NAME));

		pb5 = PropertyBox.builder(TestPropertySet.PROPERTIES).set(TestPropertySet.NAME, "tn")
				.set(TestPropertySet.SEQUENCE, 7).build();
		assertNotNull(pb5);
		assertEquals("tn", pb5.getValue(TestPropertySet.NAME));
		assertEquals(Integer.valueOf(7), pb5.getValue(TestPropertySet.SEQUENCE));

		final PropertyBox pbro = PropertyBox.builder(TestPropertySet.PROPERTIES).build();

		TestUtils.expectedException(PropertyReadOnlyException.class,
				() -> pbro.setValue(TestPropertySet.VIRTUAL, "readonly"));

	}

	private static String chain(PropertyBox propertyBox) {
		StringBuilder sb = new StringBuilder();
		sb.append(propertyBox.getValueIfPresent(TestPropertySet.NAME).orElse(""));
		Optional<Integer> sequence = propertyBox.getValueIfPresent(TestPropertySet.SEQUENCE);
		if (sequence.isPresent() && sb.length() > 0)
			sb.append(" ");
		sb.append(sequence.orElse(Integer.valueOf(-1)));
		return sb.length() > 0 ? sb.toString() : null;
	}

	@SuppressWarnings("unchecked")
	private static void testSetValueUsingConverter(PropertyBox box, Property property, Object value) {
		box.setValue(property, value);
	}

	@SuppressWarnings("boxing")
	@Test
	public void testBeanProperty() {

		BeanIntrospector.get().clearCache();
		int cs = ((DefaultBeanIntrospector) BeanIntrospector.get()).getCacheSize();
		assertEquals(0, cs);

		TestUtils.expectedException(IllegalArgumentException.class, new Runnable() {

			@Override
			public void run() {
				BeanIntrospector.get().getPropertySet(null);
			}
		});

		BeanPropertySet<TestBean> beanPropertySet = BeanIntrospector.get().getPropertySet(TestBean.class);

		beanPropertySet = BeanIntrospector.get().getPropertySet(TestBean.class);

		cs = ((DefaultBeanIntrospector) BeanIntrospector.get()).getCacheSize();
		assertEquals(1, cs);

		beanPropertySet = BeanIntrospector.get().getPropertySet(TestBean.class);

		cs = ((DefaultBeanIntrospector) BeanIntrospector.get()).getCacheSize();
		assertEquals(1, cs);

		assertFalse(beanPropertySet.getProperty("none").isPresent());

		assertTrue(beanPropertySet.getProperty("name").isPresent());
		assertTrue(beanPropertySet.getProperty("sequence").isPresent());
		assertTrue(beanPropertySet.getProperty("superText").isPresent());
		assertTrue(beanPropertySet.getProperty("nested").isPresent());
		assertTrue(beanPropertySet.getProperty("nested.nestedId").isPresent());
		assertTrue(beanPropertySet.getProperty("nested.nestedDate").isPresent());

		assertEquals(String.class, beanPropertySet.getProperty("name").get().getType());

		Optional<PathProperty<String>> nbp = beanPropertySet.getProperty("name");
		assertTrue(nbp.isPresent());

		assertEquals(String.class, nbp.get().getType());
		assertEquals("name", nbp.get().getName());

		assertEquals("nameCaption", beanPropertySet.getProperty("name").get().getMessage());
		assertEquals("nameCaptionMessageCode", beanPropertySet.getProperty("name").get().getMessageCode());

		// methods

		Optional<PathProperty<String>> np = beanPropertySet.getProperty("name");
		assertTrue(np.isPresent());
		assertEquals("name", np.get().getName());

		np.get().toString();

		Optional<PathProperty<Long>> np2 = beanPropertySet.getProperty("sequence");
		assertTrue(np2.isPresent());
		assertFalse(np2.get().equals(np.get()));
		assertFalse(np.get().hashCode() == np2.get().hashCode());

		final TestBean testMock = mock(TestBean.class);
		when(testMock.getName()).thenReturn("Test");
		when(testMock.getSequence()).thenReturn(Integer.valueOf(1));

		// read

		Object value = beanPropertySet.read("name", testMock);
		assertEquals("Test", value);
		value = beanPropertySet.read("sequence", testMock);
		assertEquals(1, value);

		value = beanPropertySet.read("nested.nestedId", testMock);
		assertNull(value);

		PathProperty<String> pName = PathProperty.create("name", String.class);
		PathProperty<Integer> pSequence = PathProperty.create("sequence", Integer.class);

		value = beanPropertySet.read(pName.getName(), testMock);
		assertEquals("Test", value);
		value = beanPropertySet.read(pSequence.getName(), testMock);
		assertEquals(1, value);

		TestNested testNested = mock(TestNested.class);
		when(testNested.getNestedId()).thenReturn(2L);
		when(testNested.getNestedDate()).thenReturn(new Date());

		when(testMock.getNested()).thenReturn(testNested);

		Optional<PathProperty<Object>> bp = beanPropertySet.getProperty("nested.nestedId");
		assertTrue(bp.isPresent());
		assertTrue(bp.get().getParent().isPresent());
		assertEquals("nestedId", bp.get().getName());
		assertNotNull(bp.get().getParent());

		bp = beanPropertySet.getProperty("nested.nestedDate");
		assertTrue(bp.isPresent());
		bp.get().toString();

		value = beanPropertySet.read("nested.nestedId", testMock);
		assertNotNull(value);
		assertEquals(2L, value);

		value = beanPropertySet.read("nested.nestedDate", testMock);
		assertNotNull(value);

		bp = beanPropertySet.getProperty("name");
		assertTrue(bp.isPresent());
		value = beanPropertySet.read("name", testMock);
		assertEquals("Test", value);

		// write

		final TestBean testWrite = new TestBean();

		beanPropertySet.write("sequence", 2, testWrite);
		value = beanPropertySet.read("sequence", testWrite);
		assertEquals(2, value);

		beanPropertySet.write("name", "Test write", testWrite);
		value = beanPropertySet.read("name", testWrite);
		assertEquals("Test write", value);

		testWrite.setNested(new TestNested());

		beanPropertySet.write("nested.nestedId", 7L, testWrite);
		value = beanPropertySet.read("nested.nestedId", testWrite);
		assertEquals(7L, value);

		testWrite.setNested(null);

		beanPropertySet.write("nested.nestedId", 9L, testWrite);
		value = beanPropertySet.read("nested.nestedId", testWrite);
		assertEquals(9L, value);

		TestBean tb = new TestBean();

		PropertyBox pb = PropertyBox.builder(PropertySet.of(TestPropertySet.NAME)).set(TestPropertySet.NAME, "tn")
				.build();

		beanPropertySet.write(pb, tb);

		assertEquals("tn", tb.getName());

	}

	@Test
	public void testBeanPropertiesNone() {

		BeanPropertySet<Object> ctx = BeanIntrospector.get().getPropertySet(Object.class);
		assertNotNull(ctx);
		assertEquals(0, ctx.size());

	}

	@SuppressWarnings("boxing")
	@Test
	public void testIgnoreProperty() {

		BeanPropertySet<TestBean> testBeanContext = BeanIntrospector.get().getPropertySet(TestBean.class);
		BeanPropertySet<TestBean2> testBean2Context = BeanIntrospector.get().getPropertySet(TestBean2.class);

		assertFalse(testBeanContext.getProperty("internalToIgnore").isPresent());

		assertFalse(testBean2Context.getProperty("nested").isPresent());
		assertFalse(testBean2Context.getProperty("nested.nestedId").isPresent());
		assertFalse(testBean2Context.getProperty("nested.nestedDate").isPresent());

		Date date = new Date();

		TestBean2 testMock = mock(TestBean2.class);
		when(testMock.getSomeDecimal()).thenReturn(new BigDecimal(2.7));

		TestNested testNested = mock(TestNested.class);
		when(testNested.getNestedId()).thenReturn(2L);
		when(testNested.getNestedDate()).thenReturn(date);

		when(testMock.getNested()).thenReturn(testNested);

		Object value = testBean2Context.read("someDecimal", testMock);
		assertEquals(new BigDecimal(2.7), value);
	}

	@Test
	public void testBeanPropertyBox() {

		Date date = new Date();

		TestBean test = new TestBean();
		test.setName("Test");
		test.setSequence(1);
		TestNested testNested = new TestNested();
		testNested.setNestedId(2L);
		testNested.setNestedDate(date);
		test.setNested(testNested);

		final PropertyBox box = PropertyBox.builder(TestPropertySet.PROPERTIES).build();
		BeanIntrospector.get().read(box, test);

		assertTrue(box.size() > 0);

		Object value = box.getValue(TestPropertySet.NAME);
		assertEquals("Test", value);

		value = box.getValue(TestPropertySet.NESTED_ID);
		assertNotNull(value);
		assertEquals(Long.valueOf(2L), value);

		value = box.getValue(TestPropertySet.NESTED_DATE);
		assertNotNull(value);

		TestUtils.expectedException(PropertyNotFoundException.class, new Runnable() {

			@Override
			public void run() {
				PathProperty<String> px = PathProperty.create("px", String.class);
				box.getValue(px);
			}
		});

	}

	@Test
	public void testPropertySet() {
		assertTrue(TestPropertySet.PROPERTIES.contains(TestPropertySet.NAME));

		assertFalse(TestPropertySet.PROPERTIES.contains(null));

		PropertySet<?> ps = PropertySet.builder().add(TestPropertySet.NAME).build();
		assertTrue(ps.contains(TestPropertySet.NAME));

		ps = PropertySet.of(TestPropertySet.NAME);
		assertTrue(ps.contains(TestPropertySet.NAME));

		@SuppressWarnings("unchecked")
		List<Property> lst = (List<Property>) ps.asList();
		assertNotNull(lst);
		assertEquals(1, lst.size());
		assertEquals(TestPropertySet.NAME, lst.get(0));

		List<Property> col = new ArrayList<>();
		col.add(TestPropertySet.NAME);
		ps = PropertySet.of(col);
		assertTrue(ps.contains(TestPropertySet.NAME));

		final Collection<Property<?>> props = new ArrayList<>();
		props.add(TestPropertySet.NAME);

		ps = PropertySet.of(props);
		assertTrue(ps.contains(TestPropertySet.NAME));

		Iterable<Property<?>> pi = new Iterable<Property<?>>() {

			@Override
			public Iterator<Property<?>> iterator() {
				return props.iterator();
			}
		};

		ps = PropertySet.of(pi);
		assertTrue(ps.contains(TestPropertySet.NAME));

		PropertySet<Property> p1 = PropertySet.of(TestPropertySet.NAME);
		PropertySet<Property> p2 = PropertySet.of(TestPropertySet.SEQUENCE);

		PropertySet<Property> p3 = PropertySet.builder().add(p1).add(TestPropertySet.GENERIC).build();
		assertTrue(p3.contains(TestPropertySet.NAME));
		assertTrue(p3.contains(TestPropertySet.GENERIC));

		p3 = PropertySet.join(p1, p2);
		assertTrue(p3.contains(TestPropertySet.NAME));
		assertTrue(p3.contains(TestPropertySet.SEQUENCE));

		p3 = PropertySet.builder().add(p1).add(TestPropertySet.GENERIC).build();
		assertTrue(p3.contains(TestPropertySet.NAME));
		assertTrue(p3.contains(TestPropertySet.GENERIC));

		PropertySet<Property> p4 = PropertySet.builder().add(p3).remove(TestPropertySet.GENERIC).build();
		assertTrue(p4.contains(TestPropertySet.NAME));
		assertFalse(p4.contains(TestPropertySet.GENERIC));

		Object value = p4.execute(() -> Context.get().resource(PropertySet.CONTEXT_KEY, PropertySet.class)
				.map(p -> p.iterator().next()).orElse(null));
		assertEquals(TestPropertySet.NAME, value);

	}

	@Test
	public void testPathProperty() {
		PathProperty<String> property = PathProperty.create("test", String.class);

		assertNotNull(property.toString());

		assertEquals("test", property.getName());

		PathProperty<String> property2 = PathProperty.create("test", String.class);

		PathProperty<Integer> ap = PathProperty.create("test", int.class).message("Test").messageCode("mc");
		assertTrue(TypeUtils.isInteger(ap.getType()));

		ap.toString();

		// sorts

		QuerySort sort = property.asc();
		assertThat(sort, instanceOf(PathQuerySort.class));
		assertNotNull(((PathQuerySort) sort).getPath());
		assertEquals(SortDirection.ASCENDING, ((PathQuerySort) sort).getDirection());

		sort = property.desc();
		assertThat(sort, instanceOf(PathQuerySort.class));
		assertNotNull(((PathQuerySort) sort).getPath());
		assertEquals(SortDirection.DESCENDING, ((PathQuerySort) sort).getDirection());

		// filters

		QueryFilter flt = property.isNull();
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NULL, ((OperationQueryFilter) flt).getOperator());

		flt = property.isNotNull();
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NOT_NULL, ((OperationQueryFilter) flt).getOperator());

		flt = property.eq("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.EQUAL, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.neq("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NOT_EQUAL, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.gt("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.GREATER_THAN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.goe("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.GREATER_OR_EQUAL, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.lt("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.LESS_THAN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.loe("x");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.LESS_OR_EQUAL, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.contains("x", false);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.MATCH, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.contains("x", true);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.MATCH, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.in("x", "y");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.IN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.nin("x", "y");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NOT_IN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.between("x", "y");
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.BETWEEN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		Collection<String> col = new ArrayList<>();
		col.add("z");

		flt = property.in(col);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.IN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.nin(col);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NOT_IN, ((OperationQueryFilter) flt).getOperator());
		assertTrue(((OperationQueryFilter) flt).getRightOperand().isPresent());

		flt = property.eq(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.EQUAL, ((OperationQueryFilter) flt).getOperator());

		flt = property.neq(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.NOT_EQUAL, ((OperationQueryFilter) flt).getOperator());

		flt = property.gt(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.GREATER_THAN, ((OperationQueryFilter) flt).getOperator());

		flt = property.goe(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.GREATER_OR_EQUAL, ((OperationQueryFilter) flt).getOperator());

		flt = property.lt(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.LESS_THAN, ((OperationQueryFilter) flt).getOperator());

		flt = property.loe(property2);
		assertNotNull(flt);
		assertThat(flt, instanceOf(OperationQueryFilter.class));
		assertNotNull(((OperationQueryFilter) flt).getLeftOperand());
		assertEquals(FilterOperator.LESS_OR_EQUAL, ((OperationQueryFilter) flt).getOperator());

		TestUtils.expectedException(UnsupportedOperationException.class, new Runnable() {

			@Override
			public void run() {
				PathProperty<Integer> ap = PathProperty.create("test", int.class);
				ap.contains("x", false);
			}
		});

		TestUtils.expectedException(UnsupportedOperationException.class, new Runnable() {

			@Override
			public void run() {
				PathProperty<Integer> ap = PathProperty.create("test", int.class);
				ap.contains("x", true);
			}
		});

	}

	@SuppressWarnings("serial")
	private class InvalidNumberClass extends Number {

		@Override
		public int intValue() {
			return 0;
		}

		@Override
		public long longValue() {
			return 0;
		}

		@Override
		public float floatValue() {
			return 0;
		}

		@Override
		public double doubleValue() {
			return 0;
		}

	}

	private static enum TestEnum {
		A, B, C;
	}

	private static enum TestEnum2 implements Localizable {
		A("valueA"), B("valueB"), C("valueC");

		private final String caption;

		private TestEnum2(String caption) {
			this.caption = caption;
		}

		@Override
		public String getMessage() {
			return caption;
		}

		@Override
		public String getMessageCode() {
			return null;
		}
	}

	private static class TestCaptionable implements Localizable {

		@Override
		public String getMessage() {
			return "TestCaptionableCaption";
		}

		@Override
		public String getMessageCode() {
			return null;
		}

	}

}
