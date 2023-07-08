(import (java.util Locale))

; gets the operating system
(def os (.toLowerCase (System/getProperty "os.name") (Locale/ROOT)))
(def osArch (.toLowerCase (System/getProperty "os.arch") (Locale/ROOT)))
(def nativeArch (cond
                  (.contains os "windows") (if (.contains osArch "64") (if (.startsWith osArch "aarch64") "windows-arm64" "windows"))
                  (or (.contains os "mac os x") (.contains os "darwin") (.contains os "osx")) (if (.startsWith osArch "aarch64") "macos-arm64" "macos-x64")
                  (.contains os "linux") (if (or (.startsWith osArch "arm") (.startsWith osArch "aarch64"))
                                           (if (or (.contains osArch "64") (.startsWith osArch "armv8")) "linux-arm64" "linux-arm32")
                                           "linux-x64")
                  :else (throw (IllegalStateException. (format "Unsupported os %s or arch %s" os osArch)))))
(def natives (str "natives-" nativeArch))

(defproject fakeos "0.1.0-SNAPSHOT"
  :description "A fake OS written in Clojure"
  :url "https://github.com/squid233/fakeos"
  :license {:name "LGPL-3.0-only"
            :url  "https://www.gnu.org/licenses/lgpl-3.0.html"}
  :repositories [["snapshots" "https://s01.oss.sonatype.org/content/repositories/snapshots/"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [io.github.over-run/overrungl "0.1.0-SNAPSHOT"]
                 [io.github.over-run/overrungl-glfw "0.1.0-SNAPSHOT"]
                 [io.github.over-run/overrungl-joml "0.1.0-SNAPSHOT"]
                 [io.github.over-run/overrungl-opengl "0.1.0-SNAPSHOT"]
                 [io.github.over-run/overrungl-stb "0.1.0-SNAPSHOT"]
                 [io.github.over-run/overrungl-glfw "0.1.0-SNAPSHOT" :classifier ~natives] ; remember to use ~ to eval it
                 [io.github.over-run/overrungl-stb "0.1.0-SNAPSHOT" :classifier ~natives]
                 ]
  :repl-options {:init-ns fakeos.core})
