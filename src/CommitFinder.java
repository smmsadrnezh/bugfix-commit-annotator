import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.*;
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
    private HashMap<ObjectId, HashMap> result = new HashMap<>();

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

            checkoutOneCommitBeforeBugfix(bugfixCommit);

            /** Find MODIFY edits */
            for (DiffEntry diffEntry : diffManager.getDiffEntries(bugfixCommit)) {
                if (diffEntry.getChangeType().toString() == "MODIFY") {

                    String changedFilePath = diffEntry.getPath(DiffEntry.Side.NEW);
                    BlameResult fileBlameResult = annotateFile(changedFilePath);

                    for (Edit edit : diffManager.getEdits(diffEntry)) {

                        ArrayList<Integer> bugfixCommitDeletedLineNumbers = getBugfixCommitDeletedLineNumbers(edit);

                        String beforeBuggyCommitCode;
                        for (Integer bugfixCommitDeletedLineNumber : bugfixCommitDeletedLineNumbers) {
                            RevCommit buggyCommit = annotateLine(fileBlameResult, bugfixCommitDeletedLineNumber);
                            beforeBuggyCommitCode = getCode(buggyCommit,changedFilePath);
                            System.out.println(beforeBuggyCommitCode);
                            AbstractSyntaxTreeCrawler astParser = new AbstractSyntaxTreeCrawler();
                            astParser.buildAST(beforeBuggyCommitCode, bugfixCommitDeletedLineNumber);
                        }

//                        buildResult(bugfixCommitDeletedLineNumbers, fileBlameResult, bugfixCommit);

                    }
                }
            }
        }
    }

    private ArrayList<Integer> getBugfixCommitDeletedLineNumbers(Edit edit) {
        ArrayList<Integer> deletedLines = new ArrayList();
        if (edit.getEndA() < edit.getEndB()) {
            for (int line = edit.getEndA(); line <= edit.getEndB(); line++) {
                deletedLines.add(line);
            }
        } else {
            deletedLines.add(edit.getEndB());
        }
        return deletedLines;
    }

    private void checkoutOneCommitBeforeBugfix(RevCommit bugfixCommit) throws GitAPIException {
        git.checkout().setName(bugfixCommit.getParent(0).getName()).call();
    }

    private String getCode(RevCommit buggyCommit, String filePath) throws IOException {
        RevTree tree = buggyCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(objectId);


        OutputStream output = new OutputStream()
        {
            private StringBuilder string = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b );
            }

            //Netbeans IDE automatically overrides this toString()
            public String toString(){
                return this.string.toString();
            }
        };

        loader.copyTo(output);

        return output.toString();

    }

    private void buildResult(ArrayList<Integer> bugfixCommitDeletedLineNumbers, BlameResult fileBlameResult, RevCommit bugfixCommit) throws GitAPIException, IOException {

        /** find annotate commits for each line */
        HashMap<RevCommit, ArrayList<Integer>> annotateCommits = new HashMap<>();
        RevCommit buggyCommit;
        for (Integer bugfixCommitDeletedLineNumber : bugfixCommitDeletedLineNumbers) {
            buggyCommit = annotateLine(fileBlameResult, bugfixCommitDeletedLineNumber);
            if (annotateCommits.get(buggyCommit) == null) {
                ArrayList<Integer> deletedLineNumbers = new ArrayList();
                deletedLineNumbers.add(bugfixCommitDeletedLineNumber);
                annotateCommits.put(buggyCommit, deletedLineNumbers);
            } else {
                annotateCommits.get(buggyCommit).add(bugfixCommitDeletedLineNumber);
            }
        }
        result.put(bugfixCommit.getId(), annotateCommits);
    }

    private BlameResult annotateFile(String changedFilePath) throws GitAPIException {
        return git.blame().setFilePath(changedFilePath).call();
    }

    private RevCommit annotateLine(BlameResult fileBlameResult, int lineNumber) throws GitAPIException, IOException {
        return fileBlameResult.getSourceCommit(lineNumber-1);
    }
}
