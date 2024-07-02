(ns penpicker
  (:require [components :as c]))

(def pen-state (atom {:panel-toggled? false
                      :pen-type :solid}))

(def pen-picker-btn
  #html [:li
         [:button {:class "ba-button" :tool "pen"} [:i {:data-feather "edit-2"}]]
         (c/option-expander "pen-picker-expand")])
