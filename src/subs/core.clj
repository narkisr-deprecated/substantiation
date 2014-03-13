(comment
  Substantiation, Copyright 2012 Ronen Narkis, narkisr.com
  Licensed under the Apache License,
  Version 2.0  (the "License") you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.)

(ns subs.core
  " Substantiation is an opinionated simple nested map validation framework:
  * Predicates and description kept seperate.
  * Validations map description sturcture follows validated input.
  * Pure datastructures to describe validations.
  * Composeability of validations should be trivial.
  * Validation predicates scope is limited (can only access the checked value).
  * High level decisions such as when to activate a group of validations should happen on upper layer.
  * Non strict, only described items checked. "
  (:use 
    [subs.access :refer (get-in* keyz)]
    [slingshot.slingshot :only  (throw+)]
    [clojure.core.strint :only (<<)] 
    [clojure.set :only (union)]))

(defn- deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn- deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.

  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
  {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    maps))

(defn- flatten-keys*
  "Flatten map keys into vectors, used to calculate fast access vectors from descriptions"
  [a ks m]
  (if (map? m)
    (reduce into (map (fn [[k v]] (flatten-keys* a (conj ks k) v)) (seq m)))
    (assoc a ks m)))

(def ^:private flatten-mem (memoize flatten-keys*))

(defn- flatten-keys 
  "memoized version that spares us the need to calculate the access vector each time"
  [m] (flatten-mem {} [] m))

(def not-nil (comp not nil?))

(def not-empty* (comp not empty?))

(defmacro when-not-nil 
  "returns an fn that applies body on pred if values isn't nil else nil"
  [pred & body]
  `(fn [v#] 
     (when (and (not-nil v#) (not (~pred v#)))
       ~@body
       )))

(defmacro when*
  "returns an fn that applies body on pred else nil"
  [pred & body]
  `(fn [v#] 
     (when (~pred v#)
       ~@body
       )))

(def ^:private base {
   :String! (when-not-nil (every-pred string? not-empty) "must be a non empty string")                     
   :String (when-not-nil string? "must be a string")                     
   :Integer  (when-not-nil integer? "must be a integer")                     
   :Boolean (when-not-nil (partial contains? #{true false})  "must be a boolean")                     
   :Vector  (when-not-nil vector? "must be a vector")                     
   :Vector!  (when-not-nil (every-pred vector? not-empty) "must be a non empty vector")                     
   :Map  (when-not-nil map? "must be a map")                     
   :Map!  (when-not-nil (every-pred map? not-empty) "must be a non empty map")                     
   :Set  (when-not-nil set? "must be a set")                     
   :Set!  (when-not-nil (every-pred set? not-empty) "must be a non empty set")                     
   :Keyword  (when-not-nil keyword? "must be a keyword")                     
   :sequential  (when-not-nil sequential? "must be sequential")                     
   :number  (when-not-nil number? "must be a number")                     
   :required  (when* nil? "must be present")})

(def ^:private externals (atom {}))

(def filter-empty (partial filter not-empty))

(defn- run-vs 
  "Runs a set of validations on a value" 
  [vs [k value]] 
  {:pre [(set? vs)]}
  (let [merged (merge base @externals)]
    (filter (fn [[k e]] (not (empty? e)))
      (map 
        (fn [t] 
          (if-let [v (merged t)] 
            [k (v value)] 
            (throw+ {:message (<< "validation of type ~{t} not found, did your forget to define it?") :type ::missing-validation }))) vs))))

(use 'clojure.tools.trace)

(defn run-validations 
   "goes through validations" 
   [m errors [k vs]]
  (let [key-vals (zipmap (keyz m k) (get-in* m k)) 
        es (mapcat (partial run-vs vs) key-vals)]
    (deep-merge errors
      (reduce 
        (fn [res [k' message]]
          (if message (merge-with merge res (assoc-in res k' message)) res)) {} es))))

(defn validate! 
  "validates a map with given validations, returns error map (or execption see :error) 
  options:
  :error - will throw exception of type :error if provided (validate! t m :error ::non-valid-m)" 
  ([m validations & opts]
   (let [{:keys [error]} (apply hash-map opts) errors (validate! m validations)]
     (if (and error (-> errors empty? not))
       (throw+ {:type error :errors errors}) 
        errors
       )))
  ([m validations]
   (reduce (partial run-validations m) {} (flatten-keys validations))))

(defn every-kv 
  "Every key value validation helper"
  [vs]
  (fn [m] 
    (filter-empty
       (map (fn [[k v]] 
         (let [res (validate! v vs)] (when (not-empty res) {k res}))) m))))

(defn every-v 
  "Every value validation helper fn"
  [vs]
  (fn [s] (filter-empty (map-indexed (fn [i v] (validate! {i v} {i vs})) s))))

(defn validation 
  "define an custom validation with type and predicate"
  [type pred]
  (swap! externals assoc type pred))

(defn combine 
  "Combines a seq of validation descriptions"
  [& ds]
  (apply deep-merge-with union ds))
