import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Scanner;

public class InputHandler {


    public static void main(String[] args) throws IOException, GitAPIException {

        /** Read Repository Path */
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        scanner.close();

        /** Initialize Repository */
        CommitFinder commitFinder = new CommitFinder(path);
        commitFinder.initializeRepository();

        /** Run Program */
        commitFinder.annotateCommits(commitFinder.findBugfixCommits());
    }

}
