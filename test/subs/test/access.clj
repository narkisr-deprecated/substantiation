(ns subs.test.access
  (:use midje.sweet)
  (:require [subs.access :as a :refer (keyz get-in*)]))

(fact ":subs/ANY keyz " 
 
  (keyz {:aws {:limits 1} :proxmox {}} [:subs/ANY :limits]) => '((:aws :limits) (:proxmox :limits))
 
  (keyz {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:subs/ANY :subs/ANY :subs/ANY :limits])
      => '((:a :dev :aws :limits) (:a :dev :proxmox :limits) (:a :prod :docker :limits))

  (keyz {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY]) => 
      '((:a :dev :aws) (:a :dev :proxmox))
  )

(fact "non sub keys"
  (keyz {:dev {:aws {:limits 1} :proxmox {}}} [:a :dev :foo]) => '((:a :dev :foo))

  (keyz {:a {:dev {:aws {:limits 1}}}} [:a :dev :aws :limits]) => '((:a :dev :aws :limits))

  (keyz {:machine {:templates {}}} [:machine :templates]) => '((:machine :templates)))

(fact "get-in*" filters
  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:subs/ANY :subs/ANY :subs/ANY :limits]) =>
      '(1 nil 2)
  (get-in* {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}} [:subs/ANY :subs/ANY :limits]) => 
      '(1 nil 2)
  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :aws :limits]) => '(1)
      
  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY :limits]) => '(1 nil)

  (get-in* {:a {:dev {:aws {:limits 1} :proxmox {}} :prod {:docker {:limits 2}}}} [:a :dev :subs/ANY]) => '({:limits 1} {}))
