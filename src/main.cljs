(ns main
  (:require
   ["alpinejs" :as a :refer [Alpine]]
   [util :as u]
   [map :as map]
   [colorpicker :as c]
   [unitpicker :as unit]
   [toolpicker :as tool]
   [hotkeys :as h]
   [bases :as bases]
   [components :as components]
   [toolpicker :as t]))

(set! js/window.Alpine Alpine)
(Alpine.start)

(def initial-template
  #html [:div [:div {:class "container"}
               [:nav
                [:ul
                 [:li (tool/tool-btn {:tool "hand"
                                      :tooltip "Move Tool [Q]"
                                      :body (components/icon "move")})]
                 [:li (tool/tool-btn {:tool "pen"
                                      :tooltip "Pen [W]"
                                      :body (components/icon "edit-2")})]
                 [:li (tool/tool-btn {:tool "eraser"
                                      :tooltip "Eraser [E]"
                                      :body (components/icon "x-square")})]
                 unit/unit-picker-btn]
                [:div c/colorpicker]
                [:div bases/base-expander-ui]]
               [:main
                [:div {:id "svg-container"}
                 [:svg {:id "map" :class "map-viewer" :xmlns "http://www.w3.org/2000/svg"}
                  [:g {:id "map-content"}
                   [:g {:id "background"}
                    [:image {:href "assets/battlemap.png"}]]
                   [:g {:id "map-images"}]
                   [:g {:id "map-paths"}]]]]]]
         unit/unit-picker-el])




(defn init []
  (-> (js/document.querySelector "body")
      .-innerHTML
      (set! initial-template))

  (map/reset-svg-refs {:container (js/document.querySelector "#svg-container")
                       :svg (js/document.querySelector "#map")
                       :g (js/document.querySelector "#map-content")
                       :images (js/document.querySelector "#map-images")
                       :paths (js/document.querySelector "#map-paths")})
  (map/register-map)
  (bases/bases-store.expand true)
  (bases/bases-store.expand false)
  (js/feather.replace))

(init)