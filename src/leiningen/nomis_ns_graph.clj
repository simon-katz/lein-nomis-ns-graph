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

(def ^:private default-options
  {"-platform" "clj"
   "-show-non-project-deps" false})

(defn ^:private build-arguments [args]
  (let [tentative-options (merge default-options
                                 (try (apply hash-map args)
                                      (catch Exception e
                                        (lcm/warn "Error with args")
                                        (lcm/warn "(count args) =" (count args))
                                        (doall (map-indexed
                                                (fn [n x]
                                                  (lcm/warn (str "arg #" (inc n))
                                                            x))
                                                args))
                                        (throw+ {:type :nomis-ns-graph/exception
                                                 :message (str
                                                           "Error when turning args into map: "
                                                           (.getMessage e))}))))]
    (merge tentative-options
           {"-name" (or (get tentative-options "-name")
                        (str "nomis-ns-graph-"
                             (get tentative-options "-platform")
                             (when (get tentative-options
                                        "-show-non-project-deps")
                               "-with-externals")))
            :exclusions (if-let [excl-text (get tentative-options
                                                "-exclusions")]
                          (for [s (str/split excl-text
                                             #" |\|")]
                            #(str/starts-with? % s))
                          [])})))

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
  (let [built-args (build-arguments args)
        filename (add-image-extension (get built-args "-name"))
        platform-as-string (get built-args "-platform")
        platform (case platform-as-string
                   "clj" ns-find/clj
                   "cljs" ns-find/cljs
                   (do
                     (lcm/info "Defaulting platform to clj")
                     ns-find/clj))
        source-paths (case platform-as-string
                       "clj" (-> project
                                 :source-paths)
                       "cljs" (let [assumed-cljs-source-paths ["src/cljs"]]
                                ;; FIXME assumed-cljs-source-paths
                                (lcm/info "Assuming cljs source paths ="
                                          assumed-cljs-source-paths)
                                assumed-cljs-source-paths))
        source-files (apply set/union
                            (map (comp #(ns-find/find-sources-in-dir %
                                                                     platform)
                                       io/file)
                                 source-paths))
        tracker (ns-file/add-files {} source-files)
        dep-graph (tracker ::ns-track/deps)
        ns-names (set (map (comp second ns-file/read-file-ns-decl)
                           source-files))
        ns-names-with-parents (ns-symbols->all-ns-symbols ns-names)
        part-of-project? (partial contains? ns-names-with-parents)
        include-node? (fn [sym]
                        (let [exclusions (:exclusions built-args)
                              extra-exclusion (comp
                                               not
                                               (if (get built-args
                                                        "-show-non-project-deps")
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
