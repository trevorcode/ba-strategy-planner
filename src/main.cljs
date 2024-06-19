(ns main
  (:require))



(def initial-template
  #html [:div {:class "container"}
         [:nav [:ul
                [:li [:button "World"]]
                [:li [:button "World"]]]]
         [:main "Hello"]])

(defn init []
  (-> (js/document.querySelector "body")
      .-innerHTML
      (set! initial-template)))

(init)


