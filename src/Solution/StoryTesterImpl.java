package Solution;

import Provided.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws WordNotFoundException, NoSuchMethodException {
        if(story == null || testClass == null)
            throw new IllegalArgumentException();

        try {
            //first, we create an instance of testClass to invoke our methods
            Object inst = testClass.getDeclaredConstructor().newInstance();

            //first, we parse the story
            List<String> parsedStory = parseStory(story);

            for (String sentence : parsedStory) {
                Class<?> anno = getAnnotationType(sentence);

                if (anno == Given.class) {
                    Method m = getAnnotatedMethodFromAncestors((Class<? extends Annotation>) anno, testClass, sentence);
                    Object[] args = getParametersOfAnnotation(sentence);

                    m.invoke(inst,args);

                    //.invoke(inst);
                }

                if (anno == When.class) {
                    try {
                        getAnnotatedMethodFromAncestors((Class<? extends Annotation>) anno, testClass, sentence).invoke(inst);
                    } catch (NoSuchMethodException e) {
                        throw new WhenNotFoundException();
                    }


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
        List<Method> methods = stream(c.getMethods())
                .filter(m->m.isAnnotationPresent(annotation))
                .filter(m->annotationIsEqual(m.getAnnotation(annotation).value,valueS))
                .collect(Collectors.toList());

        if(methods.size() == 0) {
            if(c.getSuperclass() == Object.class) {
                throw new NoSuchMethodException();
            }
            return getAnnotatedMethodFromAncestors(annotation, c.getSuperclass(), valueS);
        } else {
            return methods.get(0);
        }
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

    /*
    //This is a constructor of a '@Given' annotation 
    Given getInstanceOfGivenAnnotation(final String s) {
        Given anno = new Given(){
            public Class<? extends Annotation> annotationType() {
                Field f = anno.getClass().getField("value");
                f.setAccessible(true);
                try {
                    f.set(anno.value, s);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return Given.class;
            }
        };

        return anno;
    }
    */

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
