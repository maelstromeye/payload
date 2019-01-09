import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class Tracer extends Thread
{
    private View view;
    public static BufferedImage track;

    public static final int width = 100, ocular = 40, telescope = 40, length = 0, detrad = 8, tracerad = 2, radius = 30;
    public static int xstart=1270, ystart=600;
    public static final double dt = 0.022, p = 4.23, i = 9.69, d = 0.97 ;
    public static double base=0.6;
    private Robot robot;

    private volatile boolean running = false;

    Tracer(View view, BufferedImage image)
    {
        this.view = view;
        this.view.repaint();
        track = image;
        robot=new Robot();
    }

    public static void main(String args[])
    {
        Tracer tracer;
        BufferedImage image;
        try {
            image = ImageIO.read(new File("resources/track2.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        View view = new View(image);
        tracer = new Tracer(view, image);
        tracer.run();
    }
    public void move(){
        double angle;
        robot.steer();
        robot.setAngle(robot.getAngle() + ((double) robot.getLeftEngine().getangvel() - (double) robot.getRightEngine().getangvel()) * radius / width * dt);
        angle = robot.getAngle();
        robot.getTrace().setcrd(robot.getTrace().getx() + Math.cos(angle) * (robot.getLeftEngine().getangvel() + robot.getRightEngine().getangvel()) * dt * 3.14 * radius, robot.getTrace().gety() + Math.sin(angle) * (robot.getLeftEngine().getangvel() + robot.getRightEngine().getangvel()) * dt * 3.14 * radius);
        robot.getLeftDetector().setcrd(robot.getTrace().getx() + Math.cos(angle - robot.getSkew())*  robot.getBias(), robot.getTrace().gety() + Math.sin(angle - robot.getSkew()) * robot.getBias());
        robot.getRightDetector().setcrd(robot.getTrace().getx() + Math.cos(angle + robot.getSkew()) * robot.getBias(), robot.getTrace().gety() + Math.sin(angle + robot.getSkew()) * robot.getBias());
        robot.getLeftEngine().setcrd(robot.getTrace().getx() + Math.cos(angle - 1.5708) * width / 2, robot.getTrace().gety() + Math.sin(angle - 1.5708) * width / 2);
        robot.getRightEngine().setcrd(robot.getTrace().getx() + Math.cos(angle + 1.5708) * width / 2, robot.getTrace().gety() + Math.sin(angle + 1.5708) * width / 2);
        view.load(new Cell(robot.getTrace().getcrd(), robot.getLeftEngine().getcrd(), robot.getRightEngine().getcrd(), robot.getLeftDetector().getcrd(), robot.getRightDetector().getcrd()), 0);


    }
    public void run()
    {
        running = true;
        double time = System.currentTimeMillis();
        while(running)
        {
            move();
            view.repaint();
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
            }

        }

    }

}
class Cell {
    private Point engl, engr, left, right, trace;

    Cell(Point t, Point el, Point er, Point dl, Point dr) {
        trace = t;
        engl = el;
        engr = er;
        left = dl;
        right = dr;
    }

    public Point getengl() {
        return engl;
    }

    public Point getengr() {
        return engr;
    }

    public Point getleft() {
        return left;
    }

    public Point getright() {
        return right;
    }

    public Point gettrace() {
        return trace;
    }
}
class View extends JPanel {
    private Vector<Cell> list;
    private Image track;

    View(Image image) {
        track = image;
        JFrame frame;
        frame = new JFrame("whatevs");
        frame.setSize(2000, 2000);
        frame.add(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusable(true);
        list = new Vector<>();
    }

    public void load(Cell c, int i) {
        if (i >= list.size()) list.add(c);
        else list.set(i, c);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(track, 0, 0, this);
        for (Cell c : list) {
            g.setColor(Color.RED);
            g.fillOval((int) c.getleft().getX() - Tracer.detrad, (int) c.getleft().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) c.getright().getX() - Tracer.detrad, (int) c.getright().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) c.gettrace().getX() - Tracer.tracerad, (int) c.gettrace().getY() - Tracer.tracerad, Tracer.tracerad * 2, Tracer.tracerad * 2);
            g.setColor(Color.GRAY);
            g.fillOval((int) c.getengl().getX() - 10, (int) c.getengl().getY() - 10, 20, 20);
            g.fillOval((int) c.getengr().getX() - 10, (int) c.getengr().getY() - 10, 20, 20);

        }
    }
}