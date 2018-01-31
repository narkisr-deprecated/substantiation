(ns subs.test.access
  (:import clojure.lang.ExceptionInfo)
  (:use midje.sweet)
  (:require [subs.access :as a :refer (keyz get-in*)]))

(fact ":subs/ANY keyz "

  (keyz {:aws {:limits 1} :proxmox {}} [:subs/ANY :limits]) => '((:aws :limits) (:proxmox :limits))

  (keyz {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:subs/ANY :subs/ANY :subs/ANY :limits])
      => '((:a :dev :aws :limits) (:a :dev :proxmox :limits) (:a :prod :docker :limits))

  (keyz {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY]) =>
      '((:a :dev :aws) (:a :dev :proxmox)))

(fact "non sub keys"

  (keyz {:a {:dev {:aws {:limits 1}}}} [:a :dev :aws :limits]) => '((:a :dev :aws :limits))

  (keyz {:machine {:templates {}}} [:machine :templates]) => '((:machine :templates))

  (keyz {:machine {:names {:foo 1} :ip 1}} [:machine :names]) => '((:machine :names)))

(fact "map does not match keys"
  (keyz {:dev {:aws {:limits 1} :proxmox {}}} [:a :subs/ANY :foo]) => (throws ExceptionInfo)

  (keyz {:username "foo" :quotas {:proxmox {:used nil} }} [:quotas :subs/ANY :subs/ANY :used :count]) => (throws ExceptionInfo)

  (get-in* {:username "foo" :quotas {:proxmox {:used nil} }} [:quotas :subs/ANY :subs/ANY :used :count]) => (throws ExceptionInfo))


(fact "get-in*" filters
  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:subs/ANY :subs/ANY :subs/ANY :limits]) =>
      '(1 nil 2)
  (get-in* {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}} [:subs/ANY :subs/ANY :limits]) =>
      '(1 nil 2)
  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :aws :limits]) => '(1)

  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY :limits]) => '(1 nil)

  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY]) => '({:limits 1} {}))
