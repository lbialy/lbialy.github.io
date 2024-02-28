## The Good, The Magical and The Insane: Scala 3 API design patterns 

This directory contains the snippets of Scala 3 code that showcase new API (as in - for library authors!) design patterns. 
It is a companion to my talk at ScalaWAW Warsaw User Group `given` on 27th of February 2024. Slides are available [here](./slides.pdf).
The talk itself can be seen on [Youtube](https://youtu.be/JRgq8jjE2Dk?t=3105).

Snippets below can be executed with scala-cli separately - each contains a main method:

### Good:
* [refined types at home](./refined-types-at-home.scala)
* [Postel's law revisited](./postels-law-revisited.scala)
* [context proxies](./context-proxies.scala)
* [opaque api wrappers](./opaque-api-wrappers.scala)

### Magical:
* [macro-based interpolators](./macro-based-interpolators.scala) and their [usage](test-json-interpolation.scala) (macros have to be in separate files)
* [type providers - GraphQL - a separate repository](https://github.com/kordyjan/pytanie)
* [lifted syntax operators](./lifted-syntax) (`lifted-syntax-operators.scala` is the main)

### Insane: (don't do this)
* [fever syntax - yaml at home](./yaml-at-home.scala)
* [fever syntax - everything old is new again](./oh-no-not-the-new.scala)
