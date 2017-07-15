(ns nomisdraw.utils.nomis-quil-on-reagent
  (:require [cljs.core.async :as a]
            [quil.core :as q :include-macros true]
            [reagent.core :as r]
            [taoensso.timbre :as timbre])
  (:require-macros [cljs.core.async.macros :as a]))

;;;; Making Quil work well with Reagent...
;;;; - I got the core idea from skrat's answer at
;;;;   http://stackoverflow.com/questions/33345084/quil-sketch-on-a-reagent-canvas
;;;; - I've made it more functional.

(defn ^:private random-alpha-string [length]
  (let [ascii-codes (concat (range 65 91)
                            (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(defn ^:private random-canvas-id []
  (random-alpha-string 40))

(defn sketch
  "Wraps `quil.core/sketch` and plays nicely with Reagent.
  Let C be the canvas that will host the sketch.
  Differs from `quil.core/sketch` as follows:
  - Creates C (rather than C having to be created separately).
  - The `:host` argument must not be provided. (Instead, a unique canvas id is
    created.)
  - Returns a component that wraps C.
  - The :size argument must be either `nil` or a [width height] vector."
  [& {:as sketch-args}]
  (assert (not (contains? sketch-args :host))
          ":host should not be provided, because a unique canvas id will be created")
  (let [size            (:size sketch-args)
        _               (assert (or (nil? size)
                                    (and (vector? size)
                                         (= (count size) 2)))
                                (str ":size should be nil or a vector of size 2, but it is "
                                     size))
        [w h]           size
        canvas-id       (random-canvas-id)
        canvas-tag-&-id (keyword (str "canvas#" canvas-id))
        sketch-args*    (merge sketch-args 
                               {:host canvas-id})
        saved-sketch-atom (atom ::not-set-yet)]
    [r/create-class
     {:reagent-render
      (fn []
        (timbre/debug "====" canvas-id "Rendering canvas")
        [canvas-tag-&-id {:style {;; Prevent stretching when used in
                                  ;; flex container. (I don't really
                                  ;; understand, but never mind).
                                  :max-width w}
                          :width  w
                          :height h}])
      ;;
      :component-did-mount
      (fn []
        ;; Use a go block so that the canvas exists before we attach the sketch
        ;; to it. (Needed on initial render; not on re-render.)
        (a/go
          (reset! saved-sketch-atom
                  (apply q/sketch
                         (apply concat sketch-args*)))))
      ;;
      :component-will-unmount
      (fn []
        (timbre/debug "====" canvas-id "Unmounting & exiting sketch")
        (a/go-loop []
          (let [saved-sketch @saved-sketch-atom]
            (if (= saved-sketch ::not-set-yet)
              (do ; will probably never get here
                (timbre/info "Waiting for sketch to be created before destroying it")
                (a/<! (a/timeout 100))
                (recur))
              (q/with-sketch saved-sketch
                (q/exit))))))}]))
