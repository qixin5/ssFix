package search;

public class SearchResultItem
{
    String fpath;
    String loc;
    float score;

    public SearchResultItem(String fpath, String loc, float score) {
	this.fpath = fpath;
	this.loc = loc;
	this.score = score;
    }

    public String getFilePath() { return fpath; }

    public String getLoc() { return loc; }
    
    public void setScore(float score) {
	this.score = score;
    }

    public float getScore() {
	return score;
    }

    public String toString() {
	return "file://"+fpath+","+loc+","+score;
    }
}
