
(ns respo.render.dom
  (:require [clojure.string :as string]
            [respo.util.format :refer [dashed->camel event->prop ensure-string]]))

(def svg-ns "http://www.w3.org/2000/svg")

(defn make-element [virtual-element listener-builder]
  (let [tag-name (name (:name virtual-element))
        svg? (:svg? virtual-element)
        attrs (:attrs virtual-element)
        style (:style virtual-element)
        children (:children virtual-element)
        element (if svg?
                  (.createElementNS js/document svg-ns tag-name)
                  (.createElement js/document tag-name))
        child-elements (->> children
                            (map (fn [entry] (make-element (last entry) listener-builder))))]
    (doseq [entry attrs]
      (let [k (dashed->camel (name (first entry)))
            svg-k (name (first entry))
            v (last entry)]
        (if (some? v)
          (if (and svg? (not= k "innerHTML"))
            (.setAttribute element svg-k v)
            (aset element k v)))))
    (doseq [entry style]
      (let [k (dashed->camel (name (first entry))), v (last entry)]
        (aset (aget element "style") k (if (keyword? v) (name v) v))))
    (doseq [event-name (:event virtual-element)]
      (let [name-in-string (event->prop event-name)]
        (comment println "listener:" event-name name-in-string)
        (aset
         element
         name-in-string
         (fn [event]
           ((listener-builder event-name) event (:coord virtual-element))
           (.stopPropagation event)))))
    (doseq [child-element child-elements] (.appendChild element child-element))
    element))

(defn style->string [styles]
  (->> styles
       (map
        (fn [entry]
          (let [k (first entry), v (ensure-string (last entry))] (str (name k) ":" v ";"))))
       (string/join "")))
