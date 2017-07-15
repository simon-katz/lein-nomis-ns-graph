# lein-nomis-ns-graph

A Leiningen plugin that shows a Clojure project's namespace dependencies
as a graph, and also shows the namespace hierarchy.


## Examples

An example namespace graph:

![An example namespace graph](examples/example-nomis-ns-graph-001.png)


## Requirements

* [Graphviz](http://www.graphviz.org/) must be installed. (Run `dot -V` at the command line to check whether you have it.)

## Installation

Add `[lein-nomis-ns-graph "0.11.0"]` to the `:plugins` vector of your `:user`
profile.

## Usage

Run when in a Clojure project directory.

### Basic Usage

To show dependencies between Clojure namespaces:

    lein nomis-ns-graph

This produces a file called `nomis-ns-graph.png` showing namespace dependencies
within the project's `.clj` sources.


### Specifying an Output File

To specify a different output file:

    lein nomis-ns-graph :filename foo

This will produce a file named `foo.png` instead of the default
`nomis-ns-graph.png`.

### ClojureScript

To show dependencies between ClojureScript namespaces (assuming ClojureScript
source is in either `src/cljs` or `cljs/src`):

    lein nomis-ns-graph :platform cljs

The default is `:platform clj`.

### Source Paths

To specify source paths (useful if the defaults for ClojureScript are wrong
for your project):

    lein nomis-ns-graph :source-paths "a/b c/d e/f"

The separator for the source paths can be a vertical bar (|). I found I
needed this when using Git Bash on Windows -- space didn't work. (Strange.)

The defaults are the `:source-paths` in the project definition.

### Non-Project Dependencies

To show one level of dependencies going to namespaces outside of your project:

    lein nomis-ns-graph :show-non-project-deps

### Exclusions

To exclude namespaces:

    lein nomis-ns-graph :exclusions "user timbre"

Namespaces are not shown if they start with any of the supplied strings.

As for source paths, the separator for the source paths can be a vertical bar
(|).

## New Features

The features documented in this section are not yet released.

They may or may not be in the latest snapshot version.
(You could check https://clojars.org/lein-nomis-ns-graph for the latest
snapshot version.)

### Writing a .gv File

To write a `.gv` file:

    lein nomis-ns-graph :write-gv-file?

lein-nomis-ns-graph uses Graphviz under the covers.

Before the `.png` file is produced, an intermediate Graphviz dot file format
version of the namespace graph is produced.

If this option is provided, lein-nomis-ns-graph writes the intermediate version
to a file. The file has the same base name as the `.png` file, but a `.gv`
extension.

The Graphviz dot file format is a text format, so this is useful when developing
and debugging.

## Acknowledgments

Inspired by the following:

* https://github.com/hilverd/lein-ns-dep-graph (which was copied to get started).
* https://github.com/alexander-yakushev/ns-graph


The plugin itself is small; all the hard work is done by
[clojure.tools.namespace](https://github.com/clojure/tools.namespace) and
[Rhizome](https://github.com/ztellman/rhizome).

## License

Portions copyright © 2013 Hilverd Reker

Copyright © 2017 Simon Katz

Distributed under the Eclipse Public License, the same as Clojure.
