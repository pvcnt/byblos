# color

| Input stack | Output stack |
|-------------|--------------|
| `TimeSeriesExpr,String,:color` | `StyleExpr` |

Set the color for the line.
The value should be one of:

* Hex triplet, e.g. f00 is red.
* 6 digit hex RBG, e.g. ff0000 is red.
* 8 digit hex ARGB, e.g. ffff0000 is red. The first byte is the [alpha](alpha.md) setting to use with the color.
