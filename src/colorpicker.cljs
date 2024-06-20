(ns colorpicker
  (:require [squint.core :refer [defclass]]))


(def color-state (atom {:toggled? false
                        :primary-color :blue
                        :secondary-color :red
                        :choosing-color :primary}))

(def color-picker
  #html [:div {:class "color-picker"}
         [:ul
          [:li [:button {:color "red" :style (str "background-color:" "red")}]]
          [:li [:button {:color "orange" :style (str "background-color:" "orange")}]]
          [:li [:button {:color "yellow" :style (str "background-color:" "yellow")}]]
          [:li [:button {:color "green" :style (str "background-color:" "green")}]]
          [:li [:button {:color "blue" :style (str "background-color:" "blue")}]]
          [:li [:button {:color "purple" :style (str "background-color:" "purple")}]]
          [:li [:button {:color "black" :style (str "background-color:" "black")}]]
          [:li [:button {:color "white" :style (str "background-color:" "white")}]]]])

(defn select-color [color]
  (swap! color-state (fn [state]
                       (-> state
                           (assoc :toggled? false)
                           (assoc (str (:choosing-color state) "-color") color)))))

(defn initialize-color-picker []
  (doseq [btn (js/document.querySelectorAll "button[color]")]
    (btn.addEventListener
     "click"
     (fn [] (select-color (btn.getAttribute :color)))))

  (doseq [btn (js/document.querySelectorAll "button[color-selector]")]
    (btn.addEventListener
     "click"
     (fn []  (swap! color-state
                    (fn [s]
                      (-> s
                          (assoc :choosing-color (btn.getAttribute :color-selector))
                          (update :toggled? (fn [t] (not t)))))))))


  (reset! color-state @color-state))

(add-watch color-state :watch-color-state
           (fn [_ _ _ {:keys [toggled? primary-color secondary-color] :as new}]
             (println new)

             (-> (js/document.querySelector "button[color-selector=primary]")
                 .-style
                 .-backgroundColor
                 (set! primary-color))

             (-> (js/document.querySelector "button[color-selector=secondary]")
                 .-style
                 .-backgroundColor
                 (set! secondary-color))

             (if toggled?
               (do
                 (-> (js/document.querySelector ".color-picker")
                     (.-classList)
                     (.add "visible")))
               (do
                 (-> (js/document.querySelector ".color-picker")
                     (.-classList)
                     (.remove "visible"))))))


;; (defclass ColorPicker
;;   (extends HTMLElement)

;;   (constructor
;;    [this]
;;    (do
;;      (super)
;;      (let [shadowRoot (this.attachShadow {:mode "open"})
;;            template (js/document.createElement "template")]

;;        (set! template.innerHTML #html [:div "hello world!"])
;;        (shadowRoot.appendChild (if (= 1 (count template.content.children))
;;                                  (first template.content.children)
;;                                  template.content.children)))))
;;   )

;; (js/customElements.define "color-picker" ColorPicker)