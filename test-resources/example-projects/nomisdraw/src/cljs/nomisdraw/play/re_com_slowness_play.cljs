(ns nomisdraw.play.re-com-slowness-play
  (:require [re-com.core :as re]))

(defn render []
  (time
   (let [text          "Rhubarb"
         approach      1
         nesting-level (case 2
                         1   4
                         2 620)]
     (letfn [[nestify [elements]
              (case approach
                1 (into [:div] elements)
                2 [re/v-box :children elements])]
             (stuff [n]
               (nestify (repeat 5 [:p (str n " " text)])))
             (nested-structure [n]
               (letfn [(r [cnt]
                         (if (> cnt n)
                           nil
                           (nestify [(r (inc cnt))
                                     (stuff cnt)])))]
                 (r 1)))]
       (nested-structure nesting-level)))))
