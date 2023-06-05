# Graph API

This page details the contract of the graph API.

## Endpoint

```
GET /api/v1/graph?q=<expr>[&<params>]
```

## Data parameters

The below table details query parameters available to specify which data to display:

| Name | Type | Description | Default |
|------|------|-------------|---------|
| `q`  | string | [Query expression](stack-language.md) | *required* |
| `s`  | [time](time-parameters.md#time) | Start time | `e-3h` |
| `e`  | [time](time-parameters.md#time) | End time | `now` |
| `step` | [duration](time-parameters.md#duration) | Interval between two data points | auto |
| `tz` | [timezone](time-parameters.md#timezone) | Timezone | `US/Pacific` |

**In most cases, you should not use the `step` parameter.**
An appropriate step size will be automatically computed, depending on the specified time window and the image dimensions.

The time zone is used both to interpret relative times (`s` and `e` parameters), and to localise times in the graph.

## Output format

The below table details query parameters available to specify the output format:

| Name     | Type    | Description           | Default |
|----------|---------|-----------------------|---------|
| `format` | string  | Output format to use | `png` |
| `w`      | integer | Width of the image or canvas, in pixels | `700` |
| `h`      | integer | Height of the image or canvas, in pixels | `300` |

Note: Even when using a non-image output format, `w` and `h` parameters are used to automatically compute an appropriate `step`.

## Image flags

The below table details query parameters available to customise the image rendering:

| Name | Type | Description | Default |
|------|------|-------------|---------|
| `title` | string | A title for the graph | none |
| `ylabel` | | A title for the y-axis | none |
| `no_legend` | boolean | Do not display any legend | `0` |
| `no_legend_stats` | boolean | Do not display statistics in the legend | `0` |
| `axis_per_line` | boolean | Use a different Y-axis for every line | `0` |
| `only_graph` | boolean | Display an anonymised graph (no legend, no values) | `0` |
| `vision` | string | Simulate different types of color blindness | `vision` |
| `layout` | string | Control how components are layed out in the canvas | `canvas` |
| `stack` |  boolean | Set the default line style to stack | `0` |
| `l` | string or float | Lower bound for the Y-axis | `auto-style` |
| `u` | string or float | Upper bound for the Y-axis | `auto-style` |
| `palette` | string | Color palette to use | `armytage` |
| `scale` | string | Scale to use for the Y-axis | `linear` |
| `tick_labels` | string | Mode to use for display Y-axis ticks | `decimal` |
| `zoom`   | float   | Apply a zoom factor to the image | `1.0` |

Note: Those parameters do not apply if using a non-image output format (e.g., JSON).

## Legend flags

The below table details query parameters available to customise the legend rendering:

| Name | Type | Description | Default |
|------|------|-------------|---------|
| `sort` | string | Sort mode to use for the legend | `legend` |
| `order` | string | Sort order to use for the legend | `asc` |
