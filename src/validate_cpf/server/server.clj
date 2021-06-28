(ns validate-cpf.server.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [ring.util.http-response :as response]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [reitit.ring.coercion :as coercion]
            [reitit.ring :as ring]
            [clj-pdf.core :as pdf]
            [cadastro-de-pessoa.cpf :as cpf-validator]))

(import java.security.MessageDigest)

(defn sha256 [string]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes string "UTF-8"))]
    (apply str (map (partial format "%02x") digest))))


(def validator-api
  ["/validate"
   {:get {:description "Returns the cryptographed string of a valid CPF"
          :handler (fn [request]
                     (let [cpf (-> request :body-params :cpf)]
                          (if (cpf-validator/valid? (str cpf))
                            (do (response/ok {:encrypted (sha256 (str cpf))})
                                (pdf/pdf [{}
                                          [:phrase (sha256 (str cpf))]]
                                         "file.pdf"))
                            (response/not-found))))}}])

(def app
  (ring/ring-handler
   (ring/router
    [validator-api]
    {:data {:muuntaja   m/instance
            :middleware [params/wrap-params
                         muuntaja/format-middleware
                         coercion/coerce-exceptions-middleware
                         coercion/coerce-request-middleware
                         coercion/coerce-response-middleware]}})
   (ring/create-default-handler)))



(defonce running-server (atom nil))

(defn start
  []
  (when (nil? @running-server)
    (reset! running-server (jetty/run-jetty #'app {:port  3000
                                                   :join? false})))
  (println "Server running in port 3000"))

(defn stop
  []
  (when-not (nil? @running-server)
    (.stop @running-server)
    (reset! running-server nil))
  (println "Server stopped"))