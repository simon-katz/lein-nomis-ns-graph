(ns leiningen.nomis-ns-graph.p200-graphing.graph
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.dependency :as ctns-dep]
            [clojure.tools.namespace.file :as ctns-file]
            [clojure.tools.namespace.find :as ctns-find]
            ;; [clojure.tools.namespace.parse :as ctns-parse]
            [clojure.tools.namespace.track :as ctns-track]
            [leiningen.core.main :as lcm]
            [leiningen.nomis-ns-graph.utils.utils :as u]
            [rhizome.dot :as dot]))

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

(defn ^:private sym->style-map [node->part-of-project? sym]
  {:style (if (node->part-of-project? sym)
            :solid
            :dashed)})

(defn ^:private compute-source-files [platform
                                      source-paths]
  (let [platform-for-ctns (case platform
                            :clj ctns-find/clj
                            :cljs ctns-find/cljs)]
    (apply set/union
           (map (comp #(ctns-find/find-sources-in-dir %
                                                      platform-for-ctns)
                      io/file)
                source-paths))))

(defn ^:private source-files->dep-graph [source-files]
  (let [tracker (ctns-file/add-files {} source-files)]
    (tracker ::ctns-track/deps)))

(defn ^:private source-files->ns-names [source-files]
  (set (map (comp second ctns-file/read-file-ns-decl)
            source-files)))

(defn  ^:private leaf-nodes->node->self-and-children [leaf-nodes]
  (reduce (fn [sofar [n p]]
            (update sofar
                    p
                    (fnil conj [])
                    n))
          {}
          (for [n leaf-nodes
                p (ns-symbol->all-parent-ns-symbols-incl-self
                   n)]
            [n p])))

(defn ^:private node-excluded-by-user? [exclusions
                                        exclusions-re
                                        sym]
  (or (some #(str/starts-with? sym %)
            exclusions)
      (when exclusions-re
        (re-find (re-pattern exclusions-re)
                 (name sym)))))

(defn ^:private make-include-node?-fun [node->part-of-project?
                                        exclusions
                                        exclusions-re
                                        show-non-project-deps]
  (fn [sym]
    (let [node-satisfies-project-constraints? (fn [sym]
                                                (or (node->part-of-project? sym)
                                                    show-non-project-deps))]
      (and (node-satisfies-project-constraints? sym)
           (not (node-excluded-by-user? exclusions
                                        exclusions-re
                                        sym))))))

(defn ^:private ns-graph-spec->title-dot-data [{:keys [platform
                                                       source-paths
                                                       exclusions
                                                       exclusions-re
                                                       show-non-project-deps
                                                       project-group
                                                       project-name]
                                                :as ns-graph-spec}]
  (str (str (str project-group "/" project-name)
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
       (when-not (nil? exclusions-re)
         (str "\\l:exclusions-re: " exclusions-re))
       (when-not (empty? exclusions)
         (str "\\l:exclusions:\\l"
              (apply str
                     (str/join "\\l"
                               (map (partial str "    ")
                                    exclusions)))))
       "\\l"))

(defn ^:private make-symbol->descriptor-fun [node->part-of-project?]
  (fn [sym color]
    (merge (sym->style-map node->part-of-project?
                           sym)
           {:label (ns-symbol->last-piece sym)
            :color color
            :fontcolor color})))

(defn ^:private much-stuff->dot [nodes-to-show
                                 node->dependees
                                 node->part-of-project?
                                 node->has-dependees?
                                 node->has-dependers?
                                 ns-graph-spec]
  (let [symbol->descriptor-fun (-> node->part-of-project?
                                   make-symbol->descriptor-fun)]
    (dot/graph->dot nodes-to-show
                    node->dependees
                    :node->descriptor #(symbol->descriptor-fun % :black)
                    :edge->descriptor #(sym->style-map node->part-of-project?
                                                       %2)
                    :options {:dpi 300}
                    :cluster->show-as-node? #(or (node->has-dependees? %)
                                                 (node->has-dependers? %))
                    :cluster->descriptor #(symbol->descriptor-fun %
                                                                  (case 1 ; for easy dev/debug
                                                                    1 :blue
                                                                    2 :red
                                                                    3 :purple))
                    :node->cluster ns-symbol->parent-ns-symbol
                    :cluster->parent ns-symbol->parent-ns-symbol
                    :left-justify-cluster-labels? true
                    :title (ns-graph-spec->title-dot-data ns-graph-spec))))

(defn ns-graph-spec->dot-data [{:keys [platform
                                       source-paths
                                       exclusions
                                       exclusions-re
                                       show-non-project-deps
                                       project-group
                                       project-name]
                                :as ns-graph-spec}]
  (let [source-files            (compute-source-files platform
                                                      source-paths)
        dep-graph               (source-files->dep-graph source-files)
        ns-names-with-parents   (-> source-files
                                    source-files->ns-names
                                    ns-symbols->all-ns-symbols) 
        node->part-of-project?  (partial contains? ns-names-with-parents)
        include-node?-fun       (make-include-node?-fun node->part-of-project?
                                                        exclusions
                                                        exclusions-re
                                                        show-non-project-deps)
        leaf-nodes              (filter include-node?-fun
                                        (ctns-dep/nodes dep-graph))
        nodes                   (ns-symbols->all-ns-symbols leaf-nodes)
        node->self-and-children (leaf-nodes->node->self-and-children leaf-nodes)
        node->dependees         #(filter include-node?-fun
                                         (ctns-dep/immediate-dependencies
                                          dep-graph
                                          %))
        node->dependers         (u/invert-relation node->dependees
                                                   nodes)
        node->has-dependees?    (comp not
                                      empty?
                                      node->dependees)
        node->has-dependers?    (comp not
                                      empty?
                                      node->dependers)
        nodes-to-show           (filter (fn [n]
                                          (or (node->part-of-project? n)
                                              (some node->has-dependers?
                                                    (node->self-and-children n))))
                                        nodes)
        dot-data                (much-stuff->dot nodes-to-show
                                                 node->dependees
                                                 node->part-of-project?
                                                 node->has-dependees?
                                                 node->has-dependers?
                                                 ns-graph-spec)]
    dot-data))
