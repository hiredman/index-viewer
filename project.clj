(defproject index-viewer "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [ring "0.3.2"]
                 [clj-http "0.1.3"]
                 [org.danlarkin/clojure-json "1.1"]
                 [hiccup "0.3.5"]]
  :dev-dependencies [[lein-ring "0.4.3"]]
  :ring {:handler index.viewer/foo})
