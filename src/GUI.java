import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class GUI extends JFrame {
    public GUI () {
        // User can choose between 4 different options to test application
        Scanner reader = new Scanner(System.in);

        System.out.println("""
                Please type the number of the mode you would like to use. Modes 1-3 will find the
                connection between two pre-selected movies that we know have a short connection so you can
                see that it works, and they get progressively harder. Mode 4 finds the connection between two
                random movies, but this mode may take a long time to run.""");
        System.out.println("""
                1. Find the connection between Dune and Dune: Part Two!
                2. Find the connection between La La Land and Spider-Man 2!
                3. Find the connection between Oppenheimer and Avengers: Infinity War!
                4. Find the connection between two random movies!
                """);

        int x = Integer.parseInt(reader.nextLine());

        String movie1Name = "";
        Document movie1Doc = null;
        String movie2Name = "";

        if (x == 1) {
            try {
                movie1Name = "Dune";
                movie1Doc = Jsoup.connect("https://en.wikipedia.org/wiki/Dune_(2021_film)").get();
                movie2Name = "Dune: Part Two";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (x == 2) {
            try {
                movie1Name = "La La Land";
                movie1Doc = Jsoup.connect("https://en.wikipedia.org/wiki/La_La_Land").get();
                movie2Name = "Spider-Man 2";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (x == 3) {
            try {
                movie1Name = "Oppenheimer";
                movie1Doc = Jsoup.connect("https://en.wikipedia.org/wiki/Oppenheimer_(film)").get();
                movie2Name = "Avengers: Infinity War";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (x == 4) {
            Map.Entry<String, Document> movie1 = movieInfoGetter.getRandomMovie();
            movie1Name = movie1.getKey();
            movie1Doc = movie1.getValue();
            Map.Entry<String, Document> movie2 = movieInfoGetter.getRandomMovie();
            movie2Name = movie2.getKey();
        } else {
            System.out.println("Invalid number");
        }

        // Uses bfs to find the connection between the two movies
        ArrayList<ArrayList<String>> connectionGraph = movieInfoGetter.getConnection(movie1Name, movie1Doc, movie2Name, "Movie");

        // Instructions pop up
        JOptionPane.showMessageDialog(GUI.this, "Instructions:\nYou will select one question out of two having to do with the connection between two random movies\nand answer it. After answering your selected question, a graph will display the shortest connection\nbetween the movies, and you will be notified whether you are correct or not. Enjoy!", "Instructions", JOptionPane.INFORMATION_MESSAGE);

        // Create an array of all parties in the connection
        ArrayList<String> moviesAndActors = new ArrayList<>();
        for (ArrayList<String> innerList : connectionGraph) {
            moviesAndActors.add(innerList.get(0));
        }

        setTitle("Connection Graph Display");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Prompt user to select question they want to answer
        String[] options = {"What is the length of connection between " + movie1Name + " and " + movie2Name + "?", "Name one actor/movie in the connection between " + movie1Name + " and " + movie2Name + "?"};
        int choice = JOptionPane.showOptionDialog(null, "Which question would you like to answer?", "Select Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        String answer = "";
        if (choice != JOptionPane.CLOSED_OPTION) {
            answer = JOptionPane.showInputDialog(null, "What is your guess?:");
            if (answer == null) {
                // User canceled, exit the program
                System.exit(0);
            }
        } else {
            // User canceled, exit the program
            System.exit(0);
        }

        // Create panels for the boxes and arrows and the text below
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel boxesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));




        // Loop through the texts and create a JButton for each
        for (int i = 0; i < moviesAndActors.size(); i++) {
            final String currentText = moviesAndActors.get(i); // Declare final variable to hold the current text value

            JButton button = new JButton(currentText);
            button.setFocusPainted(false); // Remove the focus border
            button.setContentAreaFilled(false); // Remove the default background
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(), // Raised border
                    BorderFactory.createLoweredBevelBorder())); // Lowered border

            button.setPreferredSize(new Dimension(200, 100));

            boxesPanel.add(button);

            // Add arrow image between text boxes
            if (i < moviesAndActors.size() - 1) {
                ImageIcon arrowIcon = new ImageIcon("src/arrow.png"); // Change "arrow.png" to the path of your arrow image
                JLabel arrowLabel = new JLabel(arrowIcon);
                boxesPanel.add(arrowLabel);
            }
        }

        // Checks whether user's answer is correct
        String label;
        String coloredLabel;
        if (choice == 0) {
            if (Integer.parseInt(answer) == moviesAndActors.size() - 1) {
                label = "Correct! The connection between " + movie1Name +  " and " + movie2Name +  " is " + (moviesAndActors.size() - 1) + ".";
                coloredLabel = "<html><font color=\'green\'>" + label + "</font></html>";
            } else {
                label = "Wrong! The connection between " + movie1Name +  " and " + movie2Name +  " is " + (moviesAndActors.size() - 1) + ".";
                coloredLabel = "<html><font color=\'red\'>" + label + "</font></html>";
            }
        } else {
            if (moviesAndActors.contains(answer)) {
                label = "Correct! " + answer + " is in the connection between " + movie1Name +  " and " + movie2Name + ".";
                coloredLabel = "<html><font color=\'green\'>" + label + "</font></html>";
            } else {
                label = "Wrong! " + answer + " is not in the connection between " + movie1Name +  " and " + movie2Name + ".";
                coloredLabel = "<html><font color=\'red\'>" + label + "</font></html>";
            }
        }

        JLabel labelText = new JLabel(coloredLabel);
        labelPanel.add(labelText);

        mainPanel.add(boxesPanel, BorderLayout.CENTER);
        mainPanel.add(labelPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
    }
}
