(ns unitpicker
  (:require
   ["alpinejs" :refer [Alpine]]
   [util :as u]
   [colorpicker :as c]
   [toolpicker :as t]
   [components :as components]))

(def units [:advancedrecall
            :airship
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
                 :body #html [:img {:x-data nil
                                    :x-bind:src "$store.units.selectedUnitUrl"}]})
    (components/option-expander {:id "unit-picker-btn"
                                 :x-on:click "$store.units.toggle()"})])

(def unit-picker-el
  #html
   [:div {:class "unit-picker-container"
          :x-data nil
          :x-show "$store.units.toggled"}
    [:div {:class "unit-picker"}
     [:ul
      (mapv (fn [u] #html [:li [:button {:unit u
                                         :class "tooltip top"
                                         :tooltip u
                                         :x-on:click (str "$store.units.select('" u "')")}
                                (unit-image u)]]) units)]]])


(defn place-unit [state svg-refs e primary?]
  (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
        group (js/document.createElementNS "http://www.w3.org/2000/svg" "g")
        unit-container (js/document.createElementNS "http://www.w3.org/2000/svg" "rect")
        unit-elem (js/document.createElementNS "http://www.w3.org/2000/svg" "image")
        unit unit-store.selectedUnit
        color (if primary?
                c/color-store.primaryColor
                c/color-store.secondaryColor)]
    (unit-container.setAttribute :stroke color)
    (unit-container.setAttribute :stroke-width 4)
    (unit-container.setAttribute :fill "none")
    (unit-container.setAttribute :x (- x 25))
    (unit-container.setAttribute :width 50)
    (unit-container.setAttribute :height 50)
    (unit-container.setAttribute :y (- y 25))

    (unit-elem.setAttribute :href (str "assets/units/" unit ".png"))
    (unit-elem.setAttribute :x (- x 25))
    (unit-elem.setAttribute :width 50)
    (unit-elem.setAttribute :height 50)
    (unit-elem.setAttribute :y (- y 25))

    (.appendChild group unit-container)
    (.appendChild group unit-elem)
    (.appendChild (:images svg-refs) group)))
