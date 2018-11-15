#!/bin/sh

echo "Travis + Gradle release"
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git config --global push.default current
git stash
git remote set-url origin https://andreifinski:$GH_TOKEN@github.com/andreifinski/agent-java-spock
git checkout master
git update-index --chmod=+x gradlew
chmod +x gradlew
./gradlew release bintrayUpload -Prelease.useAutomaticVersion=true --debug --stacktrace -Dbintray.user=$BINTRAY_USER -Dbintray.key=$BINTRAY_KEY -Dbuild.number=$TRAVIS_BUILD_NUMBER