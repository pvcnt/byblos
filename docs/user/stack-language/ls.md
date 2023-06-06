# ls

| Input stack | Output stack |
|-------------|--------------|
| `TimeSeriesExpr,String,:ls` | `StyleExpr` |

Set the line style.
The value should be one of:

* `line`: draw a normal line. This is the default.
* `area`: fill in the space between the line value and 0 on the Y-axis.
* `stack`: stack the filled area on to the previous stacked lines on the same axis.
* `vspan`: non-zero datapoints will be drawn as a vertical span.