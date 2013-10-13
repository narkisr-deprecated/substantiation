(ns subs.test.core
  (:import clojure.lang.ExceptionInfo)
  (:use subs.core midje.sweet))

(fact "base validations"
  (validate! {:machine nil} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be present")}}
  (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}}) => {:machine {:ip '("must be a string")}}
  (validate! {:machine {:names "1"}} {:machine {:names #{:Vector :required}}}) => {:machine {:names '("must be a vector")}}
  (validate! {:machine {:used "1"}} {:machine {:used #{:Boolean :required}}}) => {:machine {:used '("must be a boolean")}})

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

(validation :named-node* (every-kv {:ip #{:required} :names #{:name*}}))

(validation :node* (every-kv {:ip #{:required}}))

(validation :name* (every-v #{:String :required}))

(fact "every item validations"

  (validate! {:proxmox {:nodes {:master {} :slave {}}}} {:proxmox {:nodes #{:node*}}}) => 
     {:proxmox {:nodes '(({:master {:ip ("must be present")}} {:slave {:ip ("must be present")}}))}}

  (validate! {:proxmox {:nodes {:master {:ip 123}}}} {:proxmox {:nodes #{:node*}}}) => {}

  (validate! {:names [1 "1"]} {:names #{:name*}}) =>  {:names '(({0 ("must be a string")}))}

  (validate! {:names ["1"]} {:names #{:name*}}) =>  {}

  (validate! {:names '("1" "2" 3)} {:names #{:name* :Vector}}) => '{:names (({2 ("must be a string")}) "must be a vector")}

  (validate! {:nodes {:master {} :slave {:names [1]}}} {:nodes #{:named-node*}}) => 
       {:nodes '(({:master {:ip ("must be present")}} {:slave {:ip ("must be present") :names (({0 ("must be a string")}))}}))}

  (validate! {0 1} {0 #{:String}}) => {0 '("must be a string")}
  )
