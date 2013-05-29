; This is a demo of client-side routing with Google Closure and Pedestal.
;
; It sets up a listener with goog.History which pushes the location token into
; the app queue. That message is passed through default pipeline up to the app
; model. Finally, there is a renderer which listens to such changes in the app
; model and actually performs a DOM manipulation.
;
; This is a very small example, but it can be easily generalized and extended on
; all stages. It is possible to do something special before leaving a route. You
; can plug in any rendering mechanism you like. You can make the configuration
; generic, like a vector of vectors mapping from location to function, template
; name, a combination, or whatever you like.

(ns routing-app.app
  (:require [io.pedestal.app :as app]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.render.push :as push]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.render.events :as events]
            [domina.events :as dom-event]
            [domina :as dom]
            [goog.History :as history]
            [goog.events :as goog-event]))

; Apparently both of those things are necessary. All they do is push the :route
; message up to the app model
(def count-app {:transform {:route {:init nil :fn #(:value %2)}}
                :emit {:router {:fn app/default-emitter-fn :input #{:route}}}})

(defn ^:private set-route [input-queue route]
  (p/put-message input-queue {msg/topic :route msg/type :set-route :value route}))

; Bridge to goog.History from Closure. All it really does is take the path token
; and push it in a :route message to the input queue.
(defn configure-router 
  ([input-queue] (configure-router input-queue ""))
  ([input-queue default-route]
    (doto (goog.History.)
      (goog.events/listen (goog.object/getValues goog.history/EventType) 
                          (fn [e]
                            (let [token (.-token e)]
                              (if (= "" token)
                                (set-route input-queue default-route)
                                (set-route input-queue token)))))
    (.setEnabled true))))

; Renderer reacting to route changes. This one is an ugly hardcoded thing, but
; it could be a reusable HOF or generic function that takes some kind of 
; configuration depending on needs.
(defn route-changed [_ [_ _ old-value new-value] input-queue]
  ; This bit could be extracted to a generic listener callback
  (.log js/console "Routing from" old-value "to" new-value)
  (let [container (dom/by-id "view-container")]
    (dom/destroy-children! container)
    (dom/append! container (str "<p>" new-value "</p>"))))

(defn ^:export main []
  (let [app (app/build count-app)
        render-fn (push/renderer "content" [[:value [:route] route-changed]])]
    (render/consume-app-model app render-fn)
    (configure-router (:input app) "first")
    (app/begin app)))
