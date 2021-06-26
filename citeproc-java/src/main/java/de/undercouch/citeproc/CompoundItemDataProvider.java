package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>Retrieves citation items from a list of other
 * {@link ItemDataProvider}s.</p>
 * <p>Calls each {@link ItemDataProvider} in the order they have been added to
 * the list and returns the first citation item retrieved. Returns
 * <code>null</code> if no provider returned a result.</p>
 * <p>Does not check for duplicate items or item IDs.</p>
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class CompoundItemDataProvider implements ItemDataProvider {
	/**
	 * The list of other providers to query
	 */
	private final List<ItemDataProvider> providers;
	
	/**
	 * Creates a new compound provider
	 * @param providers the list of other providers to query for citation items
	 */
	public CompoundItemDataProvider(List<ItemDataProvider> providers) {
		this.providers = providers;
	}

	@Override
	public CSLItemData retrieveItem(String id) {
		return providers.stream()
			.map(p -> p.retrieveItem(id))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	@Override
	public Collection<String> getIds() {
		List<String> result = new ArrayList<>();
		for (ItemDataProvider p : providers) {
			result.addAll(p.getIds());
		}
		return result;
	}
}
