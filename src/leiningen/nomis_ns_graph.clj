(ns leiningen.nomis-ns-graph
  (:require [leiningen.nomis-ns-graph.args :as args]
            [leiningen.nomis-ns-graph.graph :as graph]
            [leiningen.core.main :as lcm]
            [rhizome.viz :as viz]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import [java.io PushbackReader]))

;;;; ___________________________________________________________________________

(defn ^:private add-png-extension [name]
  (str name ".png"))

(defn ^:private add-gv-extension [name]
  (str name ".gv"))

(defn ^:private nomis-ns-graph* [project & args]
  (let [options (args/make-options args)
        {:keys [filename
                platform
                source-paths
                show-non-project-deps
                exclusions
                write-gv-file?]} options
        source-paths (or source-paths
                         (case platform
                           :clj (-> project
                                    :source-paths)
                           :cljs (let [assumed-cljs-source-paths ["src/cljs"
                                                                  "cljs/src"]]
                                   (lcm/info "Assuming cljs source paths ="
                                             assumed-cljs-source-paths
                                             "(you can override this with the :source-paths option).")
                                   assumed-cljs-source-paths)))
        ns-graph-spec {:platform              platform
                       :source-paths          source-paths
                       :exclusions            exclusions
                       :show-non-project-deps show-non-project-deps
                       :project-group         (:group project)
                       :project-name          (:name project)}
        dot-data (graph/ns-graph-spec->dot-data ns-graph-spec)]
    (when write-gv-file?
      (let [gv-filename (add-gv-extension filename)]
        (spit gv-filename
              dot-data)
        (lcm/info "Created" gv-filename)))
    (let [png-filename (add-png-extension filename)]
      (-> dot-data
          viz/dot->image
          (viz/save-image png-filename))
      (lcm/info "Created" png-filename))))

(defn nomis-ns-graph
  "Create a namespace dependency graph and save it."
  [project & args]
  (try+ (apply nomis-ns-graph*
               project
               args)
        (catch [:type :nomis-ns-graph/exception] {:keys [message]}
          (lcm/warn "Error:" message)
          (lcm/exit 1))))
