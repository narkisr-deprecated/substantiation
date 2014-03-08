(ns subs.access "validations data access")

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

    (= k :subs/ANY) (map list (keys m))

    (and k (map? (m k)) (not (empty? (m k)))) (map #(cons k (list %)) (keyz* (m k) ks))

    (and k ks) (list (add-vals k ks))

    k (list (list k))))

(defn keyz 
  "recursive map keys, ANY keys cause a fan out to all keys at the current level"
   [m ks]
  (map flatten (keyz* m ks)))

(defn get-in*
  "like core get-in fans out ANY keys to all values at level" 
   [m ks]
    (let [kz (keyz m ks)]
      (map (partial get-in m) kz)))

