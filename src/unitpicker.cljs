(ns unitpicker
  (:require [util :as u]
            [colorpicker :as c]))

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

(def unit-picker-state (atom {:toggled? true
                              :selected-unit (first units)}))

(def unit-picker-btn
  #html [:div
         [:span "Unit Picker"]
         [:ul
          [:li
           [:button {:class "unit-placer-btn ba-button" :tool :unitplacer}]
           [:button {:class "unit-picker-btn ba-button"} ">"]]]])

(defn unit-image [unit]
  #html [:img {:src (str "assets/units/" unit ".png")}])

(def unit-picker-el
  #html
   [:div {:class "unit-picker-container"}
    [:div {:class "unit-picker"}
     [:ul
      (mapv (fn [u] #html [:li [:button {:unit u} (unit-image u)]]) units)]]])

(defn select-unit [unit]
  (swap! unit-picker-state (fn [state]
                             (-> state
                                 (assoc :toggled? false)
                                 (assoc :selected-unit unit)))))

(defn toggle-picker []
  (swap! unit-picker-state (fn [state] (-> state
                                           (update :toggled? (fn [t] (not t)))))))

(defn place-unit [state svg-refs e primary?]
  (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
        unit-elem (js/document.createElementNS "http://www.w3.org/2000/svg" "image")
        unit (:selected-unit @unit-picker-state)
        color (if primary?
                (:primary-color @c/color-state)
                (:secondary-color @c/color-state))]
    (unit-elem.setAttribute :href (str "assets/units/" unit ".png"))
    (unit-elem.setAttribute :class "map-unit-image")
    (unit-elem.setAttribute :x (- x 25))
    (unit-elem.setAttribute :width 50)
    (unit-elem.setAttribute :height 50)
    (unit-elem.setAttribute :fill "gray")
    (unit-elem.setAttribute :y (- y 25))
    (.appendChild (:images svg-refs) unit-elem)))

(defn initialize []

  (-> (js/document.querySelector ".unit-picker-btn")
      (.addEventListener "click" (fn [] (toggle-picker))))

  (doseq [btn (js/document.querySelectorAll "button[unit]")]
    (btn.addEventListener
     "click"
     (fn [] (select-unit (btn.getAttribute :unit)))))

  (reset! unit-picker-state @unit-picker-state))

(add-watch unit-picker-state :watch-unit-state
           (fn [_ _ _ {:keys [toggled? selected-unit] :as new}]
             (-> (js/document.querySelector "button[tool=unitplacer]")
                 (.-innerHTML)
                 (set! (unit-image selected-unit)))

             (if toggled?
               (do
                 (-> (js/document.querySelector ".unit-picker-container")
                     (.-classList)
                     (.add "visible")))
               (do
                 (-> (js/document.querySelector ".unit-picker-container")
                     (.-classList)
                     (.remove "visible"))))))