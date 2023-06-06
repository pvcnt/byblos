# Byblos

[![Build Java](https://github.com/pvcnt/byblos/actions/workflows/build.yaml/badge.svg)](https://github.com/pvcnt/byblos/actions/workflows/build.yaml)

Byblos renders [Prometheus](https://prometheus.io/) queries as PNG graphs.

While most dashboarding tools offer dynamic graphs (where one can easily point and click), Byblos generates PNG images.
Compared to dynamic graphs, those PNG graphs can be easily embedded anywhere where images can be, such as in emails, websites or on-call systems.
They can also easily be downloaded, shared (e.g., via Slack or email), and saved in order to work around data expiration.

![Example chart](docs/node_disk_read_bytes_total.png)

The approach taken here is largely inspired by [Atlas](https://netflix.github.io/atlas-docs/), which in turn was inspired by [RRDtool](https://oss.oetiker.ch/rrdtool/).

## Documentation

Documentation is located under the `docs/` directory.

* [Getting started](docs/getting-started.md)
* [Graph API](docs/graph-api.md)
* [Stack language](docs/stack-language/index.md)

## License

This project is distributed under the Apache 2.0 License.

A large part of the code comes from [the Atlas project](https://github.com/Netflix/atlas) (a time series database from Netflix), also distributed under the Apache 2.0 License.
Code has been ported from Scala to Java, and adapted to work with Prometheus.
