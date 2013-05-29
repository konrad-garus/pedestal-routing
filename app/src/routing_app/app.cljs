; This is a demo of client-side routing with Google Closure and Pedestal.
;
; It sets up a listener with goog.History which pushes the location token into
; the app queue. That message is passed through default pipeline up to the app
; model. Finally, there is a renderer which listens to such changes in the app
; model and actually performs a DOM manipulation.
;
; This demo shows one generic way to do it - renderer is a reusable HOF 
; configured with a special structure. See below for details. 

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

; Generic route renderer. cfg is a map with the following options:
;
; :routes {"a" function-a "b" function-b} - Call function-a for path a, 
; function-b for path b.
; 
; :listener (fn [old-value new-value] ...) - Whenever route changes, call this
; function.
(defn route-renderer [cfg]
  (fn [_ [_ _ old-value new-value] input-queue]
    (when-let [listener (:listener cfg)]
      (listener old-value new-value))
    (if-let [dispatcher (get-in cfg [:routes new-value])]
      (dispatcher)
      (.log js/console "Unknown route:" new-value))))

; Specific renderers for this demo application, nothing interesting or generic here. 
(defn render-route [msg]
  (let [container (dom/by-id "view-container")]
    (dom/destroy-children! container)
    (dom/append! container (str "<p>" msg "</p>"))))

(defn route-first []
  (render-route "This is the first route"))

(defn route-second []
  (render-route "This is the second route"))

; Some "real", working config.
(def router-config
  {:routes {"first" route-first
            "second" route-second}
   :default-route "first"
   :listener (fn [old-value new-value]
               (.log js/console "Routing from" old-value "to" new-value))})

; The rendering is at least a two-phase process and it needs to be configured in
; two places - the goog.History listener to push messages to input queue, and 
; the renderer which updates DOM as those changes occur. 
(defn ^:export main []
  (let [app (app/build count-app)
        ; Plug in renderer here...
        render-fn (push/renderer "content" [[:value [:route] (route-renderer router-config)]])]
    (render/consume-app-model app render-fn)
    ; ... and configure the history listener here
    (configure-router (:input app) (:default-route router-config))
    (app/begin app)))
