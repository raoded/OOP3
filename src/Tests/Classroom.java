package Tests;

public class Classroom implements Cloneable {
    private Integer freeSpace ;

    /*
    public Classroom(Classroom classroom) {
        freeSpace = classroom.freeSpace;
    }
    */

    @Override
    public Classroom clone(){
        return new Classroom(freeSpace);
    }

    public Classroom( Integer capacity) {
        this.freeSpace = capacity;
    }
    public void numberOfStudents( Integer numberOfStudents){
        this.freeSpace -= numberOfStudents;
    }
    public void brokenChairs( Integer numberOfBrokenChairs){
        this.freeSpace -= numberOfBrokenChairs;
    }
    public String classroomCondition(){
        return this.freeSpace > 0 ? "not-full" : "full" ;
    }

    public String classroomNoiseCondition() {
        return this.freeSpace > 0 ? "not-quiet" : "noisy";
    }
}
