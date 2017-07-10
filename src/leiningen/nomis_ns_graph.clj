(ns leiningen.nomis-ns-graph
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.dependency :as ns-dep]
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.tools.namespace.parse :as parse]
            [clojure.tools.namespace.track :as ns-track]
            [leiningen.core.main :as lcm]
            [rhizome.viz :as viz]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import [java.io PushbackReader]))

;;;; ___________________________________________________________________________

(defn ^:private add-image-extension [name]
  (str name ".png"))

;;;; ___________________________________________________________________________

(def understood-options
  [:filename
   :platform
   :show-non-project-deps
   :exclusions])

(defn ^:private make-options [args]
  (let [[cmd-line-options other-cmd-line-args] (lcm/parse-options args)
        ;; --------
        filename-text (:filename cmd-line-options)
        platform-text (or (:platform cmd-line-options)
                          "clj")
        show-non-project-deps (:show-non-project-deps cmd-line-options)
        exclusions-text (:exclusions cmd-line-options)
        ;; --------
        filename (or filename-text
                     (str "nomis-ns-graph-"
                          (name platform-text)
                          (when show-non-project-deps
                            "-with-externals")))
        platform (-> platform-text
                     edn/read-string
                     keyword)
        exclusions (if exclusions-text
                     (str/split exclusions-text
                                #" |\|")
                     [])
        ;; --------
        options {:filename filename
                 :platform platform
                 :show-non-project-deps show-non-project-deps
                 :exclusions exclusions}]
    (lcm/info "options =" options)
    (let [unknown-options (set/difference (-> options keys set)
                                          understood-options)]
      (when-not (empty? unknown-options)
        (lcm/warn "Unknown options:" unknown-options)
        (lcm/exit 1))
      (when-not (empty? other-cmd-line-args)
        (lcm/warn "Unknown other-cmd-line-args:" other-cmd-line-args)
        (lcm/exit 1)))
    options))

;;;; ___________________________________________________________________________

(defn ^:private ns-symbol->pieces [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (map symbol __)))

(defn ^:private ns-symbol->last-piece [sym]
  (-> sym
      ns-symbol->pieces
      last))

(defn ^:private pieces->ns-symbol [pieces]
  (->> pieces
       (str/join ".")
       symbol))

(defn ^:private ns-symbol->parent-ns-symbol [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (butlast __)
    (str/join "." __)
    (if (= __ "")
      nil
      (symbol __))))

(defn ^:private ns-symbol->all-parent-ns-symbols-incl-self [sym]
  (as-> sym __
    (name __)
    (str/split __ #"\.")
    (iterate butlast __)
    (take-while (comp not nil?)
                __)
    (map pieces->ns-symbol __)))

(defn ^:private ns-symbols->all-ns-symbols [ns-names]
  (apply set/union
         (map (comp set ns-symbol->all-parent-ns-symbols-incl-self)
              ns-names)))

(defn ^:private dups [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
    id))

(defn sym->style-map [part-of-project? sym]
  {:style (if (part-of-project? sym)
            :solid
            :dashed)})

(defn ^:private nomis-ns-graph* [project & args]
  (let [options (make-options args)
        filename (add-image-extension (:filename options))
        platform (:platform options)
        platform-for-ns (case platform
                          :clj ns-find/clj
                          :cljs ns-find/cljs
                          (do
                            (lcm/info "Defaulting platform to clj")
                            ns-find/clj))
        source-paths (case platform
                       :clj (-> project
                                :source-paths)
                       :cljs (let [assumed-cljs-source-paths ["src/cljs"]]
                               ;; FIXME assumed-cljs-source-paths
                               (lcm/info "Assuming cljs source paths ="
                                         assumed-cljs-source-paths)
                               assumed-cljs-source-paths))
        source-files (apply set/union
                            (map (comp #(ns-find/find-sources-in-dir %
                                                                     platform-for-ns)
                                       io/file)
                                 source-paths))
        tracker (ns-file/add-files {} source-files)
        dep-graph (tracker ::ns-track/deps)
        ns-names (set (map (comp second ns-file/read-file-ns-decl)
                           source-files))
        ns-names-with-parents (ns-symbols->all-ns-symbols ns-names)
        part-of-project? (partial contains? ns-names-with-parents)
        include-node? (fn [sym]
                        (let [exclusions (for [s (:exclusions options)]
                                           #(str/starts-with? % s))
                              extra-exclusion (comp
                                               not
                                               (if (:show-non-project-deps options)
                                                 (constantly true)
                                                 part-of-project?))
                              exclusions+ (cons extra-exclusion
                                                exclusions)]
                          (not ((apply some-fn exclusions+)
                                sym))))
        leaf-nodes (filter include-node? (ns-dep/nodes dep-graph))
        nodes (ns-symbols->all-ns-symbols leaf-nodes)
        symbol->descriptor (fn [sym color]
                             (let [color color]
                               (merge (sym->style-map part-of-project?
                                                      sym)
                                      {:label (ns-symbol->last-piece sym)
                                       :color color
                                       :fontcolor color})))]
    (viz/save-graph
     nodes
     #(filter include-node? (ns-dep/immediate-dependencies dep-graph %))
     :node->descriptor #(symbol->descriptor % :black)
     :edge->descriptor #(sym->style-map part-of-project?
                                        %2)
     :options {:dpi 72}
     :do-not-show-clusters-as-nodes? true
     :cluster->descriptor #(symbol->descriptor %
                                               (case 1 ; for easy dev/debug
                                                 1 :blue
                                                 2 :red
                                                 3 :purple))
     :node->cluster ns-symbol->parent-ns-symbol
     :cluster->parent ns-symbol->parent-ns-symbol
     :filename filename)
    (lcm/info "Created" filename)))

(defn nomis-ns-graph
  "Create a namespace dependency graph and save it as either nomis-ns-graph or the supplied name."
  [project & args]
  (try+ (apply nomis-ns-graph*
               project
               args)
        (catch [:type :nomis-ns-graph/exception] {:keys [message]}
          (lcm/warn "Error:" message)
          (lcm/exit 1))))
