# Configuration

Configuration files are written in [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md), which is a hierarchical configuration format.
Additional configuration files may be passed as arguments to Byblos when starting it.

This page details the most relevant configuration parameters that an administrator may find useful to override.

## Time series backend

| Key                            | Type | Description                                              | Default                                     |
|--------------------------------|------|----------------------------------------------------------|---------------------------------------------|
| `byblos.eval.backend.class`    | string | Backend implementation to use                            | `dev.byblos.eval.backend.PrometheusBackend` |
| `byblos.eval.backend.endpoint` | string | Prometheus instance to use                               | `https://demo.promlabs.com`                 |
| `byblos.eval.backend.auth`     | string | Authentication scheme to use (`basic`, `bearer`, `none`) | `none`                                      |
| `byblos.eval.backend.username` | string | Username for `basic` authentication scheme               | -                                           |
| `byblos.eval.backend.password` | string | Password for `basic` authentication scheme              | -                                           |
| `byblos.eval.backend.token`    | string | Token for `bearer` authentication scheme                 | -                                           |

## Graph rendering

| Key | Type | Description | Default |
|-----|------|-------------|---------|
| `byblos.eval.step` | duration | Scrap interval for time series | `1 minute` |
| `byblos.eval.graph.max-datapoints` | integer | Maximum number of points to display on a single plot | `1440` |
| `byblos.eval.graph.png-metadata-enabled` | boolean | Whether to encode the graph uri will be encoded as a Source iTXt field in the generated image | `false` |
| `byblos.eval.graph.browser-agent-pattern` | string | Pattern to use to detect that a user-agent is a Web browser | `mozilla|msie|gecko|chrome|opera|webkit` |

## Graph defaults

The default value of several query parameters of the [Graph API](../user/graph-api.md) can be overriden by configuration.

| Key | Query parameter |
|-----|-----------------|
| `byblos.eval.graph.start-time` | `s` |
| `byblos.eval.graph.end-time` | `e` |
| `byblos.eval.graph.timezone` | `tz`  |
| `byblos.eval.graph.width` | `w` |
| `byblos.eval.graph.height` | `h` |
| `byblos.eval.graph.theme` | `theme` |
| `byblos.eval.graph.light.palette` | `palette` when `theme=light` |
| `byblos.eval.graph.dark.palette` | `palette` when `theme=dark` |

## Security

Configuration parameters related to security are defined in [the dedicated page](security.md). 