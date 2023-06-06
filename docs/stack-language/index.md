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
It is well adapted to the [graph API](../graph-api.md) which is designed around GET calls.

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

Any double value that is left on the stack will get implicitly converted to [a constant line](const.md).
Any string value that is left on the stack will get implicitly converted to [a query](query.ùd).

## Variables

Variables can be used to store and retrieve any item in the stack.
The following table contains a list of well-known variables:

| Name | Description |
|------|-------------|
| `tz` | Timezone to use |

## Vocabulary

### Stack manipulation

* [:2over](2over.md)
* [:-rot](-rot.md)
* [:clear](clear.md)
* [:depth](depth.md)
* [:drop](drop.md)
* [:dup](dup.md)
* [:get](get.md)
* [:ndrop](ndrop.md)
* [:over](over.md)
* [:pick](pick.md)
* [:set](set.md)
* [:swap](swap.md)

### Data

* [:const](const.md)
* [:query](query.md)

### Styling

* [:alpha](alpha.md)
* [:axis](axis.md)
* [:color](color.md)
* [:legend](legend.md)
* [:limit](limit.md)
* [:ls](ls.md)
* [:lw](lw.md)
* [:order](order.md)
* [:palette](palette.md)
* [:sort](sort.md)