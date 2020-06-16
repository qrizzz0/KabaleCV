import org.opencv.core.*;
import org.opencv.core.Point;

public class ArrayContourObject {

    private double x1, x2, y1, y2;


    public ArrayContourObject(MatOfPoint matOfPoint) {
        x1 = (matOfPoint.toArray()[0].x);
        y1 = (matOfPoint.toArray()[0].y);
        double higestSum = 0;
        int brIndex = 0;


        for (int i=0; i < matOfPoint.toArray().length; i++ ){
            if (higestSum <= (matOfPoint.toArray()[i].x + matOfPoint.toArray()[i].y)){
                higestSum = (matOfPoint.toArray()[i].x + matOfPoint.toArray()[i].y);
                brIndex = i;
            }
        }

        x2 =(matOfPoint.toArray()[brIndex].x);
        y2 =(matOfPoint.toArray()[brIndex].y);
    }


    public Point topLeft(){

    return new Point(x1,y1);
    }

    public Point bottomRight(){

    return new Point(x2,y2);
    }

}