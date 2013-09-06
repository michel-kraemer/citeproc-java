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

package $package$;

$if(!noJsonObject)$
import de.undercouch.citeproc.helper.JsonHelper;
import de.undercouch.citeproc.helper.JsonObject;
$endif$

/**
 * $description$
 * @author Michel Kraemer
 */
public class $name$ $if(!noJsonObject)$implements JsonObject$endif$ {
	$requiredProperties:{p | private final $p.type$ $p.normalizedName$;
    }$
	$properties:{p | private final $p.type$ $p.normalizedName$;
	}$
	
	public $name$($trunc(requiredProperties):{p | $p.type$ $p.normalizedName$,}$
			$if(!requiredProperties.empty)$
			$last(requiredProperties).type; format="toEllipse"$ $last(requiredProperties).normalizedName$
			$endif$) {
		$requiredProperties:{p | this.$p.normalizedName$ = $p.normalizedName$;
        }$
		$properties:{p | this.$p.normalizedName$ = $if(p.default)$$p.default$$else$null$endif$;
		}$
	}
	
	$if(!properties.empty)$
	public $name$($requiredProperties:{p | $p.type$ $p.normalizedName$,}$
			$properties:{p | $p.type$ $p.normalizedName$};separator=","$) {
		$requiredProperties:{p | this.$p.normalizedName$ = $p.normalizedName$;
        }$
		$properties:{p | this.$p.normalizedName$ = $p.normalizedName$;
	    }$
	}
	$endif$
	
	$requiredProperties:{p | /**
	 * @return the $if(shortname)$$shortname$'s $endif$$p.name$
	 */
	public $p.type$ $p.normalizedName; format="toGetter"$() {
		return $p.normalizedName$;
	\}
	}$
	
	$properties:{p | /**
	 * @return the $if(!shortname.empty)$$shortname$'s $endif$$p.name$
	 */
	public $p.type$ $p.normalizedName; format="toGetter"$() {
		return $p.normalizedName$;
	\}
	}$

	$if(!noJsonObject)$
	@Override
	public String toJson() {
		StringBuilder r = new StringBuilder("{");
		$requiredProperties:{p | r.append("\"$p.name$\": " + JsonHelper.toJson($p.normalizedName$));
		};separator="r.append(\",\");"$
		$properties:{p | if ($p.normalizedName$ != null) {
			$if(requiredProperties.empty)$
			if (r.length() > 1) r.append(",");
			$endif$
			r.append("$if(!requiredProperties.empty)$,$endif$\"$p.name$\": " + JsonHelper.toJson($p.normalizedName$));
		\}
		}$
		r.append("}");
		return r.toString();
	}
	
	@Override
	public String toString() {
		return toJson();
	}
	$endif$
}
