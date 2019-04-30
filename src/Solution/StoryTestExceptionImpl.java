package Solution;

import Provided.StoryTestException;

import java.util.LinkedList;
import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    private String firstFail ="";
    private int numFail = 0;

    public String getSentance() {
        return firstFail;
    }

    public List<String> getStoryExpected(){
        //TODO : needs to be implemented

        return new LinkedList<>(); //TODO : STAM returned
    }

    public List<String> getTestResult(){
        //TODO : needs to be implemented

        return new LinkedList<>(); //TODO : STAM returned
    }

    public int getNumFail(){
        return numFail;
    }
}
