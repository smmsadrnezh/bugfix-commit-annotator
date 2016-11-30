It find all commits which have some sort of strings such as "bugfix" or "fix" in their commit messages. After that it finds all affected lines of code in those commits and then it looks for all annotate commits (or blame-commit) for each of these lines. So finally we have all commits with bugs which are fixed later in some other commits.

How to run
=========

This plugin is based on [JGit by eclipse](https://eclipse.org/jgit). So for running the project you have to download and add these jar files to class path of the project:

  * slf4j-api.jar
  * slf4j-jdk14.jar
  * org.eclipse.jgit.jar

If you are using IntelliJ IDE you can do this by going to:

`File → Project Structure → Project Settings → Modules → Dependencies → "+" sign → JARs or directories → Select the jar file and click on OK`

How to use
=========

After running the project, enter the path to .git directory in your repository:

`/path/to/repository/.git`