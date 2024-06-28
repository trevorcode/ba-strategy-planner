(ns toolpicker)


(def selected-tool (atom :pen))

(defn select-tool [tool]
  (reset! selected-tool tool))

(add-watch selected-tool :watch-tool
           (fn [key atom old new]
             (let [btns (js/document.querySelectorAll "nav button")]
               (doseq [btn btns]
                 (if (= new (btn.getAttribute :tool))
                   (btn.classList.add "selected")
                   (btn.classList.remove "selected"))))))

(defn init []
  (doseq [btn (js/document.querySelectorAll "nav button[tool]")]
    (btn.addEventListener
     "click"
     (fn [] (select-tool (btn.getAttribute :tool)))))

  (reset! selected-tool @selected-tool))