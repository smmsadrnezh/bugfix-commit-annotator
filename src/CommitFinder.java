import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
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
    private ArrayList<String> bugTerms;
    private BlameCommand blamer = new BlameCommand(repository);
    private ArrayList<RevCommit> bugfixCommits = new ArrayList<>();
    private HashMap<RevCommit, ArrayList<RevCommit>> result = new HashMap<>();
    private List<DiffEntry> diffEntries;

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
            Iterable<RevCommit> commits = git.log().all().call();

            bugTerms = new ArrayList<String>() {{
                add("bugfix");
                add("fix");
                add("bug");
                add("issue");
            }};

            for (RevCommit commit : commits) {
                for (String bugTerm : bugTerms) {
                    if (commit.getShortMessage().toLowerCase().contains(bugTerm)) {
                        bugfixCommits.add(commit);
                    }
                }
            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void annotateRevisions() throws GitAPIException, IOException {

        for (RevCommit bugfixCommit : bugfixCommits) {

            ObjectReader reader = repository.newObjectReader();

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = bugfixCommit.getTree();
            oldTreeIter.reset(reader, oldTree);

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = bugfixCommit.getParent(0).getTree();
            newTreeIter.reset(reader, newTree);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            diffEntries = diffFormatter.scan(oldTreeIter, newTreeIter);

            for (DiffEntry diffEntry : diffEntries) {
                if (diffEntry.getChangeType().toString() == "MODIFY") {
                    String changedFilePath = diffEntry.getPath(DiffEntry.Side.NEW);
                    int lineNumber = 0;

                    blamer.setFilePath(changedFilePath);
                    blamer.setStartCommit(bugfixCommit.getId());
                    BlameResult blame = blamer.call();
                    RevCommit annotationCommit = blame.getSourceCommit(lineNumber);
                    ArrayList<RevCommit> annotateCommits = new ArrayList<>();
                    annotateCommits.add(annotationCommit);

                    result.put(bugfixCommit, annotateCommits);
                }
            }

        }
    }
}
