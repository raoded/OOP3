package Tests;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import Provided.StoryTestException;
import Provided.StoryTester;
import Solution.StoryTesterImpl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * This is a minimal test. Write your own tests!
 * Please don't submit your tests!
 */
public class ClassroomTest {

    private StoryTester tester;
    private String goodStory;
    private String badStory;
    private String badStory2;
    private String derivedStory;
    private String nestedStory;
    private Class<?> testClass;
    private Class<?> derivedTestClass;
    private Class<?> nestedNestedTestClass;
    private String nestedStory1;
    private String nestedStory2;
    private String nestedStory3;
    private String nestedStory4;


    @Before
    public void setUp() throws Exception {
        goodStory = "Given a classroom with a capacity of 75\n"
                + "When the number of students in the classroom is 60\n"
                + "Then the classroom is not-full";

        badStory = "Given a classroom with a capacity of 50\n"
                + "When the number of students in the classroom is 40\n"
                + "When the number of broken chairs in the classroom is 10\n"
                + "Then the classroom is full\n"
                + "When the number of students in the classroom is 40\n"
                + "Then the classroom is not-full";

        badStory2 = "Given a classroom with a capacity of 50\n"
                + "When the number of students in the classroom is 40\n"
                + "When the number of broken chairs in the classroom is 10\n"
                + "Then the classroom is not-full\n"
                + "When the number of students in the classroom is 40\n"
                + "Then the classroom is not-full";

        nestedStory1 = "Given a classroom that the number of seats in it is 33\n"
                + "When the number of students in the classroom is 42 and the number among them that are standing is 6\n"
                + "Then the classroom is not-full and quiet or the classroom is what and quiet";

        nestedStory2 = "Given a classroom with a capacity of 50\n" +
                "When the number of students in the classroom is 40\n" +
                "When the number of broken chairs in the classroom is 10\n" +
                "Then the classroom is full";

        nestedStory3 = "Given a classroom that the number of seats in it is 1337\n" +
                "When the number of students in the classroom is 1378 and the number among them that are standing is 42\n" +
                "Then the classroom is not-full";

        nestedStory4 = "Given a classroom with a capacity of 200\n" +
                "When a dog is in the class, number of chairs he broke is 100\n" +
                "Then the classroom is not-full";



        testClass = ClassroomStoryTest.class;
        derivedTestClass = ClassroomStoryDerivedTest.class;
        nestedNestedTestClass = DogStoryDerivedTest.class;
        tester = new StoryTesterImpl();
    }



    @Test
    public void test1() throws Exception {
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void test2() throws Exception {
        try {
            tester.testOnNestedClasses(badStory, derivedTestClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
            Assert.assertEquals("Then the classroom is not-full", e.getSentance());
            Assert.assertEquals(Arrays.asList("not-full"), e.getStoryExpected());
            Assert.assertEquals(Arrays.asList("full"), e.getTestResult());
        }
    }

    @Test
    public void test3() throws Exception {
        try {
            tester.testOnNestedClasses(nestedStory1, derivedTestClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
            Assert.assertEquals("Then the classroom is not-full and quiet or the classroom is what and quiet", e.getSentance());
            LinkedList<String> expectedList = new LinkedList<String>();
            expectedList.addLast("not-full");
            expectedList.addLast("what");
            LinkedList<String> resultList = new LinkedList<String>();
            resultList.addLast("full");
            resultList.addLast("full");
            Assert.assertEquals(expectedList, e.getStoryExpected());
            Assert.assertEquals(resultList, e.getTestResult());
        }
    }
    @Test
    public void test4() throws Exception {
        try {
            tester.testOnNestedClasses(nestedStory2, derivedTestClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void test5() throws Exception {
        try {
            tester.testOnNestedClasses(nestedStory3, derivedTestClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void test6() throws Exception {
        try {
            tester.testOnNestedClasses(nestedStory4, nestedNestedTestClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void backUpTest() throws Exception {
        try {
            tester.testOnNestedClasses(badStory2, derivedTestClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
            Assert.assertEquals("Then the classroom is not-full", e.getSentance());
            Assert.assertEquals(Arrays.asList("not-full"), e.getStoryExpected());
            Assert.assertEquals(Arrays.asList("full"), e.getTestResult());
            Assert.assertEquals(1, e.getNumFail());
        }
    }

}
