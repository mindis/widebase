resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0")

