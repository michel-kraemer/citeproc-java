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

package de.undercouch.citeproc.lint;

/**
 * A default implementation of {@link LintListener}. The methods in this class
 * are empty and can be overridden for more specific listener implementations.
 * @author Michel Kraemer
 * @since 1.1.0
 */
public abstract class LintListenerAdapter implements LintListener {
	@Override
	public void onStart(LintEvent e) {
		// nothing to do here
	}

	@Override
	public void onEnd(LintEvent e) {
		// nothing to do here
	}

	@Override
	public void onStartItem(LintItemEvent e) {
		// nothing to do here
	}

	@Override
	public void onEndItem(LintItemEvent e) {
		// nothing to do here
	}

	@Override
	public void onError(LintErrorEvent e) {
		// nothing to do here
	}
}
