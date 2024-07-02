(ns hotkeys
  (:require [toolpicker :as toolpicker]
            [unitpicker :as u]))

(js/document.addEventListener
 "keydown"
 (fn [e]
   (case (.toLowerCase e.key)
     "q" (toolpicker/select-tool :hand)
     "w" (toolpicker/select-tool :pen)
     "e" (toolpicker/select-tool :eraser)
     "r" (u/unit-store.toggle)
     nil)))

