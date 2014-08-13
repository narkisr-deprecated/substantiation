(defproject substantiation "0.2.1"
  :description "An opinionated nested maps validations framework"
  :url "https://github.com/narkisr/substantiation"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.5.1"] 
                 [slingshot "0.10.3"] 
                 [org.clojure/core.incubator "0.1.2"]]

  :exclusions [org.clojure/clojure]

  :profiles {
   :dev {
      :source-paths  ["dev" "src"]
      :dependencies [[org.clojure/tools.trace "0.7.5"] [midje "1.5.1"]]
      :set-version {
        :updates [{:path "README.md" :search-regex #"\"\d+\.\d+\.\d+\""}]
      }
    }
  }

                   
  :aliases {"autotest"
            ["midje" ":autotest" ":filter" "-integration"]
            "runtest"
            ["midje" ":filter" "-integration"] 
            }

  :plugins  [[jonase/eastwood "0.0.2"] [lein-midje "3.0.0"] [lein-ancient "0.4.2"]
             [lein-tag "0.1.0"] [lein-set-version "0.3.0"] [codox "0.6.4"]]

 
  )

