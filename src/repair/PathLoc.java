package repair;

public class PathLoc
{
    String fpath;
    String loc;
    
    public PathLoc(String fpath, String loc) {
	this.fpath = fpath;
	this.loc = loc;
    }
    
    public String getPath() { return fpath; }
    
    public String getLoc() { return loc; }
}
