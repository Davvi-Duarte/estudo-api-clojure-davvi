(ns estudo-api-clojure-davvi.servidor
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))



(defn funcao-hello [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})
(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world ]}))
(def service-map {::http/routes routes
                  ::http/port 9999
                  ::http/type :jetty
                  ::http/join? false});permiteo não travamento da thread do clojure. útil para desenvolvimento (testes)

(def server (atom nil))


(defn start-server []
  (reset! server (http/start (http/create-server service-map)))
  )

(defn test-request
  [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(println "Started HTTP Server")
(test-request :get "/hello?name=Davvi")
