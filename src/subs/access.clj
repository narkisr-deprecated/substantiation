(ns subs.access
  "validations data access")

(defn keyz*
  "non flat version of keyz"
   [m [k & ks]]
   (if (= k :subs/ANY)
    (mapcat
      (fn [[k' v']] 
        (map
          #(if % (cons k' (list %)) k') (keyz* v' ks))) m)
     (if (map? (m k)) 
        (map #(cons k (list %)) (keyz* (m k) ks)) 
        (list (cons k (or ks '()))))))

(keyz* {:aws {:limits 1} :proxmox {}} [:subs/ANY :limits])

(keyz* {:dev {:aws {:limits 1} :proxmox {}}} [:a :dev :foo])

(keyz* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY])

(defn keyz 
  "recursive map keys, ANY keys cause a fan out to all keys at the current level"
   [m ks]
  (map flatten (keyz* m ks)))

(defn get-in*
  "like core get-in fans out ANY keys to all values at level" 
   [m ks]
    (let [kz (keyz m ks)]
      (map (partial get-in m) kz)))

