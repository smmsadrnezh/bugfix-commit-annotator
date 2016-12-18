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

    void initializeRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder.setGitDir(new File(path))
                .readEnvironment().findGitDir().build();
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

            /** find commit Id to annotate from */
            ObjectId annotateFromCommitId = bugfixCommit.getId();

            /** checkout to one commit before annotateFromCommitId */
            git.checkout().setName(bugfixCommit.getParent(0).getName()).call();

            /** Find MODIFY edits */
            for (DiffEntry diffEntry : diffManager.getDiffEntries(bugfixCommit)) {
                if (diffEntry.getChangeType().toString() == "MODIFY") {

                    String changedFilePath = diffEntry.getPath(DiffEntry.Side.NEW);
                    BlameResult fileBlameResult = annotateFile(changedFilePath);

                    for (Edit edit : diffManager.getEdits(diffEntry)) {

                        /** find deleted line numbers */
                        ArrayList<Integer> deletedLines = new ArrayList();
                        if (edit.getEndA() < edit.getEndB()) {
                            for (int line = edit.getEndA(); line <= edit.getEndB(); line++) {
                                deletedLines.add(line);
                            }
                        } else {
                            deletedLines.add(edit.getEndB());
                        }

                        ArrayList<RevCommit> annotateCommits = new ArrayList<>();
                        for (Integer deletedLineNumber : deletedLines)
                            annotateCommits.add(annotateLine(annotateFromCommitId, fileBlameResult, deletedLineNumber));

                        result.put(bugfixCommit, annotateCommits);

                    }
                }
            }
        }
    }

    private BlameResult annotateFile(String changedFilePath) throws GitAPIException {
        return git.blame().setFilePath(changedFilePath).call();
    }

    private RevCommit annotateLine(ObjectId annotateFromCommitId, BlameResult fileBlameResult, int lineNumber) throws GitAPIException, IOException {

//        BlameCommand blamer;
//        blamer = new BlameCommand(repository);
//        blamer.setFilePath(changedFilePath);
//        blamer.setStartCommit(startCommitId);
//        BlameResult blame = blamer.call();
//        RevCommit annotationCommit = blame.getSourceCommit(lineNumber);

//        BlameGenerator blameGenerator;
//        blameGenerator = new BlameGenerator(repository,changedFilePath);
//        blameGenerator.setFollowFileRenames(true);
//        BlameResult aa = blameGenerator.computeBlameResult();
//        RevCommit annotationCommit = aa.getSourceCommit(lineNumber);

        System.out.println("Line Number: " + lineNumber);

        RevCommit annotationCommit = fileBlameResult.getSourceCommit(lineNumber);

        System.out.println("Annotate From Commit ID: " + annotateFromCommitId.toString().replaceAll("commit ", "").replaceAll("-", "").replaceAll(" sp", ""));
        System.out.println("Annotated Commit: " + annotationCommit.getShortMessage());
        System.out.println("Annotated ID: " + annotationCommit.getId().toString().replaceAll("commit ", "").replaceAll("-", "").replaceAll(" p", ""));
        System.out.println("");

        return annotationCommit;

    }
}
