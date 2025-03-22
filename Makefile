.PHONY: help install build package test clean

help:
	@cat $(firstword $(MAKEFILE_LIST))

install:
	mvn install

build: \
	package

package:
	mvn package

test:
	mvn test

clean:
	mvn clean