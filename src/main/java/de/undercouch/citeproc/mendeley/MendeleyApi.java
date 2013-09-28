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

package de.undercouch.citeproc.mendeley;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.model.Verb;

/**
 * Mendeley OAuth API
 * @author Michel Kraemer
 */
public class MendeleyApi extends DefaultApi10a {
	@Override
	public String getRequestTokenEndpoint() {
		return "http://api.mendeley.com/oauth/request_token/";
	}

	@Override
	public String getAccessTokenEndpoint() {
		return "http://api.mendeley.com/oauth/access_token/";
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return "http://api.mendeley.com/oauth/authorize/?oauth_token=" + requestToken.getToken();
	}
	
	@Override
	public Verb getRequestTokenVerb() {
		return Verb.GET;
	}
	
	@Override
	public Verb getAccessTokenVerb() {
		return Verb.GET;
	}
}
