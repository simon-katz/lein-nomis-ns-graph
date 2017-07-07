

FIXME


[![Clojars Project](https://img.shields.io/clojars/v/lein-ns-dep-graph.svg)](https://clojars.org/lein-ns-dep-graph)

# lein-nomis-ns-graph

This is a Leiningen plugin to show the namespace dependencies of Clojure project
sources as a graph.

Inspired by the following:

- https://github.com/hilverd/lein-ns-dep-graph
- https://github.com/alexander-yakushev/ns-graph

## Added Features

For a custom named file add this -name <file-name-without-extension> to the command line run like this:

    lein nomis-ns-graph -name my-graph

## Acknowledgements

The plugin itself is tiny, all the hard work is done by
[clojure.tools.namespace](https://github.com/clojure/tools.namespace) and
[Rhizome](https://github.com/ztellman/rhizome).

## Requirements

You will need to have [Graphviz](http://www.graphviz.org/) installed. Run `dot
-V` at the command line to check.

## Installation and Usage

Put `[lein-nomis-ns-graph "x.x.x"]` into the `:plugins` vector of your
`:user` profile. Then run

    lein nomis-ns-graph

from a Clojure project directory. This outputs a file `ns-nomis-graph.png` showing
the internal namespace dependencies of the project's `.clj` sources.
Dependencies on external namespaces, say `clojure.java.io`, are not shown.

You can also pass an optional platform argument to generate a graph for ClojureScript

    lein nomis-ns-graph :cljs # or
    lein nomis-ns-graph :clj


## Examples



## License

Copyright © 2017 Simon Katz

Distributed under the Eclipse Public License, the same as Clojure.
