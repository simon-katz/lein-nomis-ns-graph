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

(def ^:private understood-options
  #{:filename
    :platform
    :source-paths
    :show-non-project-deps
    :exclusions})

(defn ^:private make-options [args]
  (let [;; --------
        ;; Basic parsing
        [cmd-line-options other-cmd-line-args] (lcm/parse-options args)
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
         exclusions-raw            :exclusions} cmd-line-options
        ;; --------
        ;; Turn into user-oriented printable Clojure data
        show-non-project-deps (if (instance? Boolean show-non-project-deps-raw)
                                show-non-project-deps-raw
                                (-> show-non-project-deps-raw
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
        options {:filename filename
                 :platform platform
                 :source-paths source-paths
                 :show-non-project-deps show-non-project-deps
                 :exclusions exclusions}]
    (lcm/info "options =" options)
    (assoc options
           ;; Transform into non-user-oriented printable Clojure data
           :platform (-> options :platform edn/read-string keyword))))

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
        {:keys [filename
                platform
                source-paths
                show-non-project-deps
                exclusions]} options
        filename-with-extension (add-image-extension filename)
        platform-for-ns (case platform
                          :clj ns-find/clj
                          :cljs ns-find/cljs)
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
                        (let [exclusion-fns (for [s exclusions]
                                              #(str/starts-with? % s))
                              extra-exclusion-fn (comp
                                                  not
                                                  (if show-non-project-deps
                                                    (constantly true)
                                                    part-of-project?))
                              exclusions+ (cons extra-exclusion-fn
                                                exclusion-fns)]
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
     :left-justify-cluster-labels? true
     :title (str (str (str (:group project) "/" (:name project))
                      " namespace dependencies")
                 (str "\\l:platform: " (name platform))
                 (str "\\l:source-paths: "
                      (letfn [(fix-slashes [s]
                                (str/replace s "\\" "/"))]
                        (let [root-path (str (-> (clojure.java.io/file ".")
                                                 .getCanonicalPath
                                                 fix-slashes)
                                             "/")]
                          (str/join " "
                                    (->> source-paths
                                         (map fix-slashes)
                                         (map #(str/replace-first %
                                                                  root-path
                                                                  "")))))))
                 (when show-non-project-deps 
                   "\\l:show-non-project-deps true")
                 (when-not (empty? exclusions)
                   (str "\\l:exclusions:\\l"
                        (apply str
                               (str/join "\\l"
                                         (map (partial str "    ")
                                              exclusions)))))
                 "\\l")
     :filename filename-with-extension)
    (lcm/info "Created" filename)))

(defn nomis-ns-graph
  "Create a namespace dependency graph and save it."
  [project & args]
  (try+ (apply nomis-ns-graph*
               project
               args)
        (catch [:type :nomis-ns-graph/exception] {:keys [message]}
          (lcm/warn "Error:" message)
          (lcm/exit 1))))
