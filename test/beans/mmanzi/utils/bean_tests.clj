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
(def ^:private subSourceMap {"subName"   "subTargetName"})

(def ^:private mappingFull 
  (merge sourcePropertyMap
         (zipmap (map #(str "subSources." %) (keys subSourceMap))
                 (map #(str "subTargets." %) (vals subSourceMap)))))


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

(defn evaluate-swap-mapping
  "Create a map with 
   :superObject The properties mapping for the object
   :subProperty1
    ....
   :subPropertyn All the map for subProperty objects of object"
  [m]
  {:superObject m})

(deftest evaluate-swap-mapping-tests
  (is (= (evaluate-swap-mapping sourcePropertyMap) {:superObject sourcePropertyMap}))
  (is (= (evaluate-swap-mapping sourcePropertyMap) {:superObject sourcePropertyMap
                                                    :subSources-subTargets subSourceMap})))

{"subObject.name" "subTargetObject.targetName"
 "subObject.property" "subTargetObject.property"
 "subObject.property2" "subTargetObject.property2@"}
;(map #(split % #"[.]") (vals m))

;test.getClass().getMethod("getSubTargets").toGenericString()
;(partition-by #(> (.indexOf (str (first %)) ".") 0) (vec {"a" 1 "b" 2 "c.d" 3 "c.d.e" 4}))