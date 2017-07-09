(defproject lein-nomis-ns-graph "0.6.0"
  :description "Show namespace dependencies of project sources as a graph."
  :url "https://github.com/simon-katz/lein-nomis-ns-graph"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[nomis-rhizome "0.1.0"]
                 [org.clojure/tools.namespace "0.3.0-alpha4"]
                 [slingshot "0.12.2"]]
  :repl-options {:init-ns user}
  :profiles {:dev {:dependencies [[midje "1.7.0"]]
                   :source-paths ["dev"]
                   :plugins [[lein-midje "3.1.3"]]}})
