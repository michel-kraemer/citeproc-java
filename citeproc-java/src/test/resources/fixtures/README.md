# citeproc-java test fixtures

This directory contains test fixtures used by the `FixturesTest` unit test to
make sure the CSL processor produces the desired result for the specified input
data.

## File format

Each test fixtures is a YAML file with the following attributes:

| Name               | Description                                           | Possible values                                                                   | Required | Default |
|--------------------|-------------------------------------------------------|-----------------------------------------------------------------------------------|--------- |---------|
| `mode`             | Specifies what type of output to generate             | `bibliography`, `citation`                                                        | yes      | &ndash; |
| `experimentalMode` | Specifies if the test should run in experimental mode | `only` (test runs only in experimental mode), `true` (test runs in both modes)    | no       | `true`  |
| `style`            | A citation style                                      | Either CSL XML or a string specifying the name of one of the provided CSL styles. | yes      | &ndash; |
| `bibliographyFile` | A name of a bibliography file to load (conflicts with `items`)   | A name of a file in the classpath  | yes (if `items` is not specified)            | &ndash; |
| `items`            | A list of citation item data (conflicts with `bibliographyFile`) | Array of `CSLItemData` objects     | yes (if `bibliographyFile` is not specified) | &ndash; |
| `itemIds`          | A list of citation item IDs to include. Can be used to select only certain items from `items` or `bibliographyFile` | An array of strings | no       | &ndash;  |
| `citations`        | A list of citations. Can only be specified if `mode` is `citation`. If not specified, citations for all `items` will be generated. | Array of `CSLCitation` objects | no | &ndash; |
| `result`           | The rendered result | Either a text string or an object with keys specifying the output format and values specifying the respective rendered results | yes | &ndash; |
| `resultLegacy`     | Similar to `result` but will only be used in legacy (= non-experimental) mode | See `result`                                           | no       | `result` |
