(ns unitpicker
  (:require
   ["alpinejs" :refer [Alpine]]
   [util :as u]
   [colorpicker :as c]
   [toolpicker :as t]
   [components :as components]))

(def units [:advancedblink
            :advancedbot
            :advancedrecall
            :airship
            :artillery
            :assaultbot
            :ballista
            :beetle
            :behemoth
            :blink
            :blinkhunter
            :bomber
            :bulwark
            :butterfly
            :crab
            :crusader
            :destroyer
            :dragonfly
            :falcon
            :gunbot
            :heavyballista
            :heavyhunter
            :heavyturret
            :hornet
            :hunter
            :katbus
            :kingcrab
            :kraken
            :locust
            :mammoth
            :missilebot
            :mortar
            :predator
            :raider
            :recall
            :recallhunter
            :recallshocker
            :scorpion
            :shocker
            :stinger
            :sniper
            :swiftshocker
            :turret
            :valkyrie
            :wasp])

(def unit-store (do (Alpine.store "units"
                                  {:toggled false
                                   :toggle (fn []
                                             (set! js/this.toggled (not js/this.toggled)))
                                   :selectedUnit (first units)
                                   :selectedUnitUrl (str "assets/units/" (first units) ".png")
                                   :select (fn [unit]
                                             (set! js/this.toggled false)
                                             (set! t/toolstore.selectedTool :unitplacer)
                                             (set! js/this.selectedUnit unit)
                                             (set! js/this.selectedUnitUrl (str "assets/units/" unit ".png")))})
                    (Alpine.store "units")))

(defn unit-image [unit]
  #html [:img {:src (str "assets/units/" unit ".png")}])

(def unit-picker-btn
  #html
   [:li
    (t/tool-btn {:tool "unitplacer"
                 :tooltip "Unit Placer [R]"
                 :class "unit-placer-btn"
                 :x-bind:onclick "$store.units.toggle()"
                 :body #html [:img {:x-data nil
                                    :x-bind:src "$store.units.selectedUnitUrl"}]})])

(def unit-picker-el
  #html
   [:div {:class "unit-picker-container"
          :x-data nil
          :x-show "$store.units.toggled"}
    [:div {:class "unit-picker"
           :x-on:click.outside "$store.units.toggle()"}
     [:ul
      (mapv (fn [u] #html [:li [:button {:unit u
                                         :class "tooltip top"
                                         :x-bind:class
                                         (str "{'selected': $store.units.selectedUnit == '" u "'}")
                                         :tooltip u
                                         :x-on:click (str "$store.units.select('" u "')")}
                                (unit-image u)]]) units)]]])

(defn place-unit [state svg-refs e primary?]
  (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)]
    (u/place-image-on-map {:x x
                           :y y
                           :svg-refs svg-refs
                           :imageUrl (str "assets/units/" unit-store.selectedUnit ".png")
                           :color (if primary?
                                    c/color-store.primaryColor
                                    c/color-store.secondaryColor)})))
