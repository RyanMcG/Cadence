(ns cadence.config)

(def config
  {:webapp {}
   :storage {:type :mongodb
             :uri (System/getenv "MONGOHQ_URL")
             :username "cadence"
             :password "secret"}})
