(ns leiningen.nomis-ns-graph.args
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [leiningen.core.main :as lcm]
            [slingshot.slingshot :refer [throw+ try+]]))

;;;; ___________________________________________________________________________

(def ^:private understood-options
  #{:filename
    :platform
    :source-paths
    :show-non-project-deps
    :exclusions
    :write-gv-file?})

(defn make-command-line-options [command-line-args]
  (let [;; --------
        ;; Basic parsing
        [cmd-line-options other-cmd-line-args] (lcm/parse-options
                                                command-line-args)
        _ (do (let [unknown-options (set/difference (-> cmd-line-options
                                                        keys
                                                        set)
                                                    understood-options)]
                (when-not (empty? unknown-options)
                  (throw+ {:type :nomis-ns-graph/exception
                           :message (str "Unknown options: "
                                         unknown-options)}))
                (when-not (empty? other-cmd-line-args)
                  (throw+ {:type :nomis-ns-graph/exception
                           :message (str "Unknown other-cmd-line-args: "
                                         other-cmd-line-args)}))))
        ;; --------
        ;; Get the raw stuff
        {filename-raw              :filename
         platform-raw              :platform
         source-paths-raw          :source-paths
         show-non-project-deps-raw :show-non-project-deps
         exclusions-raw            :exclusions
         write-gv-file?-raw        :write-gv-file?} cmd-line-options
        ;; --------
        ;; Turn into user-oriented printable Clojure data
        show-non-project-deps (if (instance? Boolean show-non-project-deps-raw)
                                show-non-project-deps-raw
                                (-> show-non-project-deps-raw
                                    edn/read-string
                                    boolean))
        write-gv-file? (if (instance? Boolean write-gv-file?-raw)
                         write-gv-file?-raw
                         (-> write-gv-file?-raw
                             edn/read-string
                             boolean))
        platform (or platform-raw
                     "clj")
        _ (when-not (#{"clj" "cljs"} platform)
            (throw+ {:type :nomis-ns-graph/exception
                     :message (str "Bad platform: "
                                   platform)}))
        source-paths (if source-paths-raw
                       (str/split source-paths-raw
                                  #" |\|")
                       nil)
        filename (or filename-raw
                     "nomis-ns-graph")
        exclusions (if exclusions-raw
                     (str/split exclusions-raw
                                #" |\|")
                     nil)
        options {:platform platform
                 :source-paths source-paths
                 :show-non-project-deps show-non-project-deps
                 :exclusions exclusions
                 :filename filename
                 :write-gv-file? write-gv-file?}]
    (lcm/info "options =" options)
    (assoc options
           ;; Transform into non-user-oriented printable Clojure data
           :platform (-> options :platform edn/read-string keyword))))
