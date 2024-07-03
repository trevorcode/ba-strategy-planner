(ns bases
  (:require [util :as u]
            [colorpicker :as c]
            [map :as map]
            ["alpinejs" :refer [Alpine]]))

(def bases-locations
  {:ally [[139 895]
          [80 665]
          [350 582]
          [263 84]
          [690 115]]
   :enemy [[1129 140]
           [1180 365]
           [925 462]
           [980 920]
           [580 900]]})

(defn get-existing-bases [ally?]
  (sort (mapv (comp #(js/parseInt %) #(.getAttribute % "base"))
              (js/document.querySelectorAll (str "[base][ally=" ally? "]")))))

(defn non-incrementing [base-numbers]
  (->> (partition 2 1 base-numbers)
       (filter #(not= 1 (- (second %) (first %))))
       vec))

(defn next-base-num [existing-bases total-bases]
  (let [non-incrementing (non-incrementing existing-bases)
        continuous? (empty? non-incrementing)]
    (if continuous?
      (if (= (first existing-bases) 1)
        (when (< (last existing-bases) (count total-bases))
          (inc (last existing-bases)))
        1)
      (inc (apply min (first non-incrementing))))))

(defn expand [ally?]
  (let [existing-bases (get-existing-bases ally?)
        _ (println existing-bases)
        total-bases (if ally?
                      (:ally bases-locations)
                      (:enemy bases-locations))
        next-base-number (next-base-num existing-bases total-bases)
        next-base (nth total-bases (dec next-base-number))
        [x y] next-base]
    (when next-base
      (u/place-image-on-map
       {:base-number next-base-number
        :ally ally?
        :x x
        :y y
        :svg-refs map/svg-refs
        :imageUrl "assets/techtiers/core.svg"
        :color (if ally?
                 c/color-store.primaryColor
                 c/color-store.secondaryColor)}))))

(def bases-store (do (Alpine.store "bases"
                                   {:expand expand})
                     (Alpine.store "bases")))

(def base-expander-ui
  #html
   [:ul
    [:li [:button {:class "ba-button tooltip right"
                   :tooltip "Expand Team 1"
                   :x-bind:style "{ border: '4px solid ' + $store.colors.primaryColor}"
                   :x-on:click "$store.bases.expand(true)"}
          [:img {:src "assets/techtiers/core.svg"
                 :class :image-icon}]]]
    [:li [:button {:class "ba-button tooltip right"
                   :tooltip "Expand Team 2"
                   :x-bind:style "{ border: '4px solid ' + $store.colors.secondaryColor}"
                   :x-on:click "$store.bases.expand(false)"}
          [:img {:src "assets/techtiers/core.svg"
                 :class :image-icon}]]]])

