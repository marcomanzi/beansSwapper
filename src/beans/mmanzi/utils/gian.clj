(ns beans.mmanzi.utils.gian
  (require [clojure.string :as s]))

(defn split-source [v] 
 (let [[f s] (s/split v #"[.]")]
   (if s
     [f (keyword s)]
     [(keyword f)])) )

(defn split-target [v]
  (let [[f s] (s/split v #"[.]")]
   (if s
     [f (str "set" (s/capitalize s))]
     [(str "set" (s/capitalize f))])))


(defn adapt-with [m b]
    (let [mapping (reduce (fn [acc [k v]] (assoc-in acc  (split-source k) (split-target v))) {} m)]
        (for [[sm tm] mapping :when (keyword? sm)]
          [tm (sm b)])))

(defn invoke [m b & p]
  (println "invoking " m " of object " b " with params " p))

(defn pour-into [t s]
  (doall
    (for [[[m] v] s]
      (invoke m t v)))
  t)

(def source0 (bean (java.util.Date.)))

(def target0 (java.awt.Frame.))

(def mapping {"source.one" "target.two"
         "source.two" "target.four"
         "source.three" "target.ten"
         "seconds" "b"
         "date" "dt"
         "minutes" "mins"
         "res" "resizable"})

(pour-into target0 (adapt-with mapping source0))