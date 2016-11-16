// Copyright 2016 Michel Kraemer
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

package de.undercouch.citeproc;

/**
 * Decorates individual items in citations and bibliographies
 * @author Michel Kraemer
 */
public interface VariableWrapper {
	/**
	 * This method will be called by the citation processor when an item in a
	 * citation or bibliography is about to be rendered. The method may change
	 * the way the item is rendered, for example, by prepending or appending
	 * strings, or by completely replacing the item. The default implementation
	 * of this method always returns <code>prePunct + str + postPunct</code>.
	 * @param params a number of parameters that specify the context in which
	 * rendering happens, the citation item that is currently being rendered,
	 * and additional information.
	 * @param prePunct the text that precedes the item to render
	 * @param str the item to render
	 * @param postPunct the text that follows the item to render
	 * @return the string to be rendered
	 */
	String wrap(VariableWrapperParams params, String prePunct, String str, String postPunct);
}
