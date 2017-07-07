(defproject lein-nomis-ns-graph "0.1.0-SNAPSHOT"
  :description "Show namespace dependencies of project sources as a graph."
  ;; FIXME :url "https://github.com/hilverd/lein-ns-dep-graph"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]
                 [rhizome "0.1.8"]])
