(ns subs.test.core
  (:import clojure.lang.ExceptionInfo)
  (:use subs.core midje.sweet))

(fact "base validations"
  (validate! {:machine nil} {:machine {:ip #{:String :required}}}) => 
      {:machine {:ip "must be present"}}
  (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}}) =>
      {:machine {:ip "must be a string"}}
  (validate! {:machine {:names "1"}} {:machine {:names #{:Vector :required}}}) =>
      {:machine {:names "must be a vector"}}
  (validate! {:machine {:used "1"}} {:machine {:used #{:Boolean :required}}}) =>
      {:machine {:used "must be a boolean"}})

(fact "non empty validations"
  (validate! {:machine {:ip ""}} {:machine {:ip #{:String! :required}}}) =>
      {:machine {:ip "must be a non empty string"}}
  (validate! {:machine {:names ""}} {:machine {:names #{:Vector! :required}}}) =>
      {:machine {:names "must be a non empty vector"}}
  (validate! {:machine {:templates {}}} {:machine {:templates #{:Map! :required}}}) =>
      {:machine {:templates "must be a non empty map"}})

(fact "all just fine" 
  (validate! {:machine {:ip "1.2.3.4" :used false}} {:machine {:ip #{:String :required} :used #{:Boolean}}}) => {})

(fact "multiple errors" 
   (validate! {:foo false :bar ""} {:foo #{:String} :bar #{:Integer}}) => 
      {:foo "must be a string" :bar "must be a integer"}

   (validate! {:hypervisor {:dev {:aws {}}}} 
     {:hypervisor {:dev {:aws {:secret-key #{:required :String} :access-key #{:required :String}}}}}) => 
    {:hypervisor {:dev {:aws {:access-key "must be present", :secret-key "must be present"}}}}
  )

(fact "booleans" 
   (validate! {:foo false} {:foo #{:Boolean :required}}) => {}
   (validate! {:foo true} {:foo #{:Boolean :required}}) => {}
  )

(fact "booleans" 
   (validate! {:foo false} {:foo #{:Boolean :required}}) => {}
   (validate! {:foo true} {:foo #{:Boolean :required}}) => {}
   (validate! {:foo "true"} {:foo #{:Boolean :required}}) => {:foo "must be a boolean"})

(fact "order does not matter"
  (validate! {:machine {:ip 1}} {:machine {:ip #{:required :String}}})) => {:machine {:ip "must be a string"}}

(fact "missing custom validation"
  (let [v {:machine {:ip #{:String :required} :names #{:required} :level #{:level}}}] 
    (validate! {:machine {:names {:foo 1} :ip 1}} v) => (throws ExceptionInfo)))

(fact "composition"
  (let [ v1 {:machine {:ip #{:String :required} :names #{:Vector}} :vcenter {:pool #{:String}}} 
         v2 {:machine {:ip #{:String :required} :names #{:required} }}]
    (validate! {:machine {:names {:foo 1} :ip 1}} (combine v2 v1)) => 
         {:machine {:ip "must be a string", :names "must be a vector"}}  ))  

(fact "subtractions"
  (let [ v1 {:machine {:ip #{:String :required} :names #{:Vector}} :vcenter {:pool #{:String}}} 
         v2 {:machine #{:ip}}]
    (subtract v1 v2) =>  {:machine {:names #{:Vector}} :vcenter {:pool #{:String}}} ))

(fact "with error" 
   (validate! {:machine {:ip 1}} {:machine {:ip #{:String :required}}} :error ::non-vaild-machine) => (throws ExceptionInfo))

(validation :named-node* (every-kv {:ip #{:required} :names #{:name*}}))

(validation :node* (every-kv {:ip #{:required}}))

(validation :name* (every-v #{:String :required}))

(fact "every item validations"

  (validate! {:proxmox {:nodes {:master {} :slave {}}}} {:proxmox {:nodes #{:node*}}}) => 
     {:proxmox {:nodes '({:master {:ip "must be present"}} {:slave {:ip "must be present"}})}}

  (validate! {:proxmox {:nodes {:master {:ip 123}}}} {:proxmox {:nodes #{:node*}}}) => {}

  (validate! {:names [1 "1"]} {:names #{:name*}}) =>  {:names '({0 "must be a string"})}

  (validate! {:names ["1"]} {:names #{:name*}}) =>  {}

  (validate! {:names '("1" "2" 3)} {:names #{:name* :Vector}}) => 
      {:names '("must be a vector" {2 "must be a string"})}

  (validate! {:nodes {:master {} :slave {:names [1]}}} {:nodes #{:named-node*}}) => 
       {:nodes '({:master {:ip "must be present"}} {:slave {:ip "must be present" :names ({0 "must be a string"})}})}

  (validate! {0 1} {0 #{:String}}) => {0 "must be a string"})

(fact "ANY key validations"
   (validate! {:aws {:limits 1} :proxmox {}} {:subs/ANY {:limits #{:required :Integer}}}) => {:proxmox {:limits "must be present"}}

   (validate! {:dev {:aws {:limits 1} :proxmox {}}} {:subs/ANY {:subs/ANY {:limits #{:required :Integer}}}}) => 
      {:dev {:proxmox {:limits "must be present"}}}

   (validate! {:dev {:aws {:limits 1} :proxmox {}}} {:dev {:subs/ANY {:limits #{:required :Integer}}}}) => 
      {:dev {:proxmox {:limits "must be present"}}}

   (validate! {:dev {:aws {:limits 1} :proxmox {}} :qa {} :prod {:aws {:limits ""} :vcenter {}}} {:subs/ANY {:subs/ANY {:limits #{:required :Integer}}}}) => 
      {:dev {:proxmox {:limits "must be present"}} :prod {:aws {:limits "must be a integer"} :vcenter {:limits "must be present"}}}

      )
 
(validation :person {:name #{:String :required} :id #{:Integer :required}} )

(validation :people (every-v #{:person}))

(fact "components" 
   (validate! {:me {:id ""}} {:me #{:person}}) => {:me {:id "must be a integer" :name "must be present" }}
   (validate! {:group [{:id ""}]} {:group #{:people}}) => 
      {:group '({0 {:id "must be a integer", :name "must be present"}})}
  )
