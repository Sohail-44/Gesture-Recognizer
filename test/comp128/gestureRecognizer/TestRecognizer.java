package comp128.gestureRecognizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import edu.macalester.graphics.Point;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by bjackson on 11/1/2016.
 */
public class TestRecognizer {

   private Recognizer recognizer;
   private Deque<Point> originalPoints;

   private static final int ORIGINAL_N = 20;

   @BeforeEach
   public void setup(){
       recognizer = new Recognizer();
       originalPoints = new ArrayDeque<>(ORIGINAL_N);
       for(int i=0; i < ORIGINAL_N; i++){
           originalPoints.offerLast(new Point(i, 0));
       }
   }

    // /**
    //  * Tests that points are resampled correctly
    //  * Edited by Lucy Manalang on 2/22/2024
    //  */
    @Test
    public void testResample(){
        int n = 10;
        Deque<Point> resampled = recognizer.resample(originalPoints, n);
        assertEquals(n, resampled.size()); // resampling should return the correct number of points

        double interval = (ORIGINAL_N-1.0)/(n-1.0); //Path length is 19, so interval should be 19/(n-1) with n=10;

        Iterator<Point> it = resampled.iterator();
        double i=0;
        while (it.hasNext()){ //this test is a simple test that will not catch all errors,
            Point point = it.next(); // it is just here to make debugging easier
            assertEquals(i, point.getX(), 0.01);
            assertEquals(0, point.getY(), 0.01);
            i+=interval;
        }

        IOManager ioManager = new IOManager();
        Deque<Point> resampledCircle = ioManager.loadGesture("resampledCircle.xml");
        Deque<Point> circleTemplate = ioManager.loadGesture("circleTemplate.xml");
        resampled = recognizer.resample(circleTemplate,64);

        Iterator<Point> resampleIterator = resampledCircle.iterator();
        Iterator<Point> circleIterator = resampled.iterator();
        while (circleIterator.hasNext()){
            Point point1 = circleIterator.next();
            Point point2 = resampleIterator.next();
            assertEquals(point1.getX(), point2.getX(), 0.01);
            assertEquals(point1.getY(), point2.getY(), 0.01);
        }
    }

//    /**
//     * Tests the path length.
//     */
   @Test
   public void testPathLength(){
       assertEquals(ORIGINAL_N-1, recognizer.pathLength(originalPoints), 0.0001);
       assertEquals(ORIGINAL_N, originalPoints.size());
   }

//    /**
//     * Tests that the indicative angle (the angle needed to rotate the first point around the centroid to line up with the positive x axis)
//     * is correct. With points (0,0) through (19,0) the first point is on the x axis but to the left of the centroid (-x axis ) so it must rotate by pi.
//     */
   @Test
   public void testIndicativeAngle(){
       double angle = recognizer.indicativeAngle(originalPoints);
       assertEquals(0.0, angle, 0.001);
       assertEquals(ORIGINAL_N, originalPoints.size());

       Deque<Point> reversed = new ArrayDeque<>(originalPoints.size());
       Iterator<Point> it = originalPoints.descendingIterator();
       while(it.hasNext()){
           reversed.offerLast(it.next());
       }

       angle = recognizer.indicativeAngle(reversed);
       assertEquals(Math.PI, angle, 0.001);
   }

//    /**
//     * Tests rotation.
//     * The gesture starts at the points (0,0) to (19,0). When rotated by pi around the centroid the order should reverse.
//     */
   @Test
   public void testRotateBy(){
       Deque<Point> rotated = recognizer.rotateBy(originalPoints, Math.PI);

       assertEquals(ORIGINAL_N, originalPoints.size());
       assertEquals(new Point(0,0), originalPoints.peekFirst());
       Iterator<Point> it = rotated.iterator();
       double i=ORIGINAL_N-1.0;
       while (it.hasNext()){
           Point point = it.next();
           assertEquals(i, point.getX(), 0.001);
           assertEquals(0, point.getY(), 0.001);
           i-=1.0;
       }

   }

