(ns leiningen.nomis-ns-graph
  (:require [leiningen.core.main :as lcm]
            [leiningen.nomis-ns-graph.p100-args-and-options.p100-args :as args]
            [leiningen.nomis-ns-graph.p100-args-and-options.p200-options :as options]
            [leiningen.nomis-ns-graph.p200-graphing.graph :as graph]
            [rhizome.viz :as viz]
            [slingshot.slingshot :refer [throw+ try+]]))

;;;; ___________________________________________________________________________

(defn ^:private add-png-extension [name]
  (str name ".png"))

(defn ^:private add-gv-extension [name]
  (str name ".gv"))

(defn ^:private write-output [dot-data output-spec]
  (let [{:keys [filename
                write-gv-file?]} output-spec]
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

(defn ^:private nomis-ns-graph* [project & args]
  (let [command-line-options (args/make-command-line-options args)
        {:keys [ns-graph-spec
                output-spec]} (options/project-&-command-line-options->specs
                               project
                               command-line-options)
        dot-data (graph/ns-graph-spec->dot-data ns-graph-spec)]
    (write-output dot-data
                  output-spec)))

(defn nomis-ns-graph
  "Create a namespace dependency graph and save it."
  [project & args]
  (try+ (apply nomis-ns-graph*
               project
               args)
        (catch [:type :nomis-ns-graph/exception] {:keys [message]}
          (lcm/warn "Error:" message)
          (lcm/exit 1))))
