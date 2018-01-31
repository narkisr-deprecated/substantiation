(ns subs.access
  "validations data access"
  (:require
    [clojure.core.strint :refer (<<)])
  (:import clojure.lang.ExceptionInfo))

(defn add-vals
   [k ks]
  {:test
   #(assert (= (add-vals :a (list :dev :foo)) (:a (:dev (:foo)))))}
 (reduce
   (fn [r v] (cons v (list r))) (list (last ks)) (reverse (cons k (butlast ks)))))

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

    (and m ks (map? (m k)) (not (empty? (m k))))
       (map #(cons k (list %)) (keyz* (m k) ks))

    (and k ks); map does not match ks
      (throw (ex-info "could not match keys" {:type ::missing-key}) )

     k (list (list k))

    :else (throw (ex-info "illegal state" {:type ::else-clause-reached}))
    ))



(defn keyz
  "recursive map keys, ANY keys cause a fan out to all keys at the current level"
  [m ks]
  (if (first (filter #(= :subs/ANY %) ks))
    (map flatten
      (try (keyz* m ks)
        (catch ExceptionInfo e
          (when (= (-> e bean :data :object :type) ::missing-key)
           (throw
             (ex-info (<< "Could not get ~{ks} from ~{m} since it does not match map structure") {:type ::any-key-match-error})))
          (throw e)
          )))
    [ks]
    )
  )

(defn get-in*
  "like core get-in fans out ANY keys to all values at level"
  [m ks]
   (map (partial get-in m) (keyz m ks)))
