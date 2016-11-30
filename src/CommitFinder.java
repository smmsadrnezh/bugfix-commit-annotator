import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
    private HashMap<RevCommit,ArrayList<RevCommit>> result = new HashMap<>();

    CommitFinder(String path){
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
    void findBugfixCommits(){
        try {
            Iterable<RevCommit> commits = git.log().all().call();

            bugTerms = new ArrayList<String>() {{
                add("bugfix");
                add("fix");
            }};

            for (RevCommit commit : commits) {
                for(String bugTerm : bugTerms)
                {
                    if(commit.getShortMessage().toLowerCase().contains(bugTerm))
                    {
                        annotateRevisions(commit);
                    }
                }
            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void annotateRevisions(RevCommit commit) throws GitAPIException {

        String changedFilePath = "README.md";
        int lineNumber = 0;

        blamer.setFilePath(changedFilePath);
        blamer.setStartCommit(commit.getId());
        BlameResult blame = blamer.call();
        RevCommit annotationCommit = blame.getSourceCommit(lineNumber);
        ArrayList<RevCommit> annotateBugyCommits = new ArrayList<>();
        annotateBugyCommits.add(annotationCommit);

        result.put(commit,annotateBugyCommits);
    }
}
