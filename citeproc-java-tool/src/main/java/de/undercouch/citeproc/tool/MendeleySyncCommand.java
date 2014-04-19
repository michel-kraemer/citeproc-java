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

package de.undercouch.citeproc.tool;

import java.io.PrintWriter;

import de.undercouch.citeproc.helper.tool.InputReader;

/**
 * Synchronize with Mendeley Web
 * @author Michel Kraemer
 */
public class MendeleySyncCommand extends ListCommand implements NeedsSynchronization {
	@Override
	public String getUsageName() {
		return "mendeley sync";
	}
	
	@Override
	public String getUsageDescription() {
		return "Synchronize with Mendeley Web";
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out) {
		//this command does nothing
		return 0;
	}
}
