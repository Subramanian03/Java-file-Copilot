#!/usr/bin/env sh
# Lightweight mvnw that delegates to installed Maven.
# If you need a full Maven Wrapper, run `mvn -N io.takari:maven:wrapper` locally to generate the real wrapper.

WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
  java -jar "$WRAPPER_JAR" "$@"
  exit $?
fi

if command -v mvn >/dev/null 2>&1; then
  mvn "$@"
else
  echo "Maven (mvn) not found on PATH and no wrapper jar present. Install Maven or generate the Maven Wrapper with:"
  echo "  mvn -N io.takari:maven:wrapper"
  exit 1
fi
