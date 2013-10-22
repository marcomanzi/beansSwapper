(ns beans.mmanzi.utils.bean
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

;(def get-f (partial acces-field get-name-for))
(defn get-f 
  "Get the property of object"
  [o prop]
  ((keyword prop) (bean o)))
(def set-f (partial acces-field set-name-for))

(defn get-f-type
  "Return the type of the getter for the object :o and the function name f"
  [o f]
  (let [o-class (.getClass o)
        o-methods (into [] (.getMethods o-class))
        o-method-finded (filter #(= (.getName %) (get-name-for f)) o-methods)]
    (.getReturnType (first o-method-finded))))

(defn copy-field!
  "Copy the source-field (s-f) from source (s) to target-field (t-f) of target (t)"
  [s s-f t t-f]
  (set-f t t-f (get-f s s-f)))

(defn copy-field-in-map!
  "Copy the source-fields from source (s) to target-fields of target (t) that are in map"
  [m s t]
  (let [entry-vec (vec m)
        copy-map-entry! (fn [s t [s-f t-f]] (copy-field! s s-f t t-f))
        _ (doall (map #(copy-map-entry! s t %) entry-vec))]
    t))

