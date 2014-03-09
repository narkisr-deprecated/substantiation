(ns subs.access 
  "validations data access"
  (:require 
    [clojure.core.strint :refer (<<)] 
    [slingshot.slingshot :refer (throw+)]))

(defn add-vals 
   [k ks]
  {:test 
   #(assert (= (add-vals :a (list :dev :foo)) (:a (:dev (:foo)))))}
 (reduce 
   (fn [r v] (cons v (list r))) (list (last ks)) (reverse (cons k (butlast ks)))))

(use 'clojure.tools.trace)

(defn keyz*
  "non flat version of keyz"
  [m [k & ks]]
  (cond 
    (and (= k :subs/ANY) ks) 
      (mapcat 
        (fn [[k' inner-map]] 
          (map #(if % (cons k' (list %)) k') (keyz* inner-map ks))) m) 

    (= k :subs/ANY) 
       (map list (keys m))

    (and ks (map? (m k)) (not (empty? (m k)))) 
       (map #(cons k (list %)) (keyz* (m k) ks))

    (and k ks); map does not match ks 
      (throw+ {:message (<< "failed to validate ~{m} since it does not contain key ~{k}") :type ::missing-key })

     k (list (list k)) 

    :else (throw+ {:message (<< "illegal state") :type ::else-clause-reached})
    ))

(defn keyz 
  "recursive map keys, ANY keys cause a fan out to all keys at the current level"
  [m ks]
  (if (first (filter #(= :subs/ANY %) ks))
    (map flatten (keyz* m ks))
    [ks]
    )
  )

(defn get-in*
  "like core get-in fans out ANY keys to all values at level" 
   [m ks]
    (let [kz (keyz m ks)]
      (map (partial get-in m) kz)))

