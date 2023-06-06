# sort

| Input stack | Output stack |
|-------------|--------------|
| `TimeSeriesExpr,String,:sort` | `StyleExpr` |

Sort the results of an expression in the legend by one of the summary statistics or by the legend text.
The default behavior is to sort by the legend text.
Valid statistic values are `avg`, `count`, `max`, `min`, `last`, and `total`.
This will sort in ascending order by default, for descending order use [order](order.md).