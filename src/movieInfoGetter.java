import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class movieInfoGetter {

    /**
     * Given the doc of a wikipedia page of a movie, it returns the names and docs of every actor in its cast
     * @param inputMovieDoc the document of the wikipedia page of the movie
     * @return a hashmap with the name of each actor as the key and the document of their wikipedia page as the value
     */
    public static Map<String, Document> getActorsFromMovie(Document inputMovieDoc) {
        //maps the name of each actor to the document of their wikipedia page
        Map<String, Document> actors = new HashMap<>();
        try {
            //selects div with cast list, is the first div that contains ul after the "Cast" h2
            Element castDiv = inputMovieDoc.selectFirst("h2:has(span#Cast) ~ div.div-col:has(ul)");
            if (castDiv == null) {
                //for some wikipedia pages that don't store the cast list in a div
                castDiv = inputMovieDoc.selectFirst("h2:has(span#Cast) ~ ul");
            }
            if (castDiv != null) {
                //selects all li elements in the cast list
                Elements castItems = castDiv.select("li");
                for (Element item : castItems) {
                    //selects the first link in the li element that is followed by "as"
                    Pattern pattern = Pattern.compile("(<a.*?</a>) as");
                    Matcher matcher = pattern.matcher(item.toString());
                    if (matcher.find()) {
                        String actorLinkString = matcher.group(1);
                        //turns string back into Jsoup element
                        Element actorLink = Jsoup.parse(actorLinkString).selectFirst("a");

                        if (actorLink != null) {
                            //extracts actors name and url and adds them to the hashmap
                            Document actorDoc = null;
                            String actorName = actorLink.text();
                            String actorUrl = actorLink.attr("href");
                            try {
                                actorDoc = Jsoup.connect("https://en.wikipedia.org" + actorUrl + "_filmography").get();
                            } catch (IOException e1) {
                                try {
                                    actorDoc = Jsoup.connect("https://en.wikipedia.org" + actorUrl).get();
                                } catch (IOException e2) {
                                    continue;
                                }
                            }
                            actors.put(actorName, actorDoc);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return actors;
    }

    /**
     * Given the doc of a wikipedia page of an actor, it returns the names and docs of every movie they have been in
     * @param inputActorDoc the document of the wikipedia page of the actor
     * @return a hashmap with the name of each movie as the key and the document of their wikipedia page as the value
     */
    public static Map<String, Document> getMoviesFromActor(Document inputActorDoc) {
        //maps the name of each movie to the document of its wikipedia page
        Map<String, Document> movies = new HashMap<>();
        try {
            //selects the table of films from the filmography page
            Element filmsTable = inputActorDoc.selectFirst("h2:contains(Film) ~ table:contains(Title)");
            if (filmsTable == null) {
                //some wikipedia pages use an h3 instead of an h2
                filmsTable = inputActorDoc.selectFirst("h3:contains(Film) ~ table:contains(Title)");
            }
            if (filmsTable != null) {
                //selects all rows of the table of films
                Elements films = filmsTable.select("tr");
                for (Element film : films) {
                    //selects everything in the first column of each row, which is the links to all their movies
                    Element filmItem = film.selectFirst("td");
                    if (filmItem != null) {
                        //selects the link in each row
                        Element filmLink = filmItem.selectFirst("a");
                        if (filmLink != null) {
                            //extracts the movie name and its document and adds it to the hashmap
                            String filmTitle = filmLink.text();
                            String filmUrl = filmLink.attr("href");
                            Document newFilmDoc = Jsoup.connect("https://en.wikipedia.org" + filmUrl).get();
                            movies.put(filmTitle, newFilmDoc);
                        }
                    }
                }
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return movies;
    }

    /**
     * Uses BFS to find the shortest path between two movies
     * @param nameA Name of the first movie
     * @param docA Document of the wikipedia page of the first movie
     * @param nameB Name of the second movie
     * @param startType Type of the first movie (either "Actor" or "Movie")
     * @return The shortest path between the two movies
     */
    public static ArrayList<ArrayList<String>> getConnection(String nameA, Document docA, String nameB, String startType) {
        ArrayList<ArrayList<ArrayList<String>>> q = new ArrayList<>();
        Set<String> discovered = new HashSet<>();
        Map<String, Document> documents = new HashMap<>();

        //Adds first node to the queue
        ArrayList<String> startNode = new ArrayList<>();
        startNode.add(nameA);
        startNode.add(startType);
        ArrayList<ArrayList<String>> startPath = new ArrayList<>();
        startPath.add(startNode);

        q.add(startPath);
        documents.put(nameA, docA);

        //performs bfs to find the shortest path between the two movies
        while (!q.isEmpty()) {
            //gets the next node in the queue
            ArrayList<ArrayList<String>> path = q.get(0);
            q.remove(0);
            ArrayList<String> currNode = path.get(path.size() - 1);
            String currName = currNode.get(0);
            String currType = currNode.get(1);
            //if it is the movie we are looking for, it returns the path
            if (currName.equals(nameB)) {
                return path;
            } else if (!discovered.contains(currName)) {
                discovered.add(currName);
                Document currDoc = documents.get(currName);
                //if it is an actor, it gets all the movies they have been in, if it is a movie, get all the actors
                //in the movie and add them to the queue
                if (currType.equals("Actor")) {
                    System.out.println(currName);

                    Map<String, Document> connections = getMoviesFromActor(currDoc);
                    for (Map.Entry<String, Document> connection : connections.entrySet()) {
                        String name = connection.getKey();
                        Document doc = connection.getValue();
                        if (!discovered.contains(name)) {
                            documents.put(name, doc);
                            ArrayList<ArrayList<String>> newPath = new ArrayList<>(path);
                            ArrayList<String> newNode = new ArrayList<>();
                            newNode.add(name);
                            newNode.add("Movie");
                            newPath.add(newNode);
                            q.add(newPath);
                        }
                    }
                } else if (currType.equals("Movie")) {
                    System.out.println(currName);

                    Map<String, Document> connections = getActorsFromMovie(currDoc);
                    for (Map.Entry<String, Document> connection : connections.entrySet()) {
                        String name = connection.getKey();
                        Document doc = connection.getValue();
                        if (!discovered.contains(name)) {
                            documents.put(name, doc);
                            ArrayList<ArrayList<String>> newPath = new ArrayList<>(path);
                            ArrayList<String> newNode = new ArrayList<>();
                            newNode.add(name);
                            newNode.add("Actor");
                            newPath.add(newNode);
                            q.add(newPath);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a random movie from the wikipedia page of the most popular movies from the past 4 years
     * @return a map entry with the name of the movie as the key and the document of the wikipedia page as the value
     */
    public static Map.Entry<String, Document> getRandomMovie() {
        HashMap<String, String> movies = new HashMap<>();
        try {
            //goes through wikipedia pages from the past 4 years and gets all movies
            for (int i = 1; i < 5; i++) {
                Document moviesDoc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_202"
                        + i + "_box_office_number-one_films_in_the_United_States").get();
                Element movieTable = moviesDoc.selectFirst("tbody");
                Elements rows = movieTable.select("tr");
                for (Element row : rows) {
                    Element movieLink = row.selectFirst("a");
                    if (movieLink != null) {
                        String movieTitle = movieLink.text();
                        String movieUrl = movieLink.attr("href");
                        movies.put(movieTitle, "https://en.wikipedia.org" + movieUrl);
                    }
                }
            }
            //selects a random movie from the hashmap
            int rand = new Random().nextInt(movies.size());
            Map.Entry<String, String>[] entries = movies.entrySet().toArray(new Map.Entry[0]);
            Map.Entry<String, String> randEntry = entries[rand];

            String movie = randEntry.getKey();
            Document movieDoc = Jsoup.connect(randEntry.getValue()).get();
            return new AbstractMap.SimpleEntry<>(movie, movieDoc);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}