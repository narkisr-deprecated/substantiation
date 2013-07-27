(ns subs.test.core
  (:import clojure.lang.ExceptionInfo)
  (:use subs.core midje.sweet))

(fact "base validations"
  (validate! {:machine nil} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be present")}}
  (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be a string")}}
  (validate! {:machine {:names "1"}} {:machine {:names #{:Vector :required}}}) => {:machine {:names '("must be a vector")}})

(fact "all just fine" 
  (validate! {:machine {:ip "1.2.3.4"}} {:machine {:ip #{:String :required}}}) => {})

(fact "order does not matter"
  (validate! {:machine {:ip 1}} {:machine {:ip #{:required :String}}})) => {:machine {:ip '("must be a string")}}

(fact "missing custom validation"
  (let [v {:machine {:ip #{:String :required} :names #{:required} :level #{:level}}}] 
    (validate! {:machine {:names {:foo 1} :ip 1}} v) => (throws ExceptionInfo)))

(fact "composition"
  (let [ v1 {:machine {:ip #{:String :required} :names #{:Vector}} :vcenter {:pool #{:String}}} 
         v2 {:machine {:ip #{:String :required} :names #{:required} }}]
    (validate! {:machine {:names {:foo 1} :ip 1}} (combine v2 v1)) => 
         {:machine {:ip '("must be a string"), :names '("must be a vector")}}  ))  

(fact "with error" 
   (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}} :error ::non-vaild-machine) => (throws ExceptionInfo))
