(ns estudo-api-clojure-davvi.core
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]))


(defn funcao-hello [request]
    {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})
(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world ]}))
(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty
                  ::http/join? false});permiteo não travamento da thread do clojure. útil para desenvolvimento (testes)

(http/start (http/create-server service-map))
(println "Started server http")