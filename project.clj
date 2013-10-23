(defproject beans "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :java-source-paths ["java/src"]
  :aot :all
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
  
