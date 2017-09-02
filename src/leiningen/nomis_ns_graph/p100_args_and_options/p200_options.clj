(ns leiningen.nomis-ns-graph.p100-args-and-options.p200-options
  (:require [leiningen.core.main :as lcm]))

;;;; ___________________________________________________________________________

(defn project-&-command-line-options->specs [project
                                             command-line-options]
  (let [{:keys [platform
                source-paths
                show-non-project-deps
                exclusions
                exclusions-re
                filename
                write-gv-file?]} command-line-options
        source-paths (or source-paths
                         (case platform
                           :clj (-> project
                                    :source-paths)
                           :cljs (let [assumed-cljs-source-paths ["src/cljs"
                                                                  "cljs/src"]]
                                   (lcm/info "Assuming cljs source paths ="
                                             assumed-cljs-source-paths
                                             "(you can override this with the :source-paths option).")
                                   assumed-cljs-source-paths)))]
    {:ns-graph-spec {:platform              platform
                     :source-paths          source-paths
                     :exclusions            exclusions
                     :exclusions-re         exclusions-re
                     :show-non-project-deps show-non-project-deps
                     :project-group         (:group project)
                     :project-name          (:name project)}
     :output-spec   {:filename filename
                     :write-gv-file? write-gv-file?}}))
