(ns ch.exmachina.beans.swapper.beans
  (:gen-class
    :name ch.exmachina.beans.swapper.Beans
    :methods [#^{:static true} [copyWithMapping 
                                [java.util.Map java.lang.Object java.lang.Object] 
                                java.lang.Object]])
(use [ch.exmachina.beans.swapper.sub-obj-map-split])
  (require [clojure.test :as test]
           [clojure.string :as str]))

(defn create
  "Create a class from the string and args in input"
  [c-str & args] 
  (clojure.lang.Reflector/invokeConstructor
    (resolve (symbol c-str)) 
    (to-array args)))

(defn accessor-name-for 
  "Create a string with the accessor name for field f and accessor type (set, get)"
  [str-acc f] 
  (str str-acc (.toUpperCase (.substring f 0 1)) (.substring f 1)))

(def set-name-for (partial accessor-name-for "set")) 
(def get-name-for (partial accessor-name-for "get"))

(defn acces-field
  "Invoke a function on a field f of instance i using the function t to evaluate the function name"
  [t i f & args]
  (clojure.lang.Reflector/invokeInstanceMethod
    i (t f) (to-array args)))

(def get-f (partial acces-field get-name-for))
(def set-f (partial acces-field set-name-for))

;(defmulti convert)
;(defmethod java.lang.Enum convert (partial .name))
;(defmethod java.lang.String convert (partial .toString))
;(defmethod java.lang.String convert (partial .toString))

(defn copy-field
  "Copy the source-field (s-f) from source (s) to target-field (t-f) of target (t)"
  [s s-f t t-f]
  (set-f t t-f  (get-f s s-f)))

(defn copy-map-entry! 
  [s t [s-f t-f]]
  (copy-field s s-f t t-f))

(defn copyFieldsWithMapping
  "Copy the source-fields from source (s) to target-fields of target (t) that are in map"
  [m s t]
  (let [entry-vec (vec m)
        _ (doall (map #(copy-map-entry! s t %) entry-vec))]
    t))

(defn -copyFieldsWithMapping
  "Copy the source-fields from source (s) to target-fields of target (t) that are in map"
  [m s t]
  (copyFieldsWithMapping m s t))




(defn partition-map-in-maps-by
  "Partition the elements of maps using the predicate"
  [m f]
  (map #(into {} %) (partition-by f m)))

(defn evaluate-swap-mapping
  "Create a map with: 
   The properties mapping for the object
   subProperty1-subTargetProperty1
    ....
   subProperty1-subTargetProperty1 All the map for subProperty objects of object"
  [m]
  (let [partitioned-maps (partition-map-in-maps-by (sort-by #(> (.indexOf (first %) ".") 0) m) 
                                                   #(> (.indexOf (first %) ".") 0))
        first-map (first partitioned-maps)
        obj-map (if (< (.indexOf (first (first first-map)) ".") 0) first-map {})
        sub-obj-maps (if (> (.indexOf (first (first first-map)) ".") 0) first-map (second partitioned-maps)) 
        full-property-map (merge obj-map (partition-sub-objs-map sub-obj-maps))]
    full-property-map))

(defn get-method-by-name
  [o meth-name]
  (let [o-class (.getClass o)
        o-methods (into [] (.getMethods o-class))]
    (first (filter #(= (.getName %) (get-name-for meth-name)) o-methods))))

(defn get-f-type
  "Return the type of the getter for the object :o and the function name f"
  [o f]
  (let [o-method-finded (get-method-by-name o f)
        r-t-s  (.toString (.getReturnType o-method-finded))]
    (last (.split r-t-s " "))))

(defn get-f-list-type-str
  "Return the type for the list searched by property name"
  [o f]
  (let [o-method-finded (get-method-by-name o f)
        get-method-name (.toGenericString o-method-finded)]
    (last (re-find #"<(.*)>" get-method-name))))

(defn copy-list-of-sub-obj
  [m source-f-n target-f-n s t copy-function]
  (let [target-obj-type (get-f-list-type-str t target-f-n)
        objs-to-copy (get-f s source-f-n)
        objs-copied (map #(copy-function m % target-obj-type) objs-to-copy)]
    (set-f t target-f-n objs-copied)))

(defn copy-sub-obj
  [m sub-obj-id s t-obj copy-function]
  (let [[source-f-n target-f-n] (.split sub-obj-id "-")
        type (get-f-type s source-f-n)]
    (if (= type "java.util.List")
      (copy-list-of-sub-obj m source-f-n target-f-n s t-obj copy-function)
      (let [sub-source (get-f s source-f-n)
            target-obj-type (get-f-type t-obj target-f-n)]
        (set-f t-obj target-f-n (copy-function m sub-source target-obj-type))))))

(defn copy-sub-objs-maps
  [sub-objs-maps s t-obj copy-function]
  (doall (map #(copy-sub-obj (second %) (first %) s t-obj copy-function) sub-objs-maps)))

(defn copyWithMapping
  [m s t]
  (let [mapping (evaluate-swap-mapping (into {} m))
        {obj-props false sub-obj-props true} (group-by #(> (.indexOf (first %) "-") 0) mapping)
        t-obj (create t)
        _ (if obj-props (copyFieldsWithMapping obj-props s t-obj))
        _ (if sub-obj-props (copy-sub-objs-maps sub-obj-props s t-obj copyWithMapping))] 
        t-obj))

(defn -copyWithMapping
  [m s t]
  (copyWithMapping m s t))
