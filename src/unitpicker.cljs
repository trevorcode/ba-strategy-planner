(ns unitpicker)

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
                              :selected-unit :turret}))

(def unit-picker-btn
  #html [:div
         [:span "Unit Picker"]
         [:ul
          [:li [:button {:color-selector :primary}]]]])

(defn unit-image [unit]
  #html [:img {:src (str "assets/units/" unit)}])

(def unit-picker-el
  #html
   [:div {:class "unit-picker-container"}
    [:div {:class "unit-picker"}
     [:ul
      (mapv unit-image units)]]])
