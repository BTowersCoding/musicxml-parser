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

(defn parts 
  "Outputs a zipper representing a sequence of parts from a musicxml file."
  [file]
  (-> file
      (xml/parse startparse-sax)
      zip/xml-zip
      (zip-xml/xml-> :score-partwise :part)))

(parts "resources/test.musicxml")

(defn measures 
  "Takes a zipper from a parts node and outputs that part's measures."
  [part-node]
  (zip-xml/xml-> part-node :measure))

(-> "resources/test.musicxml"
    parts
    first
    measures
    first
 )

(defn notes
  "Takes a zipper from a measure node and outputs that measure's notes."
  [measure-node]
  (zip-xml/xml-> measure-node :note))

(defn chord? 
  "Returns non-nil if a note is the member of a chord."
  [note]
   (zip-xml/xml1-> note :chord))

(defn pitch
  "Returns the note's pitch, or `nil` if it is a rest."
  [note]
  (zip-xml/text (zip-xml/xml1-> note :pitch)))

 (-> "resources/test2.musicxml"
     parts
     first
     measures
     first
     notes
     (nth 3)
     pitch
    
     )