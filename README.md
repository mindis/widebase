Widebase
========

The the essential data model and its i/o operations.

<b>It's still in Beta phase and not ready for productive use.</b>

To learn more about Widebase and its architecture, <a href="http://widebase.github.com/widebase/latest/handbook/html/index.html">read the handbook</a>.

# Build from source

To build this code, get and install Vario from https://github.com/vario/vario.

Use these commands to build:

    > git clone git@github.com:widebase/widebase.git
    > cd widebase
    > sbt publish-local

And specify as a dependency in your project file:

```scala
libraryDependencies += "widebase" %% "widebase-db" % "0.1.0-SNAPSHOT"
```

# Demos/Testing

Some demo/test codes are in `widebase.db/src/test/scala/widebase/db/test`. Example how to run `Record.scala` test:

    > sbt
    > project widebase-db
    > test:run-main widebase.db.test.Record

# Generating ScalaDoc

Use this command to generating docs:

    > sbt "project widebase" "unidoc"

Open `./target/scala-2.9.1/unidoc/index.html` with any web browser.
