import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by smmsadrnezh on 11/30/16.
 */

public class CommitFinder {

    private Repository repository;
    private String path;
    private Git git;
    private RevWalk walk;
    private ArrayList<String> bugTerms = new ArrayList<String>() {{
        add("bugfix");
        add("fix");
        add("bug");
        add("issue");
        add("resolve");
    }};
    private BlameCommand blamer;
    private ArrayList<RevCommit> bugfixCommits = new ArrayList<>();
    private HashMap<RevCommit, ArrayList<RevCommit>> result = new HashMap<>();
    private List<DiffEntry> diffEntries;
    private Iterable<RevCommit> allCommits;

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
        walk = new RevWalk(repository);

    }

    void findBugfixCommits() {

        try {
            allCommits = git.log().all().call();

            for (RevCommit commit : allCommits) {
                if (isBugfixCommit(commit)) {
                    bugfixCommits.add(commit);
                }
            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean isBugfixCommit(RevCommit commit) {

        for (String bugTerm : bugTerms) {
            if (commit.getShortMessage().toLowerCase().contains(bugTerm)) {
                return true;
            }
        }

        return false;
    }

    void annotateCommits() throws GitAPIException, IOException {

        for (RevCommit bugfixCommit : bugfixCommits) {

            diffEntries = getDiffEntries(bugfixCommit);

            System.out.println("Commit: " + bugfixCommit.getShortMessage());

            for (DiffEntry diffEntry : diffEntries) {
                if (diffEntry.getChangeType().toString() == "MODIFY") {

                    ArrayList<RevCommit> annotateCommits = new ArrayList<>();
                    annotateCommits.add(annotateDiffEntry(diffEntry, bugfixCommit.getId()));

                    result.put(bugfixCommit, annotateCommits);

                }
            }

        }
    }

    private RevCommit annotateDiffEntry(DiffEntry diffEntry, ObjectId startCommitId) throws GitAPIException {
        String changedFilePath = diffEntry.getPath(DiffEntry.Side.NEW);
        int lineNumber = 0;
        blamer = new BlameCommand(repository);
        blamer.setFilePath(changedFilePath);
        blamer.setStartCommit(startCommitId);
        BlameResult blame = blamer.call();
        RevCommit annotationCommit = blame.getSourceCommit(lineNumber);
        System.out.println("Diff Annotate: " + annotationCommit.getShortMessage());
        return annotationCommit;
    }

    private List<DiffEntry> getDiffEntries(RevCommit bugfixCommit) throws IOException {

        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        diffFormatter.setDetectRenames(true);

        return diffFormatter.scan(bugfixCommit.getTree(), bugfixCommit.getParent(0).getTree());

    }
}
