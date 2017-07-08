(defproject lein-nomis-ns-graph "0.3.0-SNAPSHOT"
  :description "Show namespace dependencies of project sources as a graph."
  :url "https://github.com/simon-katz/lein-nomis-ns-graph"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/tools.namespace "0.3.0-alpha4"]
                 [rhizome "0.2.7"]]
  :repl-options {:init-ns user}
  :profiles {:dev {:dependencies [[midje "1.7.0"]]
                   :source-paths ["dev"]
                   :plugins [[lein-midje "3.1.3"]]}})
