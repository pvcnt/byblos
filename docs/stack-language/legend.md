# legend

| Input stack | Output stack |
|-------------|--------------|
| `TimeSeriesExpr,String,:legend` | `StyleExpr` |

Set the legend text.
Legends can contain variables based on the labels returned by Prometheus.
Variables start with a `$` sign and can optionally be enclosed between parentheses.
The parentheses are required for cases where the characters immediately following the name could be a part of the name.
If a variable is not defined, then the name of the variable will be used as the substitution value.
