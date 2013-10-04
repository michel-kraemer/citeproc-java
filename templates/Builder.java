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

import java.util.Map;

/**
 * Builder for {@link $name$}
 * @author Michel Kraemer
 */
public class $name$Builder {
	$requiredProperties:{p | private $p.type$ $p.normalizedName$;
	}$
	$properties:{p | private $p.type$ $p.normalizedName$;
	}$
	
	public $name$Builder($trunc(requiredProperties):{p | $p.type$ $p.normalizedName$,}$
			$if(!requiredProperties.empty)$
			$last(requiredProperties).type; format="toEllipse"$ $last(requiredProperties).normalizedName$
			$endif$) {
		$requiredProperties:{p | this.$p.normalizedName$ = $p.normalizedName$;
		}$
		$properties:{p | this.$p.normalizedName$ = $if(p.default)$$p.default$$else$null$endif$;
		}$
	}
	
	$properties:{p | public $name$Builder $p.normalizedName$($p.type; format="toEllipse"$ $p.normalizedName$) {
		this.$p.normalizedName$ = $p.normalizedName$;
		return this;
	\}
	}$
	
	public $name$ build() {
		return new $name$($requiredProperties:{p | $p.normalizedName$}; separator=","$
				$if(!requiredProperties.empty && !properties.empty)$,$endif$
				$properties:{p | $p.normalizedName$}; separator=","$);
	}
	
	$additionalBuilderMethods; separator="\n"$
}
