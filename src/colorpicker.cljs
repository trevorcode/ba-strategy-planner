(ns colorpicker
  (:require [squint.core :refer [defclass]]))

(def colors ["#88eaff"
             "#5f8ad3"
             "#eee1ae"
             "#ffa788"
             "yellow"
             "green"
             "blue"
             "purple"
             "black"
             "white"])

(def color-state (atom {:toggled? false
                        :primary-color (first colors)
                        :secondary-color (second colors)
                        :choosing-color :primary}))

(defn color-picker-btn [color]
  #html [:button {:color color :style (str "background-color:" color)}])

(def color-picker
  #html [:div {:class "color-picker"}
         [:ul
          (mapv (fn [color] #html [:li (color-picker-btn color)]) colors)]])

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