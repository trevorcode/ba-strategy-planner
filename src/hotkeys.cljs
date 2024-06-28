(ns hotkeys
  (:require [toolpicker :as toolpicker]))

(js/document.addEventListener
 "keydown"
 (fn [e]
   (case (.toLowerCase e.key)
     "q" (toolpicker/select-tool :pen)
     "e" (toolpicker/select-tool :eraser)
     "r" (toolpicker/select-tool :unitplacej)
     nil)))

