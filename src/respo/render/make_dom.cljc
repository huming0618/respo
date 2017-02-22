
(ns respo.render.make-dom
  (:require [clojure.string :as string]
            [respo.util.format :refer [dashed->camel event->prop ensure-string]]
            [polyfill.core :refer [document-create-element*]]))

(defn style->string [styles]
  (string/join
   ""
   (->> styles
        (map
         (fn [entry]
           (let [k (first entry), v (ensure-string (last entry))] (str (name k) ":" v ";")))))))

(defn make-element [virtual-element listener-builder]
  (let [tag-name (name (:name virtual-element))
        attrs (:attrs virtual-element)
        style (:style virtual-element)
        children (:children virtual-element)
        element (document-create-element* tag-name)
        child-elements (->> children
                            (map (fn [entry] (make-element (last entry) listener-builder))))]
    (doall
     (->> attrs
          (map
           (fn [entry]
             (let [k (dashed->camel (name (first entry))), v (last entry)]
               (.setAttribute element k v)
               (aset element k v))))))
    (.setAttribute element "style" (style->string style))
    (doall
     (->> (:event virtual-element)
          (map
           (fn [entry]
             (comment println "Looking into event:" entry)
             (let [event-name (key entry), name-in-string (event->prop event-name)]
               (comment println "listener:" event-name name-in-string)
               (aset
                element
                name-in-string
                (fn [event]
                  ((listener-builder event-name) event (:coord virtual-element))
                  (.stopPropagation event))))))))
    (doseq [child-element child-elements] (.appendChild element child-element))
    element))
