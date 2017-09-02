(ns leiningen.nomis-ns-graph.p100-args-and-options.p200-options
  (:require [leiningen.core.main :as lcm]))

;;;; ___________________________________________________________________________

(def ^:private output-spec-keys
  [:filename
   :write-gv-file?])

(defn ^:private maybe-add-source-paths [command-line-options
                                        project]
  (if (:source-paths command-line-options)
    command-line-options
    (assoc command-line-options
           :source-paths
           (case (:platform command-line-options)
             :clj (-> project
                      :source-paths)
             :cljs (let [assumed-cljs-source-paths ["src/cljs"
                                                    "cljs/src"]]
                     (lcm/info "Assuming cljs source paths ="
                               assumed-cljs-source-paths
                               "(you can override this with the :source-paths option).")
                     assumed-cljs-source-paths)))))

(defn project-&-command-line-options->specs [project
                                             command-line-options]
  (let [options (-> command-line-options
                    (maybe-add-source-paths project))]
    {:ns-graph-spec (merge (apply dissoc options output-spec-keys)
                           {:project-group (:group project)
                            :project-name  (:name project)})
     :output-spec   (select-keys options
                                 output-spec-keys)}))
