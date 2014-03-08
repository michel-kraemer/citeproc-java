// Copyright 2014 Michel Kraemer
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

package de.undercouch.citeproc.helper.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads user input from standard input
 * @author Michel Kraemer
 */
public class StandardInputReader implements InputReader {
	@Override
	public String readLine(String prompt) throws IOException {
		System.out.print(prompt);
		return new BufferedReader(new InputStreamReader(System.in)).readLine();
	}
}
