# musicxml-parser

A Clojure library designed to extract music data and spit out a sequence of maps, each representing a note and its attributes:

```
{:instrument "bass"
 :time 2.25
 :pitch 60
 :duration 0.25}
```

`:time` (start time, "note-on") and `:duration` are measured in "beats", as defined by the piece (typically a quarter-note). `:pitch` is expressed in standard MIDI notation (60 is middle C).