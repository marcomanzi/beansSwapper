;; Anything you type in here will be executed
;; immediately with the results shown on the
;; right.
(use (quote clojure.core))
(def ^:private sourcePropertyMap {"sourceName"           "targetName"
                                  "sourceSurname"        "targetSurname"})
(def ^:private subSourceWithIdMap {"subSources.subName"   "subTargets.subTargetName"})
(def ^:private m-full (merge sourcePropertyMap subSourceWithIdMap))
(def ^:private m-to-s {"s1.n" "t1.tN" "s1.sn" "t1.tsN" "s2.n" "t2.tN"})

m-to-s

(defn str-before
  [c s]
  (.substring s 0 (.indexOf s c)))

(defn str-after
  [c s]
  (.substring s (inc (.indexOf s c))))

(def str-before-p (partial str-before "."))
(def str-after-p (partial str-after "."))

(defn obj-ids
  [of m]
  (into #{} (map str-before-p (of m))))

(def sub-objs-s-ids-in (partial obj-ids keys))
(def sub-objs-t-ids-in (partial obj-ids vals))

(defn sort-by-sub-obj-ids
  [m]
  (into {} (sort-by #(str-before-p (first %)) m)))

(defn key-start-with
  "Map entry - String that key has to start with"
  [k e]
  (= (.indexOf (first e) k) 0))

(defn get-elements-wich-key-start-with
  "Element wich key start with k has to be near"
  [m k]
  (into {} (filter (partial key-start-with k) m)))

(defn partition-map-with-f
  [m]
  (let [s-o-ids (sub-objs-s-ids-in m)
        t-o-ids (sub-objs-t-ids-in m)
        m-key-for-sub-obj (map #(str %1 "-" %2) s-o-ids t-o-ids)
        m-sorted (sort-by-sub-obj-ids m)
        sub-objs-properties-map (map #(get-elements-wich-key-start-with m %) s-o-ids)
        properties-without-ids (map #(apply hash-map (map str-after-p (flatten (vec %)))) sub-objs-properties-map)]
    (reduce merge {} (map #(hash-map %1 %2) m-key-for-sub-obj properties-without-ids))))

(partition-map-with-f m-to-s)

(into {} (filter #(= (.indexOf (first %) "s1") 0) {"t2" "T2" "d4.n" "d5.n" "s1.n" "s3.n" "s1.f" "s2.n" "d4.f" "d5.f"}))
(sort-by-sub-obj-ids  {"s1.s" "s3.l" "d4.n" "d5.n" "s1.n" "s3.n" "d4.f" "d5.f"})

(apply hash-map (map str-after-p (flatten (vec {"s1.n" "s2.t"}))))
