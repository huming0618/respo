
(ns respo.render.differ
  (:require [clojure.string :as string]
            [respo.util.format :refer [purify-element]]
            [respo.util.detect :refer [component?]]))

(declare find-element-diffs)

(defn find-children-diffs [acc n-coord index old-children new-children]
  (comment
    .log
    js/console
    "diff children:"
    acc
    n-coord
    index
    old-children
    new-children)
  (let [old-size (count old-children) new-size (count new-children)]
    (cond
      (= old-size new-size 0) acc
      (and (zero? old-size) (pos? new-size)) (let 
                                               [element
                                                (last
                                                  (first new-children))
                                                next-acc
                                                (conj
                                                  acc
                                                  [:append
                                                   n-coord
                                                   (purify-element
                                                     element)])]
                                               (recur
                                                 next-acc
                                                 n-coord
                                                 (inc index)
                                                 []
                                                 (subvec
                                                   new-children
                                                   1)))
      (and (pos? old-size) (zero? new-size)) (let 
                                               [next-acc
                                                (conj
                                                  acc
                                                  [:rm
                                                   (conj
                                                     n-coord
                                                     index)])]
                                               (recur
                                                 next-acc
                                                 n-coord
                                                 index
                                                 (subvec
                                                   old-children
                                                   1)
                                                 []))
      :else (let [old-keys (mapv first old-children)
                  new-keys (mapv first new-children)
                  x1 (first old-keys)
                  y1 (first new-keys)
                  x1-remains? (contains? new-keys x1)
                  y1-existed? (contains? old-keys y1)
                  old-follows (subvec old-children 1)
                  new-follows (subvec new-children 1)]
              (cond
                (= x1 y1) (let [old-element (last (first old-children))
                                new-element (last (first new-children))
                                next-acc
                                (find-element-diffs
                                  acc
                                  (conj n-coord index)
                                  old-element
                                  new-element)]
                            (recur
                              next-acc
                              n-coord
                              (inc index)
                              old-follows
                              new-follows))
                (and x1-remains? (not y1-existed?)) (let 
                                                      [next-acc
                                                       (conj
                                                         acc
                                                         (let 
                                                           [element
                                                            (last
                                                              (first
                                                                new-children))]
                                                           [:append
                                                            n-coord
                                                            (purify-element
                                                              element)]))]
                                                      (recur
                                                        next-acc
                                                        n-coord
                                                        (inc index)
                                                        old-children
                                                        new-follows))
                (and (not x1-remains?) y1-existed?) (let 
                                                      [next-acc
                                                       (conj
                                                         acc
                                                         [:rm
                                                          (conj
                                                            n-coord
                                                            index)])]
                                                      (recur
                                                        next-acc
                                                        n-coord
                                                        index
                                                        old-follows
                                                        new-children))
                :else (let [xi (.indexOf (subvec new-keys 1) x1)
                            yi (.indexOf (subvec old-keys 1) y1)
                            first-old-entry (get old-children 0)
                            first-new-entry (get new-children 0)]
                        (if (<= xi yi)
                          (let [new-element (last (first new-children))
                                next-acc
                                (conj
                                  acc
                                  [:add
                                   (conj n-coord index)
                                   (purify-element new-element)])]
                            (recur
                              next-acc
                              n-coord
                              (inc index)
                              old-children
                              new-follows))
                          (let [next-acc
                                (conj acc [:rm (conj n-coord index)])]
                            (recur
                              next-acc
                              n-coord
                              index
                              old-follows
                              new-children)))))))))

(defn find-style-diffs [acc coord old-style new-style]
  (if (identical? old-style new-style)
    acc
    (cond
      (and
        (identical? 0 (count old-style))
        (identical? 0 (count new-style))) acc
      (and (identical? 0 (count old-style)) (> (count new-style) 0)) (let 
                                                                       [entry
                                                                        (get
                                                                          new-style
                                                                          0)
                                                                        follows
                                                                        (subvec
                                                                          new-style
                                                                          1)]
                                                                       (recur
                                                                         (conj
                                                                           acc
                                                                           [:add-style
                                                                            coord
                                                                            entry])
                                                                         coord
                                                                         old-style
                                                                         follows))
      (and (> (count old-style) 0) (identical? 0 (count new-style))) (let 
                                                                       [entry
                                                                        (get
                                                                          old-style
                                                                          0)
                                                                        follows
                                                                        (subvec
                                                                          old-style
                                                                          1)]
                                                                       (recur
                                                                         (conj
                                                                           acc
                                                                           [:rm-style
                                                                            coord
                                                                            (key
                                                                              entry)])
                                                                         coord
                                                                         follows
                                                                         new-style))
      :else (let [old-entry (get old-style 0)
                  new-entry (get new-style 0)
                  old-follows (subvec old-style 1)
                  new-follows (subvec new-style 1)]
              (case
                (compare (key old-entry) (key new-entry))
                -1
                (recur
                  (conj acc [:rm-style coord (key old-entry)])
                  coord
                  old-follows
                  new-style)
                1
                (recur
                  (conj acc [:add-style coord new-entry])
                  coord
                  old-style
                  new-follows)
                (recur
                  (if (identical? (val old-entry) (val new-entry))
                    acc
                    (conj acc [:replace-style coord new-entry]))
                  coord
                  old-follows
                  new-follows))))))

