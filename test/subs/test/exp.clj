(ns subs.test.exp
  "Expansions tests"
  (:use subs.core subs.exp midje.sweet)
 )

(validation :person {:name #{:String :required}})
(validation :trackable {:id #{:Integer :required}})


(fact "sanity"
   (expand {:me {:name #{:String :required} :id #{:Integer :required}}} (registry)) => 
      {:me {:name #{:String :required} :id #{:Integer :required}}}
  )

(fact "single level"
   (expand {:me #{:person :trackable}} (registry)) => 
      {:me {:name #{:String :required} :id #{:Integer :required}}}
  )

(validation :human {:me #{:person :trackable}})

(fact "nested" 
   (expand {:he #{:human}} (registry)) => 
      {:he {:me {:id #{:Integer :required} :name #{:String :required}}}}
  )

(fact "not legal!"
   (expand {:me #{:person :String}} (registry)) => (throws AssertionError)
  )
