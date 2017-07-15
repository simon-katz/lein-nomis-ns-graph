(ns nomisdraw.utils.nomis-re-com-utils
  (:require [reagent.core :as r]
            [re-com.core :as re]))

;;; TODO: Doc.
;;;       Including:
;;;       - The `:fun`s need to be #'-d for interactive development.
;;;         (Hmmm, so don't use anonymous functions. Painful.)

;;; TODO: Use a schema (or a clojure.spec spec) for options.

(defonce ^:private options-s-atom
  (atom {}))

(defn ^:private options-&-uniquifier>selected-id-atom [options uniquifier]
  (let [k [options uniquifier]]
   (or (get @options-s-atom
            k)
       (let [a (r/atom (-> options
                           first
                           :id))]
         (swap! options-s-atom
                assoc
                k
                a)
         a))))

(defn dropdown-and-chosen-item [& {:keys [options
                                          uniquifier
                                          outer-style
                                          inner-style]
                                   :or {:uniquifier ::default}}]
  (let [selected-id-atom (options-&-uniquifier>selected-id-atom options
                                                                uniquifier)]
    [re/v-box
     :style outer-style
     :width     "700px"
     :gap       "10px"
     :children  [[re/h-box
                  :gap      "10px"
                  :align    :center
                  :children [[re/label :label "Select a demo"]
                             [re/single-dropdown
                              :choices   options
                              :model     selected-id-atom
                              :width     "300px"
                              :on-change #(reset! selected-id-atom %)]]]
                 (re/box
                  :style inner-style
                  :child
                  (let [fun (->> options
                                 (filter #(= @selected-id-atom
                                             (:id %)))
                                 first
                                 :fun)]
                    [fun]))]]))
