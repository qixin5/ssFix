package patchgen;

public class Patch
{
    String fpath;
    boolean is_correct;
    int tested_num;

    public Patch(String fpath, boolean is_correct) {
	this.fpath = fpath;
	this.is_correct = is_correct;
	this.tested_num = 0;
    }

    public Patch(String fpath, boolean is_correct, int tested_num) {
	this.fpath = fpath;
	this.is_correct = is_correct;
	this.tested_num = tested_num;
    }
    
    public String getFilePath() { return fpath; }

    public boolean isCorrect() { return is_correct; }

    public void setTestedNum(int tn) { tested_num = tn; }

    public int getTestedNum() { return tested_num; }
}
