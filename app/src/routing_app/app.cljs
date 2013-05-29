; This is a demo of client-side routing with Google Closure and Pedestal.
;
; It sets up a listener with goog.History. As soon as the location changes, it
; determines the right action based the configuration provided and executes it.
;
; This demo is limited to the rendering layer, but the handlers can push 
; messages to the input queue if desired.

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

; Generic view-only route renderer. Config is a map with the following options:
;
; :routes {"a" function-a "b" function-b} - Call function-a for path a, 
; function-b for path b.
; 
; :listener (fn [old-value new-value] ...) - Whenever route changes, call this
; function.
;
; :default-route "my-route" - When the route is empty, use "my-route".
(defn ^:private route-changed [{:keys [input]} route-config]
  (fn [e]
    (let [token (.-token e)
          token (if (= "" token) (:default-route route-config) token)]
      (when-let [listener (:listener cfg)]
        (listener token))
      (if-let [dispatcher (get-in route-config [:routes token])]
        (dispatcher input)
        (.log js/console "Unknown route:" token)))))

(defn configure-router 
  ([app route-config]
    (doto (goog.History.)
      (goog.events/listen 
        (goog.object/getValues goog.history/EventType) 
        (route-changed app route-config))
      (.setEnabled true))))

; Specific renderers for this demo application, nothing interesting or generic here. 
(defn render-route [msg]
  (let [container (dom/by-id "view-container")]
    (dom/destroy-children! container)
    (dom/append! container (str "<p>" msg "</p>"))))

; These renderers only touch DOM, but they can do something completely different 
; as well. They could push a message to the input queue without doing any DOM
; manipulation, or do both.
(defn route-first [input-queue]
  (render-route "This is the first route"))

(defn route-second [input-queue]
  (render-route "This is the second route"))

; Some "real", working config.
(def router-config
  {:routes {"first" route-first
            "second" route-second}
   :default-route "first"
   :listener (fn [new-value]
               (.log js/console "Routing to" new-value))})

; The rendering is at least a two-phase process and it needs to be configured in
; two places - the goog.History listener to push messages to input queue, and 
; the renderer which updates DOM as those changes occur. 
(defn ^:export main []
  (let [app (app/build count-app)]
    (render/consume-app-model app render-fn)
    (configure-router app router-config)
    (app/begin app)))
