
(ns respo.component.task
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo.alias :refer [div input span create-comp]]
            [respo.component.debug :refer [comp-debug]]))

(def style-input
 {:line-height "24px",
  :min-width "300px",
  :font-size "16px",
  :padding "0px 8px",
  :outline "none"})

(def style-button
 {:color (hsl 40 80 100),
  :margin-left "8px",
  :background-color (hsl 200 80 50),
  :cursor "pointer",
  :padding "0 6px",
  :display "inline-block",
  :border-radius "4px",
  :font-family "Verdana"})

(defn on-click [props state]
  (fn [event dispatch mutate] (println "clicked")))

(defn handle-remove [props state]
  (fn [event dispatch mutate] (dispatch :remove (:id (:task props)))))

(defn on-text-change [props state]
  (fn [event dispatch mutate]
    (let [task-id (:id (:task props)) text (:value event)]
      (dispatch :update {:id task-id, :text text}))))

(defn render [props]
  (fn [state mutate]
    (let [task (:task props)]
      (div
        {}
        (comp-debug task {:left "60px"})
        (input
          {:style style-input,
           :event {:input (on-text-change props state)},
           :attrs {:value (:text task)}})
        (span
          {:style style-button,
           :event {:click (handle-remove props state)},
           :attrs {:inner-text "Remove"}})))))

(def task-component (create-comp :task render))