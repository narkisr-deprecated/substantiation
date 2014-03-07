(ns subs.access
  "validations data access"
 )

(defn keyz
  "recursive map keys, ANY keys cause a fan out to all keys at the current level"
   [m [k & ks]]
   (if (= k :subs/ANY)
    (mapcat
      (fn [[k' v']] 
        (mapv 
          #(if % (conj [k'] %) k') (keyz v' ks))) m)
     (if (map? (m k)) 
        (map #(conj [k] %) (keyz (m k) ks)) [k])))

(defn get-in*
  "like core get-in fans out ANY keys to all values at level" 
   [m ks]
    (let [kz (map flatten (keyz m ks))]
      (map (partial get-in m) kz)))

