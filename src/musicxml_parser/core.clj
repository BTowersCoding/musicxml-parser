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

(-> "resources/test.musicxml"
    parts
    first
    measures
    first
    notes
    first
    zip/node)

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
    (xml/parse startparse-sax)
    zip/xml-zip
    zip/down
    zip/node)

(nzb->map "resources/Piano-Sonata-n01.musicxml")

(nzb->map "resources/example.nzb")
