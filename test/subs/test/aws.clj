(ns subs.test.aws
  (:import clojure.lang.ExceptionInfo)
  (:use midje.sweet)
  (:require 
    [clojure.core.strint :refer (<<)]
    [subs.core :as subs :refer (validate! combine every-v every-kv validation when-not-nil)])
  )


(def machine-entity
  {:machine {
     :hostname #{:required :String} :domain #{:required :String} 
     :user #{:required :String} :os #{:required :Keyword} 
  }})

(def ebs-type #{"io1" "standard" "gp2"})

(validation :ebs-type
  (when-not-nil (<< "EBS type must be either ~{ebs-type}")))

(validation :volume {
    :device #{:required :String} :size #{:required :Integer}
    :clear #{:required :Boolean} :volume-type #{:required :ebs-type}
   })

(validation :iops 
  (fn [{:keys [volume-type iops]}] 
    (when (and (= volume-type "io1") (nil? iops)) "iops required if io1 type is used"))) 

(validation :volume* (every-v #{:volume}))

(validation :group* (every-v #{:String}))

(validate! {:volumes {:device "do"}} {:volumes #{:volume}} )
(validate! {:volumes {:device "do"}} {:volumes {:device #{:Integer}}} )

#_(validate! {:groups [{:device "do"}]} {:groups #{:group*}} )

(def aws-entity
  {:aws {
     :instance-type #{:required :String} :key-name #{:required :String}
     :endpoint #{:required :String} :volumes #{:volume*}
     :security-groups #{:Vector :group*} :availability-zone #{:String}
     :ebs-optimized #{:Boolean}
    }})


(defn validate-entity 
  "aws based systems entity validation " 
  [aws]
  (validate! aws (combine machine-entity aws-entity) :error ::invalid-system))


(def aws-provider
  {:instance-type #{:required :String} :key-name #{:required :String}
   :placement {:availability-zone #{:String}} :security-groups #{:Vector :group*}
   :min-count #{:required :Integer} :max-count #{:required :Integer} 
   :ebs-optimized #{:Boolean}
   })

(def redis-ec2-spec {
  :env :dev 
  :owner "admin"

  :machine {
   :hostname "red1" :user "ubuntu" 
   :domain "local" :os :ubuntu-12.10
  }

  :aws {
   :instance-type "t1.micro" 
   :key-name "Uranus" 
   :endpoint "ec2.eu-west-1.amazonaws.com"
   :ebs-optimized false
   }

  :type "redis"
})

#_(validate! 
  (merge-with merge redis-ec2-spec {:aws {:volumes [{:device "do"}]}})
  (combine machine-entity aws-entity) :error ::invalid-system)

