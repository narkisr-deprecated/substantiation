(ns subs.exp
  "Expanding validations"
  (:require 
    [clojure.tools.trace :refer (trace)]))

(defn expandable?
   "checks is there an expanable item" 
   [s registry]
  (first (filter #(-> % registry map?) s)))

(defn base?
   "checks is are any base items" 
   [s registry]
  (first (filter #(-> % registry fn?) s)))

(defn mixed? 
   "includes both expandable and base validations" 
   [s registry]
  (and (expandable? s registry) (base? s registry)))

(declare expand)

(defn expand-set 
   "expand a single validations set level"
   [s registry]
   {:pre [(not (mixed? s registry))]}
   (if (expandable? s registry)
    (reduce 
      (fn [r v] 
        (merge r (expand (registry v) registry))) {} s) s)) 

(defn expand 
   "Expands nested validations refrences to data" 
   [m registry]
  (reduce 
    (fn [res [k v]]
      (if (map? v)
        (assoc res k (expand v registry))
        (assoc res k (expand-set v registry)))) {} m))

