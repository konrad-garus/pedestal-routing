(ns config
  (:require [net.cgrand.enlive-html :as html]
            [io.pedestal.app-tools.compile :as compile]))

(def configs
  {:routing-app
   {:build {:watch-files (compile/html-files-in "templates")
            ;; When an HTML file changes, trigger the compilation of
            ;; any files which use macros to read in templates. 
            :triggers {:html [#"routing_app/app.js"]}}
    :application {:generated-javascript "generated-js"
                  :api-server {:host "localhost" :port 8080 :log-fn nil}
                  :default-template "application.html"
                  :output-root :public}
    :control-panel {:design {:uri "/design.html"
                             :name "Design"
                             :order 0}}
    :aspects {:development {:uri "/routing-app-dev.html"
                            :name "Development"
                            :out-file "routing-app-dev.js"
                            :scripts ["goog.require('routing_app.app');"
                                      "routing_app.app.main();"]
                            :order 1}
              :production {:uri "/routing-app.html"
                           :name "Production"
                           :optimizations :advanced
                           :out-file "routing-app.js"
                           :scripts ["routing_app.app.main();"]
                           :order 2}}}})
