(ns cadence.model.validators)

(defn user?
  "Returns whether the given user is valid or not."
  [user]
  (let [uc? (partial contains? user)]
    (and (uc? :password)
         (uc? :username))))
