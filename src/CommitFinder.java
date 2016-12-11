import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by smmsadrnezh on 11/30/16.
 */

public class CommitFinder {

    private Repository repository;
    private String path;
    private Git git;
    private ArrayList<String> bugTerms = new ArrayList<String>() {{
        add("bugfix");
        add("fix");
        add("bug");
        add("issue");
        add("resolve");
    }};
    private HashMap<RevCommit, ArrayList<RevCommit>> result = new HashMap<>();

    CommitFinder(String path) {
        this.path = path;
    }

    void initializeRepository() {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(path))
                    .readEnvironment().findGitDir().build();
        } catch (Exception e) {
            System.out.println("git address is not valid");
        }

        git = new Git(repository);

    }

    ArrayList<RevCommit> findBugfixCommits() throws IOException, GitAPIException {

        Iterable<RevCommit> allCommits;

        allCommits = git.log().all().call();

        ArrayList<RevCommit> bugfixCommits = new ArrayList<>();
        for (RevCommit commit : allCommits) {
            if (isBugfixCommit(commit)) {
                bugfixCommits.add(commit);
            }
        }

        return bugfixCommits;

    }

    private Boolean isBugfixCommit(RevCommit commit) {

        for (String bugTerm : bugTerms) {
            if (commit.getShortMessage().toLowerCase().contains(bugTerm)) {
                return true;
            }
        }

        return false;
    }

    void annotateCommits(ArrayList<RevCommit> bugfixCommits) throws GitAPIException, IOException {

        for (RevCommit bugfixCommit : bugfixCommits) {

            DiffManager diffManager = new DiffManager(repository);

            /** Find MODIFY edits */
            for (DiffEntry diffEntry : diffManager.getDiffEntries(bugfixCommit)) {
                if (diffEntry.getChangeType().toString() == "MODIFY") {
                    for (Edit edit : diffManager.getEdits(diffEntry)) {

                        /** find changed line numbers */
                        int endA = edit.getEndA();
                        int beginA = edit.getBeginA();
                        int endB = edit.getEndB();
                        int beginB = edit.getBeginB();

                        String changedFilePath = diffEntry.getPath(DiffEntry.Side.NEW);

                        /** find commit Id to annotate from */
                        ObjectId commitId = bugfixCommit.getId();

                        ArrayList<RevCommit> annotateCommits = new ArrayList<>();
                        annotateCommits.add(annotateEdit(commitId, changedFilePath, endA));

                        result.put(bugfixCommit, annotateCommits);

                    }
                }
            }
        }
    }

    private RevCommit annotateEdit(ObjectId startCommitId, String changedFilePath, int lineNumber) throws GitAPIException {

        BlameCommand blamer;
        blamer = new BlameCommand(repository);
        blamer.setFilePath(changedFilePath);
        blamer.setStartCommit(startCommitId);
        BlameResult blame = blamer.call();
        RevCommit annotationCommit = blame.getSourceCommit(lineNumber);
        System.out.println("Diff Annotate: " + annotationCommit.getShortMessage());
        return annotationCommit;

    }
}
