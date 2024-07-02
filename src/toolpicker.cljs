(ns toolpicker
  (:require ["alpinejs" :as a :refer [Alpine]]))

(def toolstore (do (Alpine.store "tools"
                                 {:selectedTool :pen
                                  :select (fn [tool]
                                            (when (and (= tool :pen) (= toolstore.selectedTool :pen))
                                              (toolstore.togglePenStyle))
                                            (set! toolstore.selectedTool tool))
                                  :penStyle :solid
                                  :togglePenStyle (fn []
                                                    (set! toolstore.penStyle (if (= toolstore.penStyle :solid)
                                                                               :dotted
                                                                               :solid)))})
                   (Alpine.store "tools")))

(defn select-tool [tool]
  (toolstore.select tool))

(defn tool-btn [{:keys [tool class body tooltip x-bind:onclick]}]
  #html [:button {:tool tool
                  :tooltip tooltip
                  :class (str "ba-button " (when tooltip "tooltip right ") class)
                  :x-data nil
                  :x-bind:class (str "$store.tools.selectedTool=='" tool "' ? 'selected' : ''")
                  :x-on:click (or x-bind:onclick (str "$store.tools.select('" tool "')"))} 
         body])