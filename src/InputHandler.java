import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Scanner;

public class InputHandler {


    public static void main(String[] args) throws IOException, GitAPIException {

        /** Read Repository Path */
        String path = new Scanner(System.in).nextLine();

        /** Initialize Repository */
        CommitFinder commitFinder = new CommitFinder(path);
        commitFinder.initializeRepository();

        /** Run Program */
        commitFinder.annotateCommits(commitFinder.findBugfixCommits());

    }

}
