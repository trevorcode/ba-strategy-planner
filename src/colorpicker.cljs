(ns colorpicker
    (:require [util :as u]))

  (def colors ["#88eaff"
               "#5f8ad3"
               "#eee1ae"
               "#ffa788"
               "yellow"
               "red"
               "green"
               "purple"
               "black"
               "white"])

  (def color-store (u/init-store
                    "colors"
                    {:toggled false
                     :toggle (fn [primary]
                               (set! color-store.choosingColor primary)
                               (set! color-store.toggled (not color-store.toggled)))
                     :primaryColor (first colors)
                     :secondaryColor (second colors)
                     :choosingColor :primary
                     :selectColor (fn [color]
                                    (set! color-store.toggled false)
                                    (if (= color-store.choosingColor :primary)
                                      (set! color-store.primaryColor color)
                                      (set! color-store.secondaryColor color)))}))

(defn color-picker-btn [color]
  #html [:button {:color color
                  :x-on:click (str "$store.colors.selectColor('" color "')")
                  :style (str "background-color:" color)}])


(def colorpicker
  #html
   [:ul
    [:li [:button {:class "ba-button"
                   :color-selector :primary
                   :x-bind:style "{ backgroundColor: $store.colors.primaryColor}"
                   :x-on:click "$store.colors.toggle('primary')"}]]
    [:li [:button {:class "ba-button"
                   :color-selector :secondary
                   :x-bind:style "{ backgroundColor: $store.colors.secondaryColor}"
                   :x-on:click "$store.colors.toggle('secondary')"}]]
    [:div {:class "color-picker"
           :x-show "$store.colors.toggled"
           :x-on:click.outside "$store.colors.toggle()"}
     [:ul
      (mapv (fn [color] #html [:li (color-picker-btn color)]) colors)]]])