    @Test
        public void testRotateBy45deg(){
            double quarterPI = Math.PI/4;
            Deque<Point> rotated = recognizer.rotateBy(originalPoints, quarterPI);
            Iterator<Point> it = rotated.iterator();
            double i = 0;
            while (it.hasNext()){
                Point point = it.next();
                // The original points are (0,0)...(19,0) along the x axis.
                // Rotated by 45 deg around the centroid (9.5, 0), we would expect point (0,0)
                // to become (2.782486, -6.717514)
                double expectedX = (i-9.5)*Math.cos(quarterPI)+9.5;
                double expectedY = (i-9.5)*Math.sin(quarterPI);
                assertEquals(expectedX, point.getX(), 0.001);
                assertEquals(expectedY, point.getY(), 0.001);
                i++;
            }
        }

//     /**
    //  * Tests scaling by creating a 100 by 400 size box and scaling it to 200 by 200
    //  */
    @Test
    public void testScaleTo(){
        Deque<Point> box = new ArrayDeque<>(4);
        box.add(new Point(100,100));
        box.add(new Point(200, 100));
        box.add(new Point(200,500));
        box.add(new Point(100, 500));
        Deque<Point> scaled = recognizer.scaleTo(box, 200);


        assertEquals(4, scaled.size());
        Iterator<Point> itScaled = scaled.iterator();
        Iterator<Point> itBox = box.iterator();
        while (itScaled.hasNext()){
            Point scaledPoint = itScaled.next();
            Point boxPoint = itBox.next();
            assertEquals(boxPoint.getX()*2, scaledPoint.getX()); 
            assertEquals(boxPoint.getY()*0.5, scaledPoint.getY());
        }
    }
    // I am struggling a bit in this test
    

//    /**
//     * Tests that translating the points moves the centroid to the indicated point.
//     */
   @Test
   public void testTranslateTo(){
       Deque<Point> translated = recognizer.translateTo(originalPoints, new Point(0.0,0.0));
       assertEquals(ORIGINAL_N, originalPoints.size());
       assertEquals(new Point(0,0), originalPoints.peekFirst());

       Iterator<Point> it = translated.iterator();
       double i=-(ORIGINAL_N-1.0)/2.0;
       while (it.hasNext()){
           Point point = it.next();
           assertEquals(i, point.getX(), 0.001);
           assertEquals(0, point.getY(), 0.001);
           i+=1.0;
       }
   }

//    /**
//     * Tests that pathDistance is correct
//     */
   @Test
   public void testPathDistance() {
    Deque<Point> shiftedPoints = new ArrayDeque<>(originalPoints.size());
    for(Point point : originalPoints){
        shiftedPoints.add(new Point(point.getX(), point.getY()+1.0));
    }

    double distance = recognizer.pathDistance (originalPoints, shiftedPoints);
    assertEquals(1.0, distance, 0.000001);
    assertNotEquals(20.0, distance, 0.00001);  //Make sure you are dividing by N as in eq. 1 in the paper to get the average path distance.

    // Now try it with a random arrow gesture. Make sure testResample works first or this will be wrong!
    IOManager ioManager = new IOManager();
    Deque<Point> templateGesture = recognizer.resample(ioManager.loadGesture("arrowTemplate.xml"), 64);
    Deque<Point> testGesture = recognizer.resample(ioManager.loadGesture("arrowTest.xml"), 64);
    distance = recognizer.pathDistance(templateGesture, testGesture);
    assertEquals(16.577074, distance, 0.000001);
}

//    //TODO: Test centroid and boundingBox methods

@Test
public void testGetCentroid() {
    // Created a simple square and adding points to it 
    Deque<Point> square = new ArrayDeque<>(4);
    square.add(new Point(0, 0));
    square.add(new Point(10, 0));
    square.add(new Point(10, 10));
    square.add(new Point(0, 10));
    
    Deque<Point> centered = recognizer.translateTo(square, new Point(0, 0));
    
    // The centroid of the square is (5, 5)
    // After translating to origin, points should be centered around (0, 0)
    // So the square corners should be at (-5, -5), (5, -5), (5, 5), (-5, 5)
    Iterator<Point> it = centered.iterator();
    assertEquals(new Point(-5, -5), it.next(), "First point should be at (-5, -5)");
    assertEquals(new Point(5, -5), it.next(), "Second point should be at (5, -5)");
    assertEquals(new Point(5, 5), it.next(), "Third point should be at (5, 5)");
    assertEquals(new Point(-5, 5), it.next(), "Fourth point should be at (-5, 5)");
}

@Test
public void testBounding() {
    // Created a rectangle from (10, 20) to (30, 50)
    Deque<Point> rectangle = new ArrayDeque<>(4);
    rectangle.add(new Point(10, 20));
    rectangle.add(new Point(30, 20));
    rectangle.add(new Point(30, 50));
    rectangle.add(new Point(10, 50));
    
    // test bounding indirectly through scaleTo
    Deque<Point> scaled = recognizer.scaleTo(rectangle, 100);
    
    // Original bounds: width=20, height=30
    // Scaling to 100
    Iterator<Point> it = scaled.iterator();
    Point p1 = it.next();
    Point p2 = it.next();
    Point p3 = it.next();
    Point p4 = it.next();
    
    assertEquals(50, p2.getX() - p1.getX(), 0.001, "Width should be scaled to 100");
    assertEquals(100, p3.getY() - p2.getY(), 0.001, "Height should be scaled to 100");
}


//    /**
//     * Test the recognition and scoring
//     */
   @Test
   public void testRecognize(){
       //canvas.getWindowFrame().dispose();
       IOManager ioManager = new IOManager();
       Deque<Point> templateGesture = ioManager.loadGesture("arrowTemplate.xml");
       Deque<Point> circleTemplate = ioManager.loadGesture("circleTemplate.xml");
       //TODO: Add gestures as templates in your recognizer
       recognizer.addTemplate("arrow", templateGesture);
       recognizer.addTemplate("circle", circleTemplate);

       Deque<Point> testGesture = ioManager.loadGesture("arrowTest.xml");
       //TODO: Recognize the testGesture against the template Gestures.
       Match result = recognizer.recognize(testGesture);

       //TODO: set score to the recognition score.
       double score = result.getScore();

       assertEquals(0.888684, score, 0.001); // testGesture should match against templateGesture with a score of 0.88
       // If you get 0.89 you are likely rotating by the positive indicative angle rather than the correct negative angle.
       assertEquals("arrow", result.getTermpName(), "Should recognize as arrow");

       //TODO: Recognize the template gesture against itself
       result = recognizer.recognize(templateGesture);

       //TODO: set score to the new recognition score
       score = result.getScore();
       assertEquals(1.0, score, 0.01); // A template matched with itself should be a perfect match
       assertEquals("arrow", result.getTermpName(), "Should recognize as arrow");
   }
}

// For me the testRecognize is giving very small margin error eventhough the UI works fine.


