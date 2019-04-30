package Solution;

import Provided.*;
import org.junit.ComparisonFailure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws WordNotFoundException, NoSuchMethodException {
        if(story == null || testClass == null)
            throw new IllegalArgumentException();

        try {
            boolean isWhenSequence = false;
            Object oldInst;
            List<String> whenSequence = new LinkedList<>();

            //first, we create an instance of testClass to invoke our methods
            Object inst = testClass.getDeclaredConstructor().newInstance();

            //first, we parse the story
            List<String> parsedStory = parseStory(story);

            for (String sentence : parsedStory) {
                Class<?> anno = getAnnotationType(sentence);

                if (anno == Given.class) {
                    runGiven(sentence,testClass,inst);
                } else if (anno == When.class) {
                    isWhenSequence = true;

                    ((LinkedList<String>) whenSequence).addLast(sentence);
                } else if(anno == Then.class) {
                    isWhenSequence = false;
                    oldInst = cloneObject(inst,testClass);
                    runWhens((LinkedList<String>) whenSequence,testClass,inst);
                    whenSequence.clear();

                    runThen()
                }

            }
        } catch(Exception e) {
            //TODO : What is neede to throw here?
        }

        //TODO : needs to be implemented
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if(story == null || testClass == null) throw new IllegalArgumentException();

        //TODO : needs to be implemented
    }

    /**
     * This method receives a parameter by string and decides if its an int of a regular string by the &param standards
     *
     * @return true for Int,
     *         false for String
     */
    private static boolean isParamTypeInt(String s) {
        char[] chars = s.toCharArray();

        if(chars.length == 0)
            return false;

        if(chars[0] != '-' && (chars[0] < '0' || chars[0] > '9')) {
            return false;
        }

        for(int i = 1; i<chars.length ; ++i) {
            if(chars[i] < '0' || chars[i] > '9')
                return false;
        }

        return true;
    }

    /**
     * Climbs the super-class chain to find the first method with the given signature which is
     * annotated with the given annotation.
     *
     * @return A method of the requested signature, applicable to all instances of the given
     *         class, and annotated with the required annotation
     * @throws NoSuchMethodException If no method was found that matches this description
     */
    private static Method getAnnotatedMethodFromAncestors(Class<? extends Annotation> annotation, Class c, String valueS)
            throws NoSuchMethodException {
        //first, we make an instance of the current annotation we are looking for up the inheritance tree
        //final Given given = getInstanceOfGivenAnnotation("");

        //now, we want to search for that annotation
        List<Method> methods = stream(c.getDeclaredMethods()).collect(Collectors.toList());

        //TODO : need to check how to use 'value' field inside the annotations

        //we need to set all methods to be accessible since out method can be private or protected
        for (Method m : methods) {
            //Given g = (Given) m.getAnnotation(annotation);
            //g.value();
            m.setAccessible(true);
        }

        //now we filter out the unwanted methods (those without the right primer annotation
        //if there is a match the list should be of size 1, if not it should be 0
        methods = methods.stream().filter(m->m.isAnnotationPresent(annotation)).collect(Collectors.toList());
        //.filter(m->annotationIsEqual((annotation)m.getAnnotation(annotation).value(),valueS)))

        for(Method m : methods) {
            Annotation anno = m.getAnnotation(annotation);
            if(annotationEqualHandler(anno,valueS,m))
            if(anno instanceof Given) {

            }
        }
        if(methods.size() == 0) {
            //if our super class is Object then there is no where else to search
            //TODO : we need to see that this is indeed the STOP condition for this search
            if(c.getSuperclass() == Object.class) {
                throw new NoSuchMethodException();
            }
            //in this case the list is empty so we search recursively in our super class (of test class)
            return getAnnotatedMethodFromAncestors(annotation, c.getSuperclass(), valueS);
        } else {
            return methods.get(0);
        }
    }

    private static boolean annotationEqualHandler(Annotation anno, String valueS, Method m) {


        return true;
    }

    //this method searches and runs a single sentence
    private static void runSentence(Class<? extends Annotation> annotation, String sentence, Class<?> testClass, Object inst) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //finding the method
        Method m = getAnnotatedMethodFromAncestors(annotation, testClass, sentence);

        //I think we did this already - but just in case
        m.setAccessible(true);

        //getting the parameters and putting them in Object[] with their original types (String/int)
        Object[] args = getParametersOfAnnotation(sentence);

        m.invoke(inst, args);
    }

    //This method searches a method of type Given annotation, given a sentence to run and runs it
    private static void runGiven(String sentence, Class<?> testClass, Object inst) throws NoSuchMethodException, GivenNotFoundException {
        try {
            runSentence(Given.class, sentence, testClass, inst);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new GivenNotFoundException();
        }
    }

    //This method searches each When annotated method of type When annotation, given a list of sequential sentences of When type and runs them
    private static void runWhens(LinkedList<String> sentences, Class<?> testClass, Object inst) throws NoSuchMethodException, WhenNotFoundException{
        try {
            for (String sentence : sentences) {
                runSentence(When.class, sentence,testClass,inst);
            }
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new WhenNotFoundException();
        }
    }

    //This method searches a method of type Then annotation, given a sentence to run and runs it by the Then sentence methodology
    private static void runThen(String sentence, Class<?> testClass, Object inst, Object oldInst) throws NoSuchMethodException, ThenNotFoundException{
        String[] splitInvocations = sentence.split("or");

        for (String currentInvocationSentence : splitInvocations) {
            try{
                runSentence(Then.class, sentence, testClass, inst);

                //once we succeed it means that Then clause was fulfilled - we can break our attempts in this stage
                // and go back without rolling back to the old instance
                return;
            }
            catch (ComparisonFailure e) {
                //TODO: need to add implementation regarding failure of Then invocations
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new ThenNotFoundException();
            }
        }

        //if all Then clause attempts failed - we roll back to our old instance that was cloned
        inst = oldInst;
    }

    //This method return the parameters of a certain sentence in an Object[] format
    private static Object[] getParametersOfAnnotation(String sentence) {
        String[] splitted = sentence.split("and");
        Object[] objects = new Object[splitted.length];

        for(int i = 0 ; i<splitted.length; ++i) {
            splitted[i] = splitted[i].substring(splitted[i].lastIndexOf(" &")+1);

            if(isParamTypeInt(splitted[i])) {
                objects[i] = Integer.parseInt(splitted[i]);
            } else {
                objects[i] = splitted[i];
            }
        }

        return objects;
    }



    //this method returns the type of the annotation associated with the string involved
    private static Class<?> getAnnotationType(String valueS) {
        String[] anoType = valueS.split(" ");

        if(anoType[0].equals("Given")) {
            return Given.class;
        } else {
            if(anoType[0].equals("When")) {
                return When.class;
            } else {
                return Then.class;
            }
        }
    }

    private static void runStory(List<String> story,  Class<?> testClass)
            throws IllegalAccessException, InstantiationException, WordNotFoundException {
        Object instance = testClass.newInstance();

        //Method givenPart =

    }

    /**
        This function parses a story to a list dedicating each link to a different annotation sentence
        For example, this story:

         Given a classroom with a capacity of 75
         When the number of students in the classroom is 60
         Then the classroom is not-full

        Turn to this linked list:

         (Given a classroom with a capacity of &75) -> (When the number of students in the classroom is &60) ->
         (Then the classroom is &not-full)

        Thus, later on we can find the parameters by searching for the '&' characters
     */
    private static List<String> parseStory(String story) {
        //separating story lines by new line ( \n )
        String[] lineSplitter = story.split("\n");

        //adding '&' to parameters in the sentences array
        for (String current : lineSplitter) {
            //separating sentence by number of parameters using the word 'and'
            String[] andSplitter = current.split("and");
            for(String currentSubSen : andSplitter) {
                //taking the last word in the current sentence and pushing & before it
                String lastWord = currentSubSen.substring(currentSubSen.lastIndexOf(" ")+1);
                currentSubSen = currentSubSen.substring(0, currentSubSen.lastIndexOf(" ")) + " &" + lastWord;
            }

            //returning the last sentence back to its original place in its new format
            current = "";
            for(String currentSubSen : andSplitter) {
                current += andSplitter;
            }
        }

        return stream(lineSplitter).collect(Collectors.toList());
    }

    //Method to check if two strings look alike in the terms of notation string value
    // (not checking parameter types as Guy told us its not needed string-value() wise)
    private static boolean annotationIsEqual(String s1, String s2) {
        String[] s1ParamSplitter = s1.split("and");
        String[] s2ParamSplitter = s2.split("and");

        //if the number of parameters is not equal, it can't be the same annotation value
        if(s1ParamSplitter.length != s2ParamSplitter.length)
            return false;

        //normlizing the values to have only '&' instead of the parameter name and values
        for(String current1 : s1ParamSplitter) {
            //taking the last word in the current sentence and pushing & instead of it
            current1 = current1.substring(0, current1.lastIndexOf(" ")) + " &";
        }

        for(String current2 : s2ParamSplitter) {
            //taking the last word in the current sentence and pushing & instead of it
            current2 = current2.substring(0, current2.lastIndexOf(" ")) + " &";
        }

        //checking if the strings are look alike
        for (int i = 0; i<s1ParamSplitter.length; ++i) {
            if(!s1ParamSplitter[i].equals(s2ParamSplitter[i]))
                return false;
        }

        return true;
    }

    private static  Object cloneObject(Object inst, Class<?> testClass)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        List<Field> fields = new ArrayList<>(asList(testClass.getDeclaredFields()));
        Object oldInst = testClass.getDeclaredConstructor().newInstance();
        for (Field f : fields) {
            f.setAccessible(true);
            Object fObj = f.get(inst);
            boolean done = false;
            Method met;
            Constructor cons;
            Class c = f.getType();
            if(fObj == null){
                f.set(oldInst, null);
                done = true;
            }
            if(done){
                continue;
            }
            if(fObj instanceof Cloneable){
                met = c.getDeclaredMethod("clone");
                f.set(oldInst, met.invoke(fObj));
                done = true;
            }
            if(done){
                continue;
            }
            try{
                cons = c.getConstructor(c);
                f.set(oldInst, cons.newInstance(fObj));
                done = true;
            }catch (Exception e){
                e.printStackTrace();
            }
            if(done){
                continue;
            }
            f.set(oldInst,fObj);
        }
        return oldInst;
    }
}