(defn find-props-diffs [acc coord old-props new-props]
  (comment
    .log
    js/console
    "find props:"
    acc
    coord
    old-props
    new-props
    (count old-props)
    (count new-props))
  (cond
    (and (= 0 (count old-props)) (= 0 (count new-props))) acc
    (and (= 0 (count old-props)) (> (count new-props) 0)) (recur
                                                            (conj
                                                              acc
                                                              [:add-prop
                                                               coord
                                                               (get
                                                                 new-props
                                                                 0)])
                                                            coord
                                                            old-props
                                                            (subvec
                                                              new-props
                                                              1))
    (and (> (count old-props) 0) (= 0 (count new-props))) (recur
                                                            (conj
                                                              acc
                                                              [:rm-prop
                                                               coord
                                                               (key
                                                                 (get
                                                                   old-props
                                                                   0))])
                                                            coord
                                                            (subvec
                                                              old-props
                                                              1)
                                                            new-props)
    :else (let [old-entry (get old-props 0)
                new-entry (get new-props 0)
                [old-k old-v] (get old-props 0)
                [new-k new-v] (get new-props 0)
                old-follows (subvec old-props 1)
                new-follows (subvec new-props 1)]
            (comment .log js/console old-k new-k old-v new-v)
            (case
              (compare old-k new-k)
              -1
              (recur
                (conj acc [:rm-prop coord old-k])
                coord
                old-follows
                new-props)
              1
              (recur
                (conj acc [:add-prop coord new-entry])
                coord
                old-props
                new-follows)
              (recur
                (if (= old-v new-v)
                  acc
                  (conj acc [:replace-prop coord new-entry]))
                coord
                old-follows
                new-follows)))))

(defn find-events-diffs [acc coord old-events new-events]
  (comment
    .log
    js/console
    "compare events:"
    (pr-str old-events)
    (pr-str new-events))
  (cond
    (and (= (count old-events) 0) (= (count new-events) 0)) acc
    (and (= (count old-events) 0) (> (count new-events) 0)) (recur
                                                              (conj
                                                                acc
                                                                [:add-event
                                                                 coord
                                                                 (first
                                                                   new-events)])
                                                              coord
                                                              old-events
                                                              (subvec
                                                                new-events
                                                                1))
    (and (> (count old-events) 0) (= (count new-events) 0)) (recur
                                                              (conj
                                                                acc
                                                                [:rm-event
                                                                 coord
                                                                 (first
                                                                   old-events)])
                                                              coord
                                                              (subvec
                                                                old-events
                                                                1)
                                                              new-events)
    :else (case
            (compare (first old-events) (first new-events))
            -1
            (recur
              (conj acc [:rm-event coord (first old-events)])
              coord
              (subvec old-events 1)
              new-events)
            1
            (recur
              (conj acc [:add-event coord (first new-events)])
              coord
              old-events
              (subvec new-events 1))
            (recur
              acc
              coord
              (subvec old-events 1)
              (subvec new-events 1)))))

(defn find-element-diffs [acc n-coord old-tree new-tree]
  (comment
    .log
    js/console
    "element diffing:"
    acc
    n-coord
    old-tree
    new-tree)
  (if (identical? old-tree new-tree)
    acc
    (cond
      (component? old-tree) (recur
                              acc
                              n-coord
                              (get old-tree :tree)
                              new-tree)
      (component? new-tree) (recur
                              acc
                              n-coord
                              old-tree
                              (get new-tree :tree))
      :else (let [old-children (:children old-tree)
                  new-children (:children new-tree)]
              (if (or
                    (not= (:coord old-tree) (:coord new-tree))
                    (not= (:name old-tree) (:name new-tree))
                    (not= (:c-name old-tree) (:c-name new-tree)))
                (conj acc [:replace n-coord (purify-element new-tree)])
                (-> acc
                 ((fn [acc1]
                    (let [old-style (:style old-tree)
                          new-style (:style new-tree)]
                      (if (identical? old-style new-style)
                        acc1
                        (find-style-diffs
                          acc1
                          n-coord
                          (into [] (sort-by first old-style))
                          (into [] (sort-by first new-style)))))))
                 (find-props-diffs
                   n-coord
                   (into [] (sort-by first (:attrs old-tree)))
                   (into [] (sort-by first (:attrs new-tree))))
                 (find-events-diffs
                   n-coord
                   (into [] (sort (keys (:event old-tree))))
                   (into [] (sort (keys (:event new-tree)))))
                 (find-children-diffs
                   n-coord
                   0
                   old-children
                   new-children)))))))
