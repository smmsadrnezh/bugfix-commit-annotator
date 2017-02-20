It find all commits which have some sort of strings such as "bugfix" or "fix" in their commit messages. After that it finds all affected lines of code in those commits and then it looks for all annotate commits (or blame-commit) for each of these lines. So finally we have all commits with bugs which are fixed later in some other commits.

How to run
=========

This plugin is based on [JGit by eclipse](https://eclipse.org/jgit). So for running the project you have to download and add these jar files to class path of the project:

  * slf4j-api.jar
  * slf4j-jdk14.jar
  * org.eclipse.jgit.jar
  * jdtcore-3.1.0.jar

If you are using IntelliJ IDE you can do this by going to:

`File → Project Structure → Project Settings → Modules → Dependencies → "+" sign → JARs or directories → Select the jar file and click on OK`

How to use
=========

After running the project, enter the path to .git directory in your repository:

`/path/to/repository/.git`

TODO
=========

  * Use Regex to Find bugfix commits. Add numbers to regex.
  * find all changed lines in buggy commits which is related to a specific bugfix commit. (Is it equal to line number of annotated line?)
    * DS: bugfixCommits[hashmap - no duplicate] = ( annotatedCommits[pair - duplicate] = (lineNumbers[list - no duplicate]) )
    * Is it necessary to have a DS?
  * find previous state of code in buggy commit.
  * Use Eclipse JDT to find AST Tree of the code in the previous state of buggy commit. (What if this code is not runnable?) (Whole project?)
  * Find all related nodes of that AST to specified line numbers.
  * Find the fullname of all used methods in those lines from AST Tree 