package comp128.gestureRecognizer;

import edu.macalester.graphics.*;
import edu.macalester.graphics.Point;
import edu.macalester.graphics.events.MouseButtonEvent;
import edu.macalester.graphics.events.MouseMotionEvent;
import edu.macalester.graphics.events.MouseMotionEventHandler;
import edu.macalester.graphics.ui.Button;
import edu.macalester.graphics.ui.TextField;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import edu.macalester.graphics.Line;
/**
 * The window and user interface forr drawing gestures and automatically recognizing them
 * Created by bjackson on 10/29/2016.
 */
public class GestureApp {

    private CanvasWindow canvas;
    private Recognizer recognizer;
    private IOManager ioManager;
    private GraphicsGroup uiGroup;
    private Button addTemplateButton;
    private TextField templateNameField;
    private GraphicsText matchLabel;
    private Deque<Point> path;
    private Point firstPoint;


    public GestureApp(){
        canvas = new CanvasWindow("Gesture Recognizer", 600, 600);
        recognizer = new Recognizer();
        path = new ArrayDeque<>();
        ioManager = new IOManager();
        setupUI();
    }

    /**
     * Create the user interface
     */
    private void setupUI(){
        matchLabel = new GraphicsText("Match: ");
        matchLabel.setFont(FontStyle.PLAIN, 24);
        canvas.add(matchLabel, 10, 30);

        uiGroup = new GraphicsGroup();

        templateNameField = new TextField();

        addTemplateButton = new Button("Add Template");
        addTemplateButton.onClick( () -> addTemplate() );

        Point center = canvas.getCenter();
        double fieldWidthWithMargin = templateNameField.getSize().getX() + 5;
        double totalWidth = fieldWidthWithMargin + addTemplateButton.getSize().getX();


        uiGroup.add(templateNameField, center.getX() - totalWidth/2.0, 0);
        uiGroup.add(addTemplateButton, templateNameField.getPosition().getX() + fieldWidthWithMargin, 0);
        canvas.add(uiGroup, 0, canvas.getHeight() - uiGroup.getHeight());

        Consumer<Character> handleKeyCommand = ch -> keyTyped(ch);
        canvas.onCharacterTyped(handleKeyCommand);

        // Adding the mouse listeners to allow the user to draw and add the points to the path variable.
        
        // Handles the mouse down - start a new gesture
        canvas.onMouseDown((MouseButtonEvent event) -> {
            // Clears the canvas and resets the path
            removeAllNonUIGraphicsObjects();
            path.clear();
            
            
            
            
            // Stores the first point
            firstPoint = new Point(event.getPosition().getX(), event.getPosition().getY());
            path.add(firstPoint);
            
            // Draws the initial point
            Line line = new Line(firstPoint, firstPoint);
            line.setStrokeWidth(5);
            canvas.add(line);
        });

        // Handles the mouse drag - continue the gesture
        canvas.onDrag((MouseMotionEvent event) -> {
            // Create the line from the previous position to the current position
            Line line = new Line(event.getPreviousPosition(), event.getPosition());
            line.setStrokeWidth(5);
            canvas.add(line);
            
            // Adds the current point to the path
            path.add(event.getPosition());
        });

        // Handle the mouse up - recognize the gesture
        canvas.onMouseUp((MouseButtonEvent event) -> {
            // Only attempt to recognize if we have enough points
            if (path.size() >= 2) {
                Match match = recognizer.recognize(path);
                
                // Updates the match label with the result
                if (match != null) {
                    matchLabel.setText("Match: " + match.getTermpName() + " (" + 
                                      String.format("%.2f", match.getScore() * 100) + "%)");
                } else {
                    matchLabel.setText("Match: None");
                }
            }
        });
    }

    /**
     * Clears the canvas, but preserves all the UI objects
     */
    private void removeAllNonUIGraphicsObjects() {
        canvas.removeAll();
        canvas.add(matchLabel);
        canvas.add(uiGroup);
    }

    /**
     * Handle what happens when the add template button is pressed. This method adds the points stored in path as a template
     * with the name from the templateNameField textbox. If no text has been entered then the template is named with "no name gesture"
     */
    private void addTemplate() {
        String name = templateNameField.getText();
        if (name.isEmpty()){
            name = "no name gesture";
        }
        
        // Only adds template if we have enough points
        if (path.size() >= 2) {
            recognizer.addTemplate(name, path); // Add the points stored in the path as a template
            matchLabel.setText("Added template: " + name);
        } else {
            matchLabel.setText("Please draw a gesture first");
        }
    }

    /**
     * Handles keyboard commands used to save and load gestures for debugging and to write tests.
     * Note, once you type in the templateNameField, you need to call canvas.requestFocus() in order to get
     * keyboard events. This is best done in the mouseDown callback on the canvas.
     */
    public void keyTyped(Character ch) {
        if (ch.equals('L')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            Deque<Point> points = ioManager.loadGesture(name+".xml");
            if (points != null){
                recognizer.addTemplate(name, points);
                System.out.println("Loaded "+name);
                matchLabel.setText("Loaded template: " + name);
            }
        }
        else if (ch.equals('s')){
            String name = templateNameField.getText();
            if (name.isEmpty()){
                name = "gesture";
            }
            if (path.size() >= 2) {
                ioManager.saveGesture(path, name, name+".xml");
                System.out.println("Saved "+name);
                matchLabel.setText("Saved gesture: " + name);
            } else {
                matchLabel.setText("Please draw a gesture first");
            }
        }
    }

    public static void main(String[] args){
        GestureApp window = new GestureApp();
        // the UI works well!  
    }
}