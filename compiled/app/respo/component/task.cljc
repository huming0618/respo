
(ns respo.component.task
  (:require [clojure.string :as string]
            [hsl.core :refer [hsl]]
            [respo.alias :refer [div input span create-comp button]]
            [respo.component.debug :refer [comp-debug]]
            [respo.component.space :refer [comp-space]]))

(defn init-state [props] "")

(defn update-state [state text] text)

(def style-task {:display "flex"})

(def style-input
 {:line-height "24px",
  :min-width "300px",
  :font-size "16px",
  :padding "0px 8px",
  :outline "none"})

(def style-button
 {:color (hsl 40 80 100),
  :background-color (hsl 200 80 50),
  :cursor "pointer",
  :padding "0 6px",
  :display "inline-block",
  :border-radius "4px",
  :font-family "Verdana"})

(defn style-done [done?]
  {:vertical-align "middle",
   :background-color (hsl 200 80 70),
   :width "32px",
   :outline "none",
   :border "none",
   :height "32px"})

(def style-time {:color (hsl 0 0 80)})

(defn on-click [props state]
  (fn [event dispatch mutate] (println "clicked")))

(defn handle-remove [props state]
  (fn [event dispatch mutate] (dispatch :remove (:id (:task props)))))

(defn on-text-state [mutate] (fn [e dispatch] (mutate (:value e))))

(defn on-text-change [props state]
  (fn [event dispatch mutate]
    (let [task-id (:id (:task props)) text (:value event)]
      (dispatch :update {:id task-id, :text text}))))

(defn render [props]
  (fn [state mutate]
    (let [task (:task props)]
      (div
        {:style style-task}
        (comp-debug task {:left "160px"})
        (button {:style (style-done (:done task))})
        (comp-space 8 nil)
        (input
          {:style style-input,
           :event {:input (on-text-change props state)},
           :attrs {:value (:text task)}})
        (comp-space 8 nil)
        (input
          {:style style-input,
           :event {:input (on-text-state mutate)},
           :attrs {:value state}})
        (comp-space 8 nil)
        (div {} (span {:attrs {:inner-text state}}))
        (comp-space 8 nil)
        (div
          {:style style-button}
          (span
            {:event {:click (handle-remove props state)},
             :attrs {:inner-text "Remove"}}))
        (comp-space 8 nil)
        (div
          {:style style-time}
          (span
            {:style style-time,
             :attrs {:inner-text (:time state)}}))))))

(def task-component (create-comp :task init-state update-state render))
