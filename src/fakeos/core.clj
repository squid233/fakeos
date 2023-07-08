;
; fakeos - A fake OS written in Clojure
; Copyright (C) 2023  squid233
;
; This program is free software: you can redistribute it and/or modify
; it under the terms of the GNU Lesser General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU Lesser General Public License for more details.
;
; You should have received a copy of the GNU Lesser General Public License
; along with this program.  If not, see <https://www.gnu.org/licenses/>.
;

(ns fakeos.core
  (:import (java.lang.foreign MemorySegment)
           (overrungl RuntimeHelper)
           (overrungl.glfw Callbacks GLFW GLFWErrorCallback IGLFWFramebufferSizeFun)
           (overrungl.opengl GL10C GLLoadFunc$Getter GLLoader)
           (overrungl.util MemoryStack)))

(defn -main []
  (.set (GLFWErrorCallback/createPrint))
  (RuntimeHelper/check (GLFW/init) "Failed to initialize GLFW")

  (try
    (GLFW/windowHint GLFW/VISIBLE false)
    (GLFW/windowHint GLFW/RESIZABLE false)
    (let [^MemorySegment window (with-open [stack (MemoryStack/stackPush)]
                                  (GLFW/createWindow stack 640 480 "fakeos" MemorySegment/NULL MemorySegment/NULL))]
      (RuntimeHelper/check (not (RuntimeHelper/isNullptr window)) "Failed to create the window")
      (try
        (GLFW/setFramebufferSizeCallback window (reify IGLFWFramebufferSizeFun (invoke [_ _ width height]
                                                                                 (GL10C/viewport 0 0 width height))))
        (if-some [vidMode (GLFW/getVideoMode (GLFW/getPrimaryMonitor))]
          (let [size (GLFW/getWindowSize window)]
            (GLFW/setWindowPos window (/ (- (.width vidMode) (.x size)) 2) (/ (- (.height vidMode) (.y size)) 2))))

        (GLFW/makeContextCurrent window)
        (GLFW/swapInterval 1)

        (GLFW/showWindow window)

        (RuntimeHelper/check (some? (GLLoader/loadConfined
                                      (reify GLLoadFunc$Getter (get [_ string] (GLFW/ngetProcAddress string)))
                                      )) "Failed to load OpenGL")

        (GL10C/clearColor 0.0 0.0 0.0 1.0)

        (while (not (GLFW/windowShouldClose window))
          (GL10C/clear (bit-or GL10C/COLOR_BUFFER_BIT GL10C/DEPTH_BUFFER_BIT))
          (GLFW/swapBuffers window)
          (GLFW/pollEvents))

        (finally (Callbacks/free window) (GLFW/destroyWindow window))))

    (finally (GLFW/terminate) (GLFW/setErrorCallback nil))))
