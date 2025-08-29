package SnakeGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class SnakeGame {
    public static void main(String[] args) {
        new MenuFrame();
    }
}

class MenuFrame extends JFrame {
    public MenuFrame() {
        setTitle("Snake Game Menu");    //Title on Top left of window
        setSize(300, 150);      //size of frame
        setLayout(new FlowLayout());    //components will be arranged manually left to right
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //frame will be closed if X button (close) is clicked
        setLocationRelativeTo(null);    //frame will always open in centre of screen
        setResizable(false);
        this.getContentPane().setBackground(Color.BLACK);   //background color of frame

        JButton startButton = new JButton("New Game");  //"new game" button to start game
        JButton exitButton = new JButton("Exit");   //"exit" button to exit main menu

        startButton.setForeground(Color.ORANGE);    //color of text of button
        startButton.setBackground(Color.BLACK);     // color of button
        startButton.setFocusable(false);    // unfocuses/clears border around text in button
        startButton.setFont(new Font("Cascadia Code", Font.PLAIN, 20));     //Font name,font style,font size

        exitButton.setForeground(Color.ORANGE); //color of text of button
        exitButton.setBackground(Color.BLACK);  // color of button
        exitButton.setFocusable(false);     //u nfocuses/clears border around text in button
        exitButton.setFont(new Font("Cascadia Code", Font.PLAIN, 20)); //Font name,font style,font size

        startButton.addActionListener(e -> {
            new GameFrame();
            this.dispose();
        });

        exitButton.addActionListener(e -> {
            System.exit(0);
        });

        add(startButton);
        add(exitButton);
        setVisible(true);
    }
}

class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Snake Game"); //title of second window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //exit on clicking close button
        GamePanel gp = new GamePanel(this);
        add(gp);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private int delay = 75;

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];

    private int bodyParts = 4;  //snake body size
    private int applesEaten;    //no of apples eaten
    private int appleX;     //apple location at x axis
    private int appleY;     //apple location at y axis
    private char direction = 'R';   //default direction "Right"
    private boolean running = false;    //default position of snake

    private Timer timer;    //timer class object
    private Random random;  //random class to generate apples at random places
    private int seconds = 0;    //seconds timer initial value
    private int minutes = 0;    //minutes timer initial value
    private Timer clock;    //another timer class object

    private int highScore = 0;  //initial value of highscore
    private int level = 1;  //initial value of difficulty level
    private final String scoreFile = "E:\\Swing\\SnakeGame\\src\\SnakeGame\\score.txt";  //file absolute path to store data

    private JFrame parentFrame;

    public GamePanel(JFrame parent) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));   //panel size
        setBackground(Color.black); //panel background color
        setFocusable(true); //allows panel to accept keyboard focus
        addKeyListener(new MyKeyAdapter()); //allows keyboard input
        random = new Random();  //random class obj called
        loadHighScore();    //class called
        startGame();    //class called
    }

    public void startGame() {
        newApple();     //class called
        running = true; //snake runs
        applesEaten = 0;    //applesEaten initialized
        delay = 75; //speed of snake (refresh rate ms)
        timer = new Timer(delay, this); //timer class obj called(delay in snake movement, gamepanel is the actionlistener)
        timer.start();  //starts game and calls actonlistener input every 75ms

        seconds = 0;    //sec timer initialized
        minutes = 0;    //min timer initialized
        clock = new Timer(1000, e -> {  //another timer obj
            seconds++;  //sec++ every 1000ms(1 sec)
            if (seconds == 60) {
                seconds = 0;
                minutes++;
            }
        });
        clock.start();  //obj 2 of timer class started takng actionlistener
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {  //if snake is running
            g.setColor(Color.RED);  //color of apple
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);   //x,y,height,width of apple created

            for (int i = 0; i < bodyParts; i++) {   //dry run
                g.setColor(i == 0 ? new Color(100, 255, 0) : new Color(45, 180, 0));
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);   //snake body created
            }

            g.setColor(Color.white);    //styling of score board
            g.setFont(new Font("Cascadia Code", Font.PLAIN, 20));
            g.drawString("Score: " + applesEaten, 10, 25);
            g.drawString("High Score: " + highScore, 10, 50);
            g.drawString("Level: " + level, 10, 75);
            g.drawString(String.format("Time: %02d:%02d", minutes, seconds), 10, 100);
        } else {
            gameOver();
        }
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();

            if (applesEaten % 5 == 0) {
                delay = (int) (delay * 0.8);    //casts back the float into int
                timer.setDelay(delay); //expects int only
                level++;
            }
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            clock.stop();
            saveScore();
            gameOver();
        }
    }

    public void saveScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile, true))) { //false = prev data delete
            writer.write("Score: " + applesEaten + ", Time: " + String.format("%02d:%02d", minutes, seconds));
            writer.newLine();   //available only by bufferwriter class while writer() is from ioexception lib
        } catch (IOException e) {
            e.printStackTrace();    //prints exactly where and why error occured
        }

        if (applesEaten > highScore) {
            highScore = applesEaten;
        }
    }

    public void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("E:\\Swing\\SnakeGame\\src\\SnakeGame\\score.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Score: ")) {
                    int score = Integer.parseInt(line.split(",")[0].split(": ")[1]);
                    highScore = Math.max(highScore, score);
                }
            }
        } catch (IOException e) {
        }
    }

    public void gameOver() {
        int result = JOptionPane.showOptionDialog(
                this,
                "Game Over!",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Restart", "Exit"},
                "Restart"
        );

        if (result == JOptionPane.YES_OPTION) {
            parentFrame.dispose();
            new GameFrame();
        } else {
            parentFrame.dispose();
            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R') direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L') direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D') direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U') direction = 'D';
                }
            }
        }
    }
}