(ns musicxml-parser.core
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.java.io :as io]
            [clojure.data.zip.xml :as zip-xml])
  (:import (javax.xml.parsers SAXParser SAXParserFactory)))

;; MusicXML example set: https://www.musicxml.com/music-in-musicxml/example-set/

(defn startparse-sax
  "Don't validate the DTDs, they are usually messed up."
  [s ch]
  (let [factory (SAXParserFactory/newInstance)]
    (.setFeature factory "http://apache.org/xml/features/nonvalidating/load-external-dtd" false)
    (let [^SAXParser parser (.newSAXParser factory)]
      (.parse parser s ch))))

(defn meta->map
  [root]
  (into {}
        (for [m (zip-xml/xml-> root :head :meta)]
          [(keyword (zip-xml/attr m :type))
           (zip-xml/text m)])))

(defn segment->map
  [seg]
  {:bytes  (Long/valueOf (zip-xml/attr seg :bytes))
   :number (Integer/valueOf (zip-xml/attr seg :number))
   :id     (zip-xml/xml1-> seg zip-xml/text)})

(defn file->map
  [file]
  {:poster   (zip-xml/attr file :poster)
   :date     (Long/valueOf (zip-xml/attr file :date))
   :subject  (zip-xml/attr file :subject)
   :groups   (vec (zip-xml/xml-> file :groups :group zip-xml/text))
   :segments (mapv segment->map
                   (zip-xml/xml-> file :segments :segment))})

(defn nzb->map [input]
  (let [root (-> input
                 io/input-stream
                 (xml/parse startparse-sax)
                 zip/xml-zip)]
    {:meta  (meta->map root)
     :files (mapv file->map (zip-xml/xml-> root :file))}))

(-> "resources/example.nzb"
    io/input-stream
    (xml/parse startparse-sax)
    zip/xml-zip)

(io/input-stream "resources/example.nzb")

(nzb->map "resources/Piano-Sonata-n01.musicxml")

(nzb->map "resources/example.nzb")
