package Tests;

import org.junit.Assert;

import Solution.Given;
import Solution.Then;
import Solution.When;

public class DogStoryTest {
	protected Dog dog;
	@Given("a Dog of age &age")
	public void aDog(Integer age) {
		dog = new Dog(age);
	}
	
	@When("the dog is not taken out for a walk, the number of hours is &hours")
	public void dogNotTakenForAWalk(Integer hours) {
		dog.notTakenForAWalk(hours);
	}

	@When("the dog is not taken out for a walk, the number of hours is &hours and the time is &time")
	public void dogNotTakenForAWalkAndTime(Integer hours, Integer time) {
		dog.notTakenForAWalk(hours);
	}
	
	@Then("the house condition is &condition")
	public void theHouseCondition(String condition) {
		Assert.assertEquals(condition, dog.houseCondition());
	}
}
