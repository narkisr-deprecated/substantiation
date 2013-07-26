(ns subs.test.core
  (:use subs.core midje.sweet))

(fact "basic validations"
  (validate! {:machine nil} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be present")}}
  (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be a string")}})

(fact "order does not matter"
  (validate! {:machine {:ip 1}} {:machine {:ip #{:required :String}}})) => {:machine {:ip '("must be a string")}}

(fact "composition"
  (let [ v1 {:machine {:ip #{:String :required} :names #{:Vector}} :vcenter {:pool #{:String}}} 
         v2 {:machine {:ip #{:String :required} :names #{:required} :level #{:level}}}] 
    (validate! {:machine {:names {:foo 1} :ip 1}} (combine v2 v1)) => 
         {:machine {:ip '("must be a string"), :names '("must be a vector")}}  ))  
