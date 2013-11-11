// Copyright 2013 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Calculates the Levenshtein distance according to the iterative algorithm
 * with two matrices described in
 * <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia</a>
 * which is licensed under "Creative Commons Attribution-ShareAlike 3.0 Unported".
 * @author Michel Kraemer
 */
public class Levenshtein {
	private static class Item<T> implements Comparable<Item<T>> {
		private final T str;
		private final int distance;
		
		public Item(T str, int distance) {
			this.str = str;
			this.distance = distance;
		}
		
		@Override
		public int compareTo(Item<T> o) {
			return (distance < o.distance ? -1 : (distance == o.distance ? 0 : 1));
		}
	}
	
	/**
	 * Calculates the Levenshtein distance between two strings. The Levenshtein
	 * distance is the number of insertions, deletions and substitutions you
	 * have to perform on the first string <code>s</code> in order to get
	 * the second string <code>t</code>. A distance of 0 means that both
	 * strings are equal.
	 * @param s the first string
	 * @param t the second string
	 * @return the Levenshtein distance
	 */
	public static int distance(CharSequence s, CharSequence t) {
		if (s.equals(t)) {
			return 0;
		}
		
		if (s.length() == 0) {
			return t.length();
		}
		
		if (t.length() == 0) {
			return s.length();
		}

		int[] v0 = new int[t.length() + 1];
		int[] v1 = new int[t.length() + 1];

		for (int i = 0; i < v0.length; i++) {
			v0[i] = i;
		}

		for (int i = 0; i < s.length(); i++) {
			v1[0] = i + 1;
			for (int j = 0; j < t.length(); j++) {
				int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
				v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
			}
			for (int j = 0; j < v0.length; j++) {
				v0[j] = v1[j];
			}
		}

		return v1[t.length()];
	}
	
	/**
	 * Searches the given collection of strings and returns the string that
	 * has the lowest Levenshtein distance to a given second string <code>t</code>.
	 * If the collection contains multiple strings with the same distance to
	 * <code>t</code> only the first one will be returned.
	 * @param <T> the type of the strings in the given collection
	 * @param ss the collection to search
	 * @param t the second string
	 * @return the string with the lowest Levenshtein distance
	 */
	public static <T extends CharSequence> T findMinimum(Collection<T> ss, CharSequence t) {
		int min = Integer.MAX_VALUE;
		T result = null;
		for (T s : ss) {
			int d = distance(s, t);
			if (d < min) {
				min = d;
				result = s;
			}
		}
		return result;
	}
	
	/**
	 * Searches the given collection of strings and returns a collection of at
	 * most <code>n</code> strings that have the lowest Levenshtein distance
	 * to a given string <code>t</code>. The returned collection will be
	 * sorted according to the distance with the string with the lowest
	 * distance at the first position.
	 * @param <T> the type of the strings in the given collection
	 * @param ss the collection to search
	 * @param t the string to compare to
	 * @param n the maximum number of strings to return
	 * @return the strings with the lowest Levenshtein distance
	 */
	public static <T extends CharSequence> Collection<T> findMinimum(
			Collection<T> ss, CharSequence t, int n) {
		LinkedList<Item<T>> result = new LinkedList<Item<T>>();
		for (T s : ss) {
			int d = distance(s, t);
			result.offer(new Item<T>(s, d));
			
			if (result.size() > n + 10) {
				//resort, but not too often
				Collections.sort(result);
				while (result.size() > n) result.removeLast();
			}
		}
		
		Collections.sort(result);
		while (result.size() > n) result.removeLast();
		
		List<T> arr = new ArrayList<T>(n);
		for (Item<T> i : result) {
			arr.add(i.str);
		}
		return arr;
	}
}
