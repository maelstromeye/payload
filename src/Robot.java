
import java.awt.*;

public class Robot {
    private double steering;
    private int center;
    private Detector leftDetector, rightDetector, trace;
    private Engine leftEngine, rightEngine;

    private double integral, derivative, lastError;

    private long accumulatedTrace = 0, maxTrace = 0;
    private Point lastgood = new Point(Tracer.xstart, Tracer.ystart);
    private int state = 0;
    private int count=0;
    private double max_U;
    boolean beg=false, turn=false, mem=false;
    private boolean best = false;


    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    private double angle = Math.PI * 3.0 / 2.0;

    public double getFitness() {
        return fitness;
    }


    private long fitness = 0;

    public Detector getLeftDetector() {
        return leftDetector;
    }

    public void setLeftDetector(Detector leftDetector) {
        this.leftDetector = leftDetector;
    }

    public Detector getRightDetector() {
        return rightDetector;
    }

    public void setRightDetector(Detector rightDetector) {
        this.rightDetector = rightDetector;
    }

    public Engine getLeftEngine() {
        return leftEngine;
    }

    public void setLeftEngine(Engine leftEngine) {
        this.leftEngine = leftEngine;
    }

    public Engine getRightEngine() {
        return rightEngine;
    }

    public void setRightEngine(Engine rightEngine) {
        this.rightEngine = rightEngine;
    }

    public Detector getTrace() {
        return trace;
    }

    public static double getSkew() {
        return skew;
    }

    public static double getBias() {
        return bias;
    }

    private static double skew = Math.acos((Tracer.telescope / Math.sqrt(((double) Tracer.ocular / 2) * ((double) Tracer.ocular / 2) + ((double) Tracer.telescope) * ((double) Tracer.telescope))));
    private static double bias = Math.sqrt(((double) Tracer.ocular / 2) * ((double) Tracer.ocular / 2) + ((double) Tracer.telescope) * ((double) Tracer.telescope));

    public void isBest() {
        best = true;
    }

    public void setTrace(Detector trace) {
        this.trace = trace;
    }


    public Robot() {
        trace = new Detector(new Point(Tracer.xstart, Tracer.ystart), Tracer.tracerad);
        leftDetector = new Detector(new Point(Tracer.xstart - Tracer.ocular / 2, Tracer.ystart - Tracer.telescope), Tracer.detrad);
        rightDetector = new Detector(new Point(Tracer.xstart + Tracer.ocular / 2, Tracer.ystart - Tracer.telescope), Tracer.detrad);
        leftEngine = new Engine(new Point(Tracer.xstart - Tracer.width / 2, Tracer.ystart), Tracer.base);
        rightEngine = new Engine(new Point(Tracer.xstart + Tracer.width / 2, Tracer.ystart), Tracer.base);

        integral = 0;
        derivative = 0;
        lastError = 0;
    }
    private void turnleft()
    {

    }
    private void turnright()
    {

    }
    public void steer() {
        double rvalue=0, lvalue=0;
        double error = 0;
        switch (state) {
            case 0:
                Tracer.base=0.6;
                try
                {
                    rvalue= rightDetector.detect();
                }
                catch (InterruptedException e)
                {
                    if(!beg||(beg&&e.getMessage()=="red")) state=1;
                    break;
                }
                try {
                    lvalue= leftDetector.detect();
                } catch (InterruptedException e) {
                    if(!beg||(beg&&e.getMessage()=="red")) state=2;
                    break;
                }
                error=rvalue-lvalue;
                integral += error * Tracer.dt;
                derivative = (error - lastError) / Tracer.dt;
                steering = Tracer.p * error + Tracer.i * integral + Tracer.d * derivative;


                if (Math.abs(steering) > Tracer.base * 6) {
                    if (steering > 0) {
                        steering = Tracer.base * 6;
                    } else {
                        steering = -Tracer.base * 6;
                    }
                }

                lastError = error;

                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                break;
            case 2:
                count++;
                if(count<15)
                {
                    Tracer.base=0.2;
                    steering=0;
                    leftEngine.setangvel((Tracer.base - steering));
                    rightEngine.setangvel((Tracer.base + steering));
                    break;
                }
                System.out.println("amturn");
                Tracer.base=0.15;
                steering=-5;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                try
                {
                    rvalue=rightDetector.detect();
                }
                catch(InterruptedException e)
                {
                    rvalue=100;
                }
                try
                {
                    leftDetector.detect();
                }
                catch (InterruptedException e)
                {
                    if(rvalue<100) state = 3;
                    else break;
                }
                try{rvalue=rightDetector.detect();} catch(InterruptedException e){}
                if(rvalue==100) state=0;
                turn=false;
                break;
            case 1:
                count++;
                if(count<15)
                {
                    Tracer.base=0.2;
                    steering=0;
                    leftEngine.setangvel((Tracer.base - steering));
                    rightEngine.setangvel((Tracer.base + steering));
                    break;
                }
                System.out.println("amturn");
                Tracer.base=0.15;
                steering=5;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                try
                {
                    lvalue=leftDetector.detect();
                }
                catch(InterruptedException e)
                {
                    lvalue=100;
                }
                try
                {
                    rightDetector.detect();
                }
                catch (InterruptedException e)
                {
                    if(lvalue<100) state = 3;
                    else break;
                }
                try{lvalue=leftDetector.detect();} catch(InterruptedException e){}
                if(lvalue==100) state=0;
                turn=true;
                break;
            case 3:
                count=0;
                Tracer.base=0.2;
                try
                {
                    rvalue= rightDetector.detect();
                }
                catch (InterruptedException e)
                {
                    rvalue=0;
                    if(e.getMessage()=="red") mem=true;
                }
                try
                {
                    lvalue= leftDetector.detect();
                }
                catch (InterruptedException e)
                {
                    lvalue=0;
                    if(e.getMessage()=="red"&&mem) state=9;
                }
                error=rvalue-lvalue;
                integral += error * Tracer.dt;
                derivative = (error - lastError) / Tracer.dt;
                steering = Tracer.p * error + Tracer.i * integral + Tracer.d * derivative;
                if(Math.hypot(trace.getx()-1190, trace.gety()-350)<50) state=4;
                if (Math.abs(steering) > 4) {
                    if (steering > 0) {
                        steering = 4;
                    } else {
                        steering = -4;
                    }
                }
                lastError = error;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                maxTrace += 0;
                break;
            case 4:
            {
                Tracer.base=0.2;
                steering=0;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                if(Math.hypot(trace.getx()-1190, trace.gety()-350)<30) state=5;
                break;
            }
            case 5:
            {
                count++;
                steering=0;
                Tracer.base=0;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                if(count>100)
                {
                    state=6;
                    count=0;
                }
                break;
            }
            case 6:
            {
                count++;
                Tracer.base=-0.2;
                steering=0;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                if(count>50)
                {
                    state=7;
                    count=0;
                }
                break;
            }
            case 7:
            {
                Tracer.base=0;
                steering=-2;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                try
                {
                    rvalue=rightDetector.detect();
                }
                catch (InterruptedException e)
                {
                    rvalue=100;
                }
                if(rvalue==0) beg=true;
                try
                {
                    lvalue=leftDetector.detect();
                }
                catch (InterruptedException e)
                {
                    lvalue=100;
                }
                if(lvalue<=10&&beg)
                {
                    state=8;
                }
                break;
            }
            case 8:
            {
                count=0;
                Tracer.base=0.4;
                try
                {
                    rvalue= rightDetector.detect();
                }
                catch (InterruptedException e)
                {
                    rvalue=0;
                }
                try
                {
                    lvalue= leftDetector.detect();
                } catch (InterruptedException e)
                {
                    lvalue=0;
                }
                error=rvalue-lvalue;
                integral += error * Tracer.dt;
                derivative = (error - lastError) / Tracer.dt;
                steering = Tracer.p * error + Tracer.i * integral + Tracer.d * derivative;
                if (Math.abs(steering) > Tracer.base * 8) {
                    if (steering > 0) {
                        steering = Tracer.base * 8;
                    } else {
                        steering = -Tracer.base * 8;
                    }
                }
                lastError = error;
                leftEngine.setangvel((Tracer.base - steering));
                rightEngine.setangvel((Tracer.base + steering));
                if(lvalue<=45&&rvalue<=45)
                {
                    if(turn) state=1;
                    else state=2;
                }
                break;
            }
            case 9:
                count++;
                if(count<50)
                {
                    Tracer.base=0.2;
                    steering=0;
                    leftEngine.setangvel((Tracer.base - steering));
                    rightEngine.setangvel((Tracer.base + steering));
                    break;
                }
                else state=5;
        }
        System.out.println(state);


    }

