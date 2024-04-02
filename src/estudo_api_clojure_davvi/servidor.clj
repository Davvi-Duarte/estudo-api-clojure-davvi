(ns estudo-api-clojure-davvi.servidor
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [estudo-api-clojure-davvi.database :as database]))
(defn assoc-store [contexto]
  (update contexto :request assoc :store database/store)
  )

(defn crirar-tarefa-mapa [uuid nome status]
  {:uuid uuid :nome nome :status status})


;exemplo:
;{id {tarefa_id tarefa_nome tarefa_status}}
(defn criar-tarefa [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        tarefa (crirar-tarefa-mapa uuid nome status)
        store (:store request)]
    (swap! store assoc uuid tarefa)
    {:status 200 :body {:mensagem "tarefa registrada com sucesso!"
                        :tarefa   tarefa}}
    ))


(def db-interceptor
  {:name  :db-interceptor
   :enter assoc-store})
(defn listar-tarefas [request]
  {:status 200 :body @(:store request)})

(defn deleta-tarefa [request]
  (let [store (:store request)
        tarefa-id (get-in request [:path-params :id])
        tarefa-id-uuid (java.util.UUID/fromString tarefa-id)]
    (swap! store dissoc tarefa-id-uuid)
    {:status 200 :body {:mensagem "Removida com sucesso"}}))

(defn atualiza-tarefa [request]
  (let [tarefa-id (get-in request [:path-params :id])
        tarefa-id-uuid (java.util.UUID/fromString tarefa-id)
        nome (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        tarefa (crirar-tarefa-mapa tarefa-id-uuid nome status)
        store (:store request)]
    (swap! store assoc tarefa-id-uuid tarefa)
    {:status 200 :body {:mensagem "tarefa atualizada com sucesso!"
                        :tarefa   tarefa}}
    ))

(defn funcao-hello [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world]
                ["/tarefa" :post [db-interceptor criar-tarefa] :route-name :criar-tarefa]
                ["/tarefa" :get [db-interceptor listar-tarefas] :route-name :listar-tarefas]
                ["/tarefa/:id" :delete [db-interceptor deleta-tarefa] :route-name :deleta-tarefa]
                ["/tarefa/:id" :patch [db-interceptor atualiza-tarefa] :route-name :atualiza-tarefa]
                }))


(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})                     ;permite o não travamento da thread do clojure. útil para desenvolvimento (testes)

(def server (atom nil))

(defn start-server []
  (reset! server (http/start (http/create-server service-map)))
  )

(defn test-request
  [verb url]
  (test/response-for (::http/service-fn @server) verb url))


(defn stop-server []
  (http/stop @server))

(defn restart-server []
  (stop-server)
  (start-server))

(start-server)
(println "Started HTTP Server")
(println (test-request :get "/hello?name=Davvi"))
(println (test-request :post "/tarefa?name=Correr&status=pendente"))
(println (test-request :post "/tarefa?name=ler&status=feito"))

;TESTES PARA DELETAR E ATUALIZAR
;(test-request :delete "/tarefa/<INSERIR UUID AQUI SEM AS CHAVES>")
;(test-request :patch "/tarefa/UUID?name=novonome&status=novostatus)

(println "listando todas as tarefas:")
(println (clojure.edn/read-string (:body (test-request :get "/tarefa"))))

