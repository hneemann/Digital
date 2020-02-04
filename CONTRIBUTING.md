# Contributing to Digital

First off all, thanks for taking the time to contribute! :smile::+1:

## Before creating a pull request

Before you create a pull request, you should at least run `mvn verify` on your machine.

If `mvn verify` does not run without errors, I cannot merge the pull request, because that would break 
the code immediately on the next CI run.

The tests that are run will not only test the java source code, but also the `*.dig` files that are 
included in the project, so `mvn verify` should be run even if only a `*.dig` file is changed or added. 

To run the tests with `mvn verify`, [maven](https://maven.apache.org/) and a jdk must be installed.

If you want to contribute a `*.dig` file, but you are not familiar with maven or just don't want 
to install all this stuff, and therefore can't 
run `mvn verify`, it is easier for me if you don't create a pull request, but instead pack the 
`*.dig` files, create an [issue](https://github.com/hneemann/Digital/issues/new), and 
attach the zip file to this issue.
