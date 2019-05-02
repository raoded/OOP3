package Solution;

import Provided.*;
import org.junit.ComparisonFailure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class StoryTesterImpl implements StoryTester {
    private static boolean firstFail = false;
    private static int numFail = 0;
    private static LinkedList<String> storyExpceted = new LinkedList<>(), storyResults=new LinkedList<>();

    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass)
            throws WordNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, StoryTestExceptionImpl {
        if(story == null || testClass == null)
            throw new IllegalArgumentException();

        String firstFailedSentence = "";

        Object oldInst;
        LinkedList<String> whenSequence = new LinkedList<>();

        //first, we create an instance of testClass to invoke our methods
        Object inst = testClass.getDeclaredConstructor().newInstance();

        //first, we parse the story
        List<String> parsedStory = parseStory(story);

        for (String sentence : parsedStory) {
            Class<?> anno = getAnnotationType(sentence);

            if (anno == Given.class) {
                runGiven(sentence,testClass,inst);
            } else if (anno == When.class) {
                //in this case we dont invoke the When sentences until they are all read sequentially
                //we save all the When sentences in a list until the next Then sentence
                whenSequence.addLast(sentence);
            } else if(anno == Then.class) {
                //now we save the old instance of the testClass before invoking the When annotated methods
                oldInst = cloneObject(inst,testClass);

                //now we run the entire When sentences that were read so far
                runWhens(whenSequence,testClass,inst);

                //after invoking them, we clear the list for the next run
                whenSequence.clear();

                boolean lastIsAnyThenFailed = firstFail;

                //now we can run the current Then sentence - this call returns the instance accordingly to success of the Then clause (oldInst if failed)
                inst = runThen(sentence,testClass,inst,oldInst);

                //this means that the last invocation changed isAnyThenFailed from false to true, that means that this is the first Then sentence that failed
                // so we need to start handling the storyTestException instance
                if(lastIsAnyThenFailed != firstFail) {
                    firstFailedSentence = oldSentence(sentence);
                }
            }
        }

        if(firstFail){
            throw new StoryTestExceptionImpl(firstFailedSentence,storyExpceted,storyResults,numFail);
        }
    }

    //This method gets a sentence with marked parameters (&) and returns a "clean" sentence(the way it was in the story)
    private String oldSentence(String sentence) {
        String[] ampSplitter = sentence.split("&");

        for(int i = 0; i<ampSplitter.length-1; ++i) {
            String lastWord = ampSplitter[i].substring(ampSplitter[i].lastIndexOf("&") + 1);
            ampSplitter[i] = ampSplitter[i].substring(0, ampSplitter[i].lastIndexOf("&") - 1) + lastWord;
        }

        StringBuilder oldString = new StringBuilder();

        for(int i = 0; i<ampSplitter.length-1; ++i) {
            oldString.append(ampSplitter[i]);
        }

        return oldString.toString();
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if(story == null || testClass == null) throw new IllegalArgumentException();

        String first_sentence=parseStory(story).get(0);
        Class<?> actual_class=findNestedClass(first_sentence, testClass);
        testOnInheritanceTree(story,actual_class);
    }

    private static Class<?> findNestedClassesRecursivly(String first_sentence, Class<?> testClass) throws NoSuchMethodException{
        try {
            getAnnotatedMethodFromAncestors(Given.class,testClass,first_sentence);
            return testClass;
        }
        catch (NoSuchMethodException e)
        {
            Class[] inner_classes=testClass.getDeclaredClasses();

            for (Class inner_class : inner_classes) {
                try {
                    getAnnotatedMethodFromAncestors(Given.class, inner_class, first_sentence);
                    return inner_class;
                } catch (NoSuchMethodException ignored) { }
            }

            for (Class inner_class : inner_classes) {
                try {
                    return findNestedClassesRecursivly(first_sentence, inner_class);
                } catch (NoSuchMethodException ignored) { }
            }
        }
        throw new NoSuchMethodException();
    }

    private static Class<?> findNestedClass(String first_sentence, Class<?> testClass) throws GivenNotFoundException {
        try {
            getAnnotatedMethodFromAncestors(Given.class,testClass,first_sentence);
            return testClass;
        } catch (NoSuchMethodException ignored) {}

        Class[] inner_classes=testClass.getDeclaredClasses();

        for (Class inner_class : inner_classes) {
            try {
                return findNestedClassesRecursivly(first_sentence, inner_class);
            } catch (NoSuchMethodException ignored) { }
        }

        // if we havent returned until now
        throw new GivenNotFoundException();
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
        //now, we want to search for that annotation
        List<Method> methods = stream(c.getDeclaredMethods()).collect(Collectors.toList());

        //we need to set all methods to be accessible since our method can be private or protected
        for (Method m : methods) {
            m.setAccessible(true);
        }

        //now we filter out the unwanted methods (those without the right primer annotation
        methods = methods.stream().filter(m->m.isAnnotationPresent(annotation)).collect(Collectors.toList());

        for(Method m : methods) {
            Annotation anno = m.getAnnotation(annotation);
            if(annotationEqualHandler(anno,valueS)) {
                return m;
            }
        }

        //if this returns null then (c == Object.class) so there is no more classes to observe up the inheritance tree
        if(c.getSuperclass() == null) {
            throw new NoSuchMethodException();
        }
        //in this case the list is empty so we search recursively in our super class (of test class)
        return getAnnotatedMethodFromAncestors(annotation, c.getSuperclass(), valueS);
    }

    private static boolean annotationEqualHandler(Annotation anno, String valueS) {
        if(anno instanceof Given) {
            return annotationIsEqual(((Given) anno).value(),valueS);
        }

        if(anno instanceof When) {
            return annotationIsEqual(((When) anno).value(),valueS);
        }

        if(anno instanceof Then) {
            return annotationIsEqual(((Then) anno).value(),valueS);
        }

        return false;
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
    private static void runGiven(String sentence, Class<?> testClass, Object inst) throws GivenNotFoundException {
        try {
            runSentence(Given.class, sentence, testClass, inst);
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new GivenNotFoundException();
        }
    }

    //This method searches each When annotated method of type When annotation, given a list of sequential sentences of When type and runs them
    private static void runWhens(LinkedList<String> sentences, Class<?> testClass, Object inst) throws WhenNotFoundException{
        try {
            for (String sentence : sentences) {
                runSentence(When.class, sentence,testClass,inst);
            }
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new WhenNotFoundException();
        }
    }

    //This method searches a method of type Then annotation, given a sentence to run and runs it by the Then sentence methodology
    private static Object runThen(String sentence, Class<?> testClass, Object inst, Object oldInst) throws ThenNotFoundException{
        String[] splitInvocations = sentence.split("or ");

        //now we remove all the instances of the word "or " from our sentences (we dont need it for the annotation searching
        //"or " is the last word of each sub-sentence except the last one
        for (int i = 0; i<splitInvocations.length-1; ++i) {
            splitInvocations[i] = splitInvocations[i].substring(splitInvocations[i].lastIndexOf(" ") + 1);
        }

        for (String currentInvocationSentence : splitInvocations) {
            try{
                runSentence(Then.class, currentInvocationSentence, testClass, inst);

                //once we succeed it means that Then clause was fulfilled - we can break our attempts in this stage
                // and go back without rolling back to the old instance
                return inst;
            }
            catch (ComparisonFailure e) {
                if(!firstFail) {
                    storyExpceted.addLast(e.getExpected());
                    storyResults.addLast(e.getActual());
                }
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new ThenNotFoundException();
            }
        }

        //if all Then clause attempts failed - we roll back to our old instance that was cloned
        if(!firstFail) {
            firstFail = true;
        }

        ++numFail;

        return oldInst;
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
        for(int i=0; i<lineSplitter.length; ++i) {
            //separating sentence by number of parameters using the word 'and'
            String[] andSplitter = lineSplitter[i].split("and");

            for(int j = 0; j<andSplitter.length; ++j) {
                //taking the last word in the current sentence and pushing & before it
                String lastWord = andSplitter[j].substring(andSplitter[j].lastIndexOf(" ")+1);
                andSplitter[j] = andSplitter[j].substring(0, andSplitter[j].lastIndexOf(" ")) + " &" + lastWord;
            }

            //returning the last sentence back to its original place in its new format
            lineSplitter[i] = "";
            for(String currentSubSen : andSplitter) {
                lineSplitter[i] += currentSubSen;
            }
        }

        //we return the array of strings as a list (can be easier to handle later on)
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
        for(int i = 0; i < s1ParamSplitter.length; ++i) {
            //taking the last word in the current sentence and pushing & instead of it
            s1ParamSplitter[i] = s1ParamSplitter[i].substring(0, s1ParamSplitter[i].lastIndexOf(" ")) + " &";
        }

        for(int i = 0; i < s2ParamSplitter.length; ++i) {
            //taking the last word in the current sentence and pushing & instead of it
            s2ParamSplitter[i] = s2ParamSplitter[i].substring(0, s2ParamSplitter[i].lastIndexOf(" ")) + " &";
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
