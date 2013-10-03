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
import java.util.Collection;
import java.util.Map;

import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
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
	public Object toJson(JsonBuilder builder) {
		$requiredProperties:{p | builder.add("$p.name$", $p.normalizedName$);
		}$
		$properties:{p | if ($p.normalizedName$ != null) {
			builder.add("$p.name$", $p.normalizedName$);
		\}
		}$
		return builder.build();
	}
	
	/**
	 * Converts a JSON object to a $name$ object. $if(!requiredProperties.empty)$The JSON object must at least contain the following required properties: $requiredProperties:{p | <code>$p.name$</code>}; separator=", "$$endif$
	 * @param obj the JSON object to convert
	 * @return the converted $name$ object
	 */
	@SuppressWarnings("unchecked")
	public static $name$ fromJson(Map<String, Object> obj) {
		$requiredProperties:{p | $p.type$ $p.normalizedName$;
		}$
		
		$requiredProperties:{p | {
			Object v = obj.get("$p.name$");
			if (v == null) {
				throw new IllegalArgumentException("Missing property `$p.name$'");
			\}
			$propertyTemplate(p, "v")$
		\}}$
		
		$name$Builder builder = new $name$Builder($requiredProperties:{p | $p.normalizedName$ }; separator=","$);
		
		$properties:{p | {
			Object v = obj.get("$p.name$");
			if (v != null) {
				$propertyTemplate(p, "v")$
			\}
			$if(p.default)$
			else {
				builder.$p.normalizedName$($p.default$);
			\}
			$endif$
		\}}$
		
		return builder.build();
	}
	$endif$
	
	$additionalMethods; separator="\n"$
}
