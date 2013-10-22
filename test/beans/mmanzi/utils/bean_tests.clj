(ns beans.mmanzi.utils.bean_tests
  (use [clojure.test]
       [clojure.string]
       [beans.mmanzi.utils.bean]))

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
(def ^:private subSourceWithHisPosInfoMap {"subSources.subName"   "subTarget.subTargetName"})

(deftest copy-properties-in-map!-tests
  (is (= (let [t create-target
               t (copy-field-in-map! sourcePropertyMap source0 t)]
           (get-f t "targetName")) "testName")))

(deftest copy-source-sub-object!-tests
  (is (= (let [t create-sub-target
               t (copy-field-in-map! subSourceMap ((get-f source0 "subSources") 0) t)]
           (get-f t "subTargetName")) "sub1")))


{"subObject.property" "subTargetObject.property"
 "subObject.property2" "subTargetObject.property2@"}
;(map #(split % #"[.]") (vals m))

(deftest full-copy-source!-tests
  (is (= (let [t create-target
               _ (copy-field-in-map! sourcePropertyMap source0 t)
               _ (set-f t "subTargets" (map #(let [st (create "test.objecs.SubTarget")]
                         (copy-field-in-map! subSourceMap % st)) (get-f source0 "subSources")))]
           (get-f t "subTargets") 1)) "sub2"))

(deftest get-f-type!-tests
  (is (= (let [t create-target]
           (get-f-type t "subTargets")) java.util.List)))

(deftest get-f-list-type-str!-tests
  (is (= (let [t create-target]
           (get-f-type t "subTargets")) "test.objects.SubTarget")))
;test.getClass().getMethod("getSubTargets").toGenericString()
;(partition-by #(> (.indexOf (str (first %)) ".") 0) (vec {"a" 1 "b" 2 "c.d" 3 "c.d.e" 4}))