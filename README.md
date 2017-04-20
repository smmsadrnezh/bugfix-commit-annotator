It find all commits which their commit messages matches the specific regular expression with "bugfix" or "fix" or numbers. After that it finds all affected lines of code in those commits and then it looks for all annotate commits (or blame-commit) for each of these lines. So finally we have all commits with bugs which are fixed later in some other commits. After that we build AST from the buggy commit and find library methods and classes which caused bugs.

How to run
=========

This plugin is based on [JGit by eclipse](https://eclipse.org/jgit) and [Eclipse JDT](http://www.eclipse.org/jdt). So for running the project you have to download and add these jar files to class path of the project:

  * slf4j-api-1.7.21
  * slf4j-jdk14-1.7.21
  * org.eclipse.jgit-4.5.0.201609210915-r
  
  * [org.eclipse.core.resources-3.5.2](https://mvnrepository.com/artifact/org.eclipse/core-resources/3.5.2)
  * [org.eclipse.jdt.core-3.5.2](https://mvnrepository.com/artifact/org.eclipse/jdt-core/3.5.2)
  * [org.eclipse.core.runtime-3.5.0](https://mvnrepository.com/artifact/org.eclipse.core/org.eclipse.core.runtime/3.5.0.v20090525)
  * [org.eclipse.osgi-3.5.2](https://mvnrepository.com/artifact/org.eclipse.osgi/org.eclipse.osgi/3.5.2.R35x_v20100126)
  * [org.eclipse.core.contenttype-3.4.1](https://mvnrepository.com/artifact/org.eclipse.core/org.eclipse.core.contenttype/3.4.100)
  * [org.eclipse.core.jobs-3.4.100](https://mvnrepository.com/artifact/org.eclipse.core/org.eclipse.core.jobs/3.4.100.v20090429-1800)
  * [org.eclipse.equinox.common-3.5.0](https://mvnrepository.com/artifact/org.eclipse.equinox/org.eclipse.equinox.common/3.5.0.v20090520-1800)
  * [org.eclipse.equinox.preferences-3.3.0](https://mvnrepository.com/artifact/org.eclipse.equinox/org.eclipse.equinox.preferences/3.3.0.v20100503)
  

If you are using IntelliJ IDE you can do this by going to:

`File → Project Structure → Project Settings → Modules → Dependencies → "+" sign → JARs or directories → Select the jar file and click on OK`

How to use
=========

After running the project, enter the path to .git directory in your repository:

`/path/to/repository/.git`

`/home/smmsadrnezh/Desktop/lab/bilityab/.git`

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