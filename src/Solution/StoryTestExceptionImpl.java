package Solution;

import Provided.StoryTestException;

import java.util.LinkedList;
import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    private String _firstFail;
    private List<String> _storyExpceted, _testResults;
    private int _numFail;

    StoryTestExceptionImpl(String firstExpected, List<String> storyExpceted, List<String> testResults, int numFail) {
        _firstFail = firstExpected;
        _storyExpceted = storyExpceted;
        _testResults = testResults;
        _numFail = numFail;
    }

    @Override
    public String getSentance() {
        return _firstFail;
    }

    @Override
    public List<String> getStoryExpected(){ return _storyExpceted; }

    @Override
    public List<String> getTestResult(){ return _testResults; }

    @Override
    public int getNumFail(){ return _numFail; }
}
