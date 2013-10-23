(ns beans.mmanzi.utils.bean_tests
  (use [clojure.test]
       [beans.mmanzi.utils.bean])
  (require [clojure.string :as str]))

(defn- create-sub-source
  [name]
  (let [s (create "test.objects.SubSource")
        _ (set-f s "subName" name)]
    s))

(defn- create-source
  [name surname]
  (let [s (create "test.objects.Source")
        _ (set-f s "sourceName" name)
        _ (set-f s "sourceSurname" surname)
        _ (set-f s "subSources" [(create-sub-source "sub1") (create-sub-source "sub2")])]
    s))

(def ^:private create-target (create "test.objects.Target"))
(def ^:private create-sub-target (create "test.objects.SubTarget"))
(def ^:private source0 (create-source "testName" "testSurname"))
(def ^:private sourcePropertyMap {"sourceName"           "targetName" 
                                  "sourceSurname"        "targetSurname"})
(def ^:private subSourceWithIdMap {"subSources.subName"   "subTargets.subTargetName"})
(def ^:private subSourceMap {"subName"   "subTargetName"})

(def ^:private mappingFull 
  (merge sourcePropertyMap subSourceWithIdMap))


(deftest copy-properties-in-map!-tests
  (is (= (let [t create-target
               t (copy-field-in-map! sourcePropertyMap source0 t)]
           (get-f t "targetName")) "testName")))

(deftest copy-source-sub-object!-tests
  (is (= (let [t create-sub-target
               t (copy-field-in-map! subSourceMap ((get-f source0 "subSources") 0) t)]
           (get-f t "subTargetName")) "sub1")))

(deftest full-copy-source!-tests
  (is (= (let [t create-target
               _ (copy-field-in-map! sourcePropertyMap source0 t)
               _ (set-f t "subTargets" (map #(let [st (create "test.objecs.SubTarget")]
                         (copy-field-in-map! subSourceMap % st)) (get-f source0 "subSources")))]
           (get-f t "subTargets") 1)) "sub2"))

(defn get-method-by-name
  [o meth-name]
  (let [o-class (.getClass o)
        o-methods (into [] (.getMethods o-class))]
    (first (filter #(= (.getName %) (get-name-for meth-name)) o-methods))))

(defn get-f-type
  "Return the type of the getter for the object :o and the function name f"
  [o f]
  (let [o-method-finded (get-method-by-name o f)]
    (.getReturnType o-method-finded)))

(deftest get-f-type-tests
  (is (= (let [t create-target]
           (get-f-type t "subTargets")) java.util.List)))

(defn get-f-list-type-str
  "Return the type for the list searched by property name"
  [o f]
  (let [o-method-finded (get-method-by-name o f)
        get-method-name (.toGenericString o-method-finded)]
    (last (re-find #"<(.*)>" get-method-name))))

(deftest get-f-list-type-str-tests
  (is (= (let [t create-target]
           (get-f-list-type-str t "subTargets")) "test.objects.SubTarget"))
  (is (= (let [s source0]
           (get-f-list-type-str s "subSources")) "test.objects.SubSource")))

(defn partition-map-in-maps-by
  "Partition the elements of maps using the predicate"
  [m f]
  (map #(into {} %) (partition-by f m)))
  

(deftest partition-map-in-maps-by-tests
  (is (= (partition-map-in-maps-by {"test" "test" "withPoint.test" "withPoint.test"} #(.contains (first %) ".")) 
         [{"test" "test"}{"withPoint.test" "withPoint.test"}])))

(def first-map-key-str (comp first first))
(def second-map-key-str (comp second first))

(defn first-point-index
  [s]
  (.indexOf s "."))
  

(defn sub-obj-property-name
  "Evalutae the sub object property name for source or target depending on f"
  [m f]
  (let [key-str (f m)
        point-index (first-point-index key-str)]
    (if (> point-index 0)
      (.substring key-str 0 point-index)
      nil)))

(defn keyword-for-sub-object
  "Evaluate the keyword for the map with subObject properties"
  [subObj-m]
  (let [source-property-name (sub-obj-property-name subObj-m first-map-key-str)
        target-property-name (sub-obj-property-name subObj-m second-map-key-str)]
    (if source-property-name
      (keyword (str source-property-name "-" target-property-name))
      :superObject)))

(deftest keyword-for-sub-object-tests
  (is (= (keyword-for-sub-object {"test" "test" }) :superObject))
  (is (= (keyword-for-sub-object {"withPoint.test" "withPoint.test" }) :withPoint-withPoint)))

(defn s-after-first-point
  "Return the string after the . if a . is in the string
(ex: 'sub.test'=>'test' 'test'=>'test')"
  [s]
  (if (> (first-point-index s) 0)
    (.substring s (inc (first-point-index s)))
    s))

(defn remove-sub-obj-id
  "Remove the first sub object id from elements in map 
(ex: {'sub.name'} => {'name'} {'sub.name.sub'} => {'name.sub'})"
  [m]
  (reduce #(assoc %1 (s-after-first-point (first %2)) (s-after-first-point (second %2))) {} (vec m)))

(deftest remove-sub-obj-id-tests
  (is (= (remove-sub-obj-id {"test" "test" "sub.test1" "sub2.test"}) 
         {"test" "test" "test1" "test"})))

(defn evaluate-swap-mapping
  "Create a map with 
   :superObject The properties mapping for the object
   :subProperty1
    ....
   :subPropertyn All the map for subProperty objects of object"
  [m]
  (let [maps-partitioned (partition-map-in-maps-by m  #(.contains (first %) "."))]
    (reduce #(assoc %1 (keyword-for-sub-object %2) 
                    (remove-sub-obj-id %2)) {} maps-partitioned)))

(deftest evaluate-swap-mapping-tests
  (is (= (evaluate-swap-mapping sourcePropertyMap) {:superObject sourcePropertyMap}))
  (is (= (evaluate-swap-mapping mappingFull) {:superObject sourcePropertyMap
                                              :subSources-subTargets subSourceMap})))
