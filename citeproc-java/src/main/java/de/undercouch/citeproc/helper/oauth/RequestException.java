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

package de.undercouch.citeproc.helper.oauth;

import java.io.IOException;

/**
 * This exception is thrown when an HTTP request fails
 * @author Michel Kraemer
 */
public class RequestException extends IOException {
	private static final long serialVersionUID = 3499522813501819757L;

	/**
	 * Constructs a new exception
	 * @see Exception#Exception()
	 */
	public RequestException() {
		//nothing to do here
	}

	/**
	 * Constructs a new exception with a detail message
	 * @param message the detail message
	 * @see Exception#Exception(String)
	 */
	public RequestException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with a specified cause
	 * @param cause the cause
	 * @see Exception#Exception(Throwable)
	 */
	public RequestException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with a specified detail message and cause
	 * @param message the detail message
	 * @param cause the cause
	 * @see Exception#Exception(String, Throwable)
	 */
	public RequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
