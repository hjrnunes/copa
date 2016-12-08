(ns copa.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.auth.backends.token :as t]))

(def secret "mysupersecret")

(defn jws-token [claims]
  (jwt/sign claims secret {:alg :hs512}))

(defn jws-backend []
  (t/jws-backend {:secret secret :options {:alg :hs512}}))
