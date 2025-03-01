package comp128.gestureRecognizer;

public class Match {
    private String theTemplate;
    private double matchingScore;

    public Match(String model, double score){
        matchingScore = score;
        theTemplate = model;
    }
    public double getScore(){
        return matchingScore;
    }

    public String getTermpName(){
        return theTemplate;
    }
    
}
