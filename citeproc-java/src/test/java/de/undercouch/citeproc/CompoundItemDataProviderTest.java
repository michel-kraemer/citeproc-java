package de.undercouch.citeproc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLItemDataBuilder;

/**
 * Tests for {@link CompoundItemDataProvider}
 * @author Michel Kraemer
 */
public class CompoundItemDataProviderTest {
	private final ListItemDataProvider l1 = new ListItemDataProvider(
			new CSLItemDataBuilder().id("A").title("The A").build(),
			new CSLItemDataBuilder().id("B").title("The B").build());

	private final ListItemDataProvider l2 = new ListItemDataProvider(
			new CSLItemDataBuilder().id("C").title("The C").build(),
			new CSLItemDataBuilder().id("D").title("The D").build(),
			new CSLItemDataBuilder().id("E").title("The E").build());

	/**
	 * Test if the compound provider can handle an empty list
	 */
	@Test
	public void empty() {
		CompoundItemDataProvider p = new CompoundItemDataProvider(
				Collections.emptyList());
		assertArrayEquals(new String[0], p.getIds());
		assertNull(p.retrieveItem("ID"));
		assertNull(p.retrieveItem(""));
		assertNull(p.retrieveItem(null));
	}

	/**
	 * Test if the compound provider can handle a list with one
	 * {@link ListItemDataProvider}
	 */
	@Test
	public void oneItemList() {
		CompoundItemDataProvider p = new CompoundItemDataProvider(
				Arrays.asList(l1));
		assertArrayEquals(l1.getIds(), p.getIds());
		assertNull(p.retrieveItem("ID"));
		assertNull(p.retrieveItem(""));
		assertNull(p.retrieveItem(null));
		assertEquals(l1.retrieveItem("A"), p.retrieveItem("A"));
		assertEquals(l1.retrieveItem("B"), p.retrieveItem("B"));
		assertNull(p.retrieveItem("C"));
		assertNull(p.retrieveItem("D"));
		assertNull(p.retrieveItem("E"));
	}

	/**
	 * Test if the compound provider can handle a list with two
	 * {@link ListItemDataProvider}s
	 */
	@Test
	public void twoItemList() {
		CompoundItemDataProvider p = new CompoundItemDataProvider(
				Arrays.asList(l1, l2));
		ArrayList<String> a = new ArrayList<>();
		a.addAll(Arrays.asList(l1.getIds()));
		a.addAll(Arrays.asList(l2.getIds()));
		assertEquals(a, Arrays.asList(p.getIds()));
		assertNull(p.retrieveItem("ID"));
		assertNull(p.retrieveItem(""));
		assertNull(p.retrieveItem(null));
		assertEquals(l1.retrieveItem("A"), p.retrieveItem("A"));
		assertEquals(l1.retrieveItem("B"), p.retrieveItem("B"));
		assertEquals(l2.retrieveItem("C"), p.retrieveItem("C"));
		assertEquals(l2.retrieveItem("D"), p.retrieveItem("D"));
		assertEquals(l2.retrieveItem("E"), p.retrieveItem("E"));
	}
}
