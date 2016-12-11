import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.List;

/**
 * Created by smmsadrnezh on 12/11/16.
 */
public class DiffManager {

    private DiffFormatter diffFormatter;

    DiffManager(Repository repository) {
        /** Set diffFormatter */
        diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        diffFormatter.setDetectRenames(true);
    }

    public List<DiffEntry> getDiffEntries(RevCommit bugfixCommit) throws IOException {

        return diffFormatter.scan(bugfixCommit.getTree(), bugfixCommit.getParent(0).getTree());

    }

    public EditList getEdits(DiffEntry diffEntry) throws IOException {

        return diffFormatter.toFileHeader(diffEntry).toEditList();

    }
}
