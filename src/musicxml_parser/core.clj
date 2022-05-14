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

(defn measures 
  "Takes a zipper from a parts node and outputs that part's measures."
  [part-node]
  (zip-xml/xml-> part-node :measure))

(defn notes
  "Takes a zipper from a measure node and outputs that measure's notes."
  [measure-node]
  (zip-xml/xml-> measure-node :note))

(defn chord? 
  "Returns non-nil if a note is tagged `chord`,
   meaning it does not advance the beat.
   A `chord` must be accompanied by exactly one note
   that plays at the same time that is _not_ a chord."
  [note]
   (zip-xml/xml1-> note :chord))

(defn pitch
  "Returns the note's pitch, or `nil` if it is a rest."
  [note]
  (when (zip-xml/xml1-> note :pitch)
    (zip-xml/text (zip-xml/xml1-> note :pitch))))

(defn beats 
  "Takes a sequence of notes and"
  [notes]
  (loop [result [] coll notes]
    (if (empty? coll)
      result
      (recur (conj result (into [(pitch (first coll))]
                                (map pitch (take-while #(chord? %) (rest coll)))))
             (drop-while #(chord? %) (rest coll))))))

(comment
  (->> "resources/test2.musicxml"
       parts
       first
       measures
       first
       notes
       beats)
  )