(ns leiningen.nomis-ns-graph.p100-args-and-options.p100-args
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
    :exclusions-re
    :write-gv-file?})

(defn ^:private command-line-args->raw-options [command-line-args]
  ;; Basic processing of command-line args.
  (let [[raw-options
         other-cmd-line-args] (lcm/parse-options command-line-args)
        _ (do (let [unknown-options (set/difference (-> raw-options
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
                                         other-cmd-line-args)}))))]
    raw-options))

;;;; ___________________________________________________________________________

(defn ^:private boolean-ify [raw-value]
  (if (instance? Boolean raw-value)
    raw-value
    (-> raw-value
        edn/read-string
        boolean)))

(defn ^:private raw-options->options-for-printing [raw-options]
  ;; Transform into user-oriented printable Clojure data.
  (let [{filename-raw              :filename
         platform-raw              :platform
         source-paths-raw          :source-paths
         show-non-project-deps-raw :show-non-project-deps
         exclusions-raw            :exclusions
         exclusions-re-raw         :exclusions-re
         write-gv-file?-raw        :write-gv-file?} raw-options]
    (when-not (or (nil? platform-raw)
                  (#{"clj" "cljs"} platform-raw))
      (throw+ {:type :nomis-ns-graph/exception
               :message (str "Bad platform: "
                             platform-raw)}))
    {:platform              (or platform-raw
                                "clj")
     :source-paths          (if source-paths-raw
                              (str/split source-paths-raw
                                         #" |\|")
                              nil)
     :show-non-project-deps (boolean-ify show-non-project-deps-raw)
     :exclusions            (if exclusions-raw
                              (str/split exclusions-raw
                                         #" |\|")
                              nil)
     :exclusions-re         (if exclusions-re-raw
                              exclusions-re-raw
                              nil)
     :filename              (or filename-raw
                                "nomis-ns-graph")
     :write-gv-file?        (boolean-ify write-gv-file?-raw)}))

;;;; ___________________________________________________________________________

(defn ^:private options-for-printing->options [options-for-printing]
  ;; Transform into non-user-oriented Clojure data.
  (assoc options-for-printing
         :platform (-> options-for-printing
                       :platform
                       edn/read-string
                       keyword)))

;;;; ___________________________________________________________________________

(defn make-command-line-options [command-line-args]
  (let [options-for-printing (-> command-line-args
                                 command-line-args->raw-options
                                 raw-options->options-for-printing)]
    (lcm/info "options =" options-for-printing)
    (-> options-for-printing
        options-for-printing->options)))
