(ns estudo-api-clojure-davvi.servidor
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(defn crirar-tarefa-mapa [uuid nome status]
  {:uuid uuid :nome nome :status status})

(def store (atom {}))
;exemplo:
;{id {tarefa_id tarefa_nome tarefa_status}}
(defn criar-tarefa [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        tarefa (crirar-tarefa-mapa uuid nome status)]
    (swap! store assoc uuid tarefa)
    {:status 200 :body {:mensagem "tarefa registrada comsucesso!"
                        :tarefa tarefa}}
    ))

(defn listar-tarefas [request]
  {:status 200 :body @store})

(defn funcao-hello [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world ]
                ["/tarefa" :post criar-tarefa :route-name :criar-tarefa ]
                ["/tarefa" :get listar-tarefas :route-name :listar-tarefas ]}))




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
(println (test-request :get "/hello?name=Davvi"))
(println (test-request :post "/tarefa?name=Correr&status=pendente"))
(println (test-request :post "/tarefa?name=Correr&status=feito"))
(println "listando todas as tarefas:")
(println (test-request :get "/tarefa"))

