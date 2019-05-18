package Tests;

public class Dog implements Cloneable {
	private int age,hoursCleaningFloors;
	private boolean peeOnFloor;
	
	public Dog(Dog g) {
		this.age = g.age;
		this.hoursCleaningFloors=g.hoursCleaningFloors;
		this.peeOnFloor = g.peeOnFloor;
	}

	@Override
	public Dog clone() {
		Dog g = new Dog(this.age);
		this.hoursCleaningFloors=g.hoursCleaningFloors;
		this.peeOnFloor = g.peeOnFloor;

		return g;
	}

	public Dog(int age) {
		this.age = age;
		this.hoursCleaningFloors=0;
		this.peeOnFloor = false;
	}

	public void notTakenForAWalk(int hours) {
		if(hours>8&& this.age > 5){
			this.peeOnFloor=true;
			hoursCleaningFloors=0;
		}
	}
	
	public void hoursCleaningFloors(int hours) {
		if(this.peeOnFloor){
			this.hoursCleaningFloors=this.hoursCleaningFloors+hours;
		}
		if(this.hoursCleaningFloors>10){
			this.peeOnFloor=false;
			this.hoursCleaningFloors=0;
		}
	}
	
	public String houseCondition() {
		if(this.peeOnFloor){
			return "smelly";
		}
		return "clean";
	}
}