    class Engine {
        private Point crd;
        private double angvel;
        private double direction;

        Engine(Point point, double i) {
            crd = point;
            direction = 90;
            angvel = i;
        }

        public double getx() {
            return crd.getX();
        }

        public double gety() {
            return crd.getY();
        }

        public Point getcrd() {
            return crd;
        }

        public void setcrd(double x, double y) {
            crd.setLocation(x, y);
        }

        public double getangvel() {
            return angvel;
        }

        public void setangvel(double i) {
            angvel = i;
        }
    }

    class Detector {
        private Point crd;
        private int radius;

        Detector(Point point, int i) {
            crd = point;
            radius = i;

        }

        public double detect() throws InterruptedException {

            Color color;
            int count = 0;
            double r = 0, g = 0, b = 0;
            for (int x = (int) crd.getX() - radius; x < (int) crd.getX() + radius; x++) {
                for (int y = (int) crd.getY() - radius; y < (int) crd.getY() + radius; y++) {
                    if ((x - crd.getX()) * (x - crd.getX()) + (y - crd.getY()) * (y - crd.getY()) <= radius * radius) {
                        color = new Color(Tracer.track.getRGB(x, y));
                        r += color.getRed();
                        g += color.getGreen();
                        b += color.getBlue();
                        count++;
                    }
                }
            }
            if (g / (r + b) > 250) throw new InterruptedException("green");
            if (r / (g + b) > 10) throw new InterruptedException("red");
            return (r / count + g / count + b / count) / 765 * 100;//+(((Math.random()*10)<=1)?(Math.random()*10):0);
        }

        public double track() {
            Color color;
            int count = 0;
            double r = 0, g = 0, b = 0;
            for (int x = (int) crd.getX() - radius; x < (int) crd.getX() + radius; x++) {
                for (int y = (int) crd.getY() - radius; y < (int) crd.getY() + radius; y++) {
                    if ((x - crd.getX()) * (x - crd.getX()) + (y - crd.getY()) * (y - crd.getY()) <= radius * radius) {
                        color = new Color(Tracer.track.getRGB(x, y));
                        r += color.getRed();
                        g += color.getGreen();
                        b += color.getBlue();
                        count++;
                    }
                }
            }

            return (100 - (r / count + g / count + b / count) / 765 * 100);
        }

        public double getx() {
            return crd.getX();
        }

        public double gety() {
            return crd.getY();
        }

        public Point getcrd() {
            return crd;
        }

        public void setcrd(double x, double y) {
            crd.setLocation(x, y);
        }
    }
}