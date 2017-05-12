package patchgen;

import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jdt.core.dom.ASTNode;

public class CandidateChunk extends Chunk
{
    boolean is_local;
    static Pattern local_path_ptn;
    static {
	local_path_ptn = Pattern.compile(".+/defects4j-bugs/.+/.+_buggy/.+\\.java|.+/search_fix/.+_local_fix.java");
    }
    
    public CandidateChunk(String fpath, String loc) {
	super(fpath, loc);
	is_local = isLocal0(fpath);
    }

    public CandidateChunk(String fpath, String loc, List<ASTNode> node_list) {
	super(fpath, loc, node_list);
	is_local = isLocal0(fpath);
    }

    public boolean isLocal() {
	return is_local;
    }
    
    private boolean isLocal0(String fpath) {
	if (fpath == null) { return false; }
	return local_path_ptn.matcher(fpath).matches();
    }
}
