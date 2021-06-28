(ns validate-cpf.core
  (:gen-class))

(require '[validate-cpf.server.server :as validate-server])


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (validate-server/start))
