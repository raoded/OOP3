package Tests;

import org.junit.Assert;

import Solution.Given;
import Solution.Then;
import Solution.When;

public class ClassroomStoryTest {
    protected Classroom classroom ;
    @Given( "a classroom with a capacity of &capacity" )
    public void aClassroom(Integer capacity) {
        classroom = new Classroom(capacity);
    }
    @When( "the number of students in the classroom is &size" )
    public void classroomIsWIthStudents(Integer size) {
        classroom.numberOfStudents(size);
    }
    @Then( "the classroom is &condition" )
    public void theClassroomCondition(String condition) {
        Assert.assertEquals(condition, classroom.classroomCondition());
    }

    @Then( "the classroom is &condition and &noiseCondition" )
    public void theClassroomCondition2(String condition, String noiseCondition)
    {
        Assert.assertEquals(condition, classroom.classroomCondition());
        Assert.assertEquals(noiseCondition, classroom.classroomNoiseCondition());
    }
}
