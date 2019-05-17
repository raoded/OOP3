package Tests;

import Solution.Given;
import Solution.When;

public class DogStoryDerivedTest extends DogStoryTest {
	@When("the house is cleaned, the number of hours is &hours")
	private void theHouseCleaned(Integer hours) {
		dog.hoursCleaningFloors(hours);
	}
	
	public class InnerClass extends DogStoryTest {
		@Given("a Dog that his age is &age")
		public void aDog(Integer age) {
			dog = new Dog(age);
		}

		public class InnerInnerClass extends ClassroomStoryDerivedTest {
			@When("a dog is in the class, number of chairs he broke is &chairs")
			public void dogBreaksChairs(Integer chairs) {
				classroom.numberOfStudents(chairs); }
		}
	}

}
