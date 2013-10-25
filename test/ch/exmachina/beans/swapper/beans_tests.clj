(ns ch.exmachina.beans.swapper.beans-tests
  (use [clojure.test]
       [ch.exmachina.beans.swapper.beans]
       [ch.exmachina.beans.swapper.sub-obj-map-split])
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
               t (copyFieldsWithMapping sourcePropertyMap source0 t)]
           (get-f t "targetName")) "testName")))

(deftest copy-source-sub-object!-tests
  (is (= (let [t create-sub-target
               t (copyFieldsWithMapping subSourceMap ((get-f source0 "subSources") 0) t)]
           (get-f t "subTargetName")) "sub1")))

(deftest full-copy-source!-tests
  (is (= (let [t create-target
               _ (copyFieldsWithMapping sourcePropertyMap source0 t)
               _ (set-f t "subTargets" (map #(let [st (create "test.objecs.SubTarget")]
                         (copyFieldsWithMapping subSourceMap % st)) (get-f source0 "subSources")))]
           (get-f t "subTargets") 1)) "sub2"))

(deftest partition-map-in-maps-by-tests
  (is (= (partition-map-in-maps-by {"test" "test" "withPoint.test" "withPoint.test"} #(.contains (first %) ".")) 
         [{"test" "test"}{"withPoint.test" "withPoint.test"}])))

(deftest evaluate-swap-mapping-tests
  (is (= (evaluate-swap-mapping sourcePropertyMap) sourcePropertyMap))
  (is (= (evaluate-swap-mapping mappingFull) (merge sourcePropertyMap
                                              {"subSources-subTargets" subSourceMap}))))

(deftest get-f-type-tests
  (is (= (let [t create-target]
           (get-f-type t "subTargets")) "java.util.List")))

(deftest get-f-list-type-str-tests
  (is (= (let [t create-target]
           (get-f-list-type-str t "subTargets")) "test.objects.SubTarget"))
  (is (= (let [s source0]
           (get-f-list-type-str s "subSources")) "test.objects.SubSource")))

(deftest copyWithMapping-tests
  (is (let [t (copyWithMapping mappingFull source0 "test.objects.Target")]
         (= (get-f t "targetName") "testName")))
  (is (let [t (copyWithMapping sourcePropertyMap source0 "test.objects.Target")]
         (= (get-f t "targetName") "testName")))
  (is (let [t (copyWithMapping mappingFull source0 "test.objects.Target")]
         (= (get-f ((vec (get-f t "subTargets")) 0) "subTargetName") "sub1")))
  (is (let [t (copyWithMapping mappingFull source0 "test.objects.Target")]
         (= (get-f ((vec (get-f t "subTargets")) 0) "subTargetName") "sub1"))))

  


