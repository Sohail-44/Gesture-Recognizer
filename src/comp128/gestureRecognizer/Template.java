package comp128.gestureRecognizer;
import java.util.Deque;

import edu.macalester.graphics.Point;

public class Template { // created the template class
        private String name;
        private Deque<Point> ListTemplate;
    
        public Template(String name, Deque<Point> points){
            this.ListTemplate = points;
            this.name = name;
        }
    
        public Deque<Point> getTemplate(){
            return ListTemplate;
        }
        public String getName(){
            return name;
        }
    }
    

