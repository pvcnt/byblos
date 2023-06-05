# Stack language

Stack language is a way of specifying Prometheus queries to plot on a graph.
It is adapted from [Atlas Stack Language](https://netflix.github.io/atlas-docs/asl/tutorial/), which is itself based on [RPN expressions](https://oss.oetiker.ch/rrdtool/doc/rrdgraph_rpn.en.html) supported by [RRDtool](https://oss.oetiker.ch/rrdtool/).
Here is an example of a stack language expression:

```
node_disk_read_bytes_total{node=app1},#0000FF,:color,42
```

This pushes on the stack a PromQL query, applies some styling on it (draw the line in blue), and pushes a constant value.

## Goals and non-goals

Stack language is intended to be compact and URL-friendly.
It is well adapted to the [graph API](graph-api.md) which is designed around GET calls.

Stack language is an extension of PromQL, i.e., any valid PromQL expression is also a valid stack language expression.
Moreover, is it by design organised around a syntax that does not look like PromQL, i.e., it does not try to add new primitives to PromQL but rather allows to combine or enrich PromQL expressions with another language.

Stack language sticks to adding presentation features to PromQL.
It does not try to add new capabilies around time series manipulation.
Said otherwise, data processing is done by Prometheus servers and not by Byblos.

## Stack structure

Elements to push on the stack are separated by a comma.
For example `a` defines a stack with a single element `"a"`, while `a,b` defines a stack with two elements `"a"` and `"b"`.

Commas nested inside brackets (`[]`), curly brackets (`{}`) or parens (`()`) are ignored.
For example, `metric{app=app1,status=200}` defines a stack with a single element, ignoring the comma inside the selector.

Elements starting with a colon (`:`) are words, which define commands that manipulate the stack by evaluating, replacing, adding or removing elements.
For example, `a,:dup` defines a stack that is equivalent to `a,a` once evaluated.

Parenthesis are used to define lists.
Lists always contain litteral, i.e., commands are not executed.
For example, `(,a,:dup,)` defines a stack with a single element, which is the list `["a", ":dup"]`.

Any double value that is left on the stack will get implicitly converted to [a constant line](#const).
Any string value that is left on the stack will get implicitly converted to [a query](#query).

## Vocabulary

This section explain words that are defined in the stack language.

### query

| Input stack | Output stack |
| `String,:query` | `TimeSeriesExpr` |

Generate a time series that is the result of PromQL expression.

### const

| Input stack | Output stack |
| `Float,:const` | `TimeSeriesExpr` |

Generate a line where each datapoint is a constant value.

### clear

| Input stack | Output stack |
| `...,:clear` | `` |

Remove all items from the stack.

### dup

| Input stack | Output stack |
| `Any,:clear` | `Any,Any` |

Duplicate the item on the top of the stack.

### drop

| Input stack | Output stack |
| `Any,:clear` | `` |

Remove the item on the top of the stack.

### get

| Input stack | Output stack |
| `String,:get` | `Any` |

Get the value of a variable and push it on the stack.

### set

| Input stack | Output stack |
| `String,Any,:set` | `` |

Set the value of a variable.

### alpha

| Input stack | Output stack |
| `TimeSeriesExpr,String,:alpha` | `StyleExpr` |

Set the alpha value for the colors on the line.
The value should be a two digit hex number where 00 is transparent and ff is opague.
This setting will be ignored if the [color](#color) setting is used for the same line.

### color

| Input stack | Output stack |
| `TimeSeriesExpr,String,:color` | `StyleExpr` |

Set the color for the line.
The value should be one of:

* Hex triplet, e.g. f00 is red.
* 6 digit hex RBG, e.g. ff0000 is red.
* 8 digit hex ARGB, e.g. ffff0000 is red. The first byte is the [alpha](#alpha) setting to use with the color.

### palette

| Input stack | Output stack |
| `TimeSeriesExpr,String,:palette` | `StyleExpr` |

Set the palette to use for the results of an expression.
This operator is allows for scoping a palette to a particular query instead of to all lines that share the same axis.

### ls

| Input stack | Output stack |
| `TimeSeriesExpr,String,:ls` | `StyleExpr` |

Set the line style.
The value should be one of:

* `line`: this is the default, draws a normal line.
* `area`: fill in the space between the line value and 0 on the Y-axis.
* `stack`: stack the filled area on to the previous stacked lines on the same axis.
* `vspan`: non-zero datapoints will be drawn as a vertical span.

### lw

| Input stack | Output stack |
| `TimeSeriesExpr,Integer,:lw` | `StyleExpr` |

The width of the stroke used when drawing the line.

### legend

| Input stack | Output stack |
| `TimeSeriesExpr,String,:legend` | `StyleExpr` |

Set the legend text.
Legends can contain variables based on the labels returned by Prometheus.
Variables start with a `$` sign and can optionally be enclosed between parentheses.
The parentheses are required for cases where the characters immediately following the name could be a part of the name.
If a variable is not defined, then the name of the variable will be used as the substitution value.

### axis

| Input stack | Output stack |
| `TimeSeriesExpr,Integer,:axis` | `StyleExpr` |

Specify which Y-axis to use for the line. The value specified is the axis number and should be an integer in the range 0 to 4 inclusive.

### limit

| Input stack | Output stack |
| `TimeSeriesExpr,Integer,:limit` | `StyleExpr` |

Restrict the output to the first specified number of lines from the input expression.
The lines will be chosen in order based on the [sort](#sort) and [order](#order) used.

### sort

| Input stack | Output stack |
| `TimeSeriesExpr,String,:sort` | `StyleExpr` |

Sort the results of an expression in the legend by one of the summary statistics or by the legend text.
The default behavior is to sort by the legend text.
Valid statistic values are `avg`, `count`, `max`, `min`, `last`, and `total`.
This will sort in ascending order by default, for descending order use [order](#order).

### order

| Input stack | Output stack |
| `TimeSeriesExpr,String,:order` | `StyleExpr` |

Order to use for [sorting](#sort) results.
Supported values are `asc` and `desc` for ascending and descending order respectively.
Default is `asc`.

