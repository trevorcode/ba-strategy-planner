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

(def unit-picker-state (atom {:toggled? false
                              :selected-unit (first units)}))

(def unit-picker-btn
  #html [:div
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
        group (js/document.createElementNS "http://www.w3.org/2000/svg" "g")
        unit-container (js/document.createElementNS "http://www.w3.org/2000/svg" "rect")
        unit-elem (js/document.createElementNS "http://www.w3.org/2000/svg" "image")
        unit (:selected-unit @unit-picker-state)
        color (if primary?
                (:primary-color @c/color-state)
                (:secondary-color @c/color-state))]
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
               (-> (js/document.querySelector ".unit-picker-container")
                   (.-classList)
                   (.add "visible"))
               (-> (js/document.querySelector ".unit-picker-container")
                   (.-classList)
                   (.remove "visible")))))