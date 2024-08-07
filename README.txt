Cinematic Connections

Project Description
Our project, Cinematic Connections, is a game that allows users to explore connections between popular movies.
Users are presented with two random movies and choose between two questions to answer. A BFS algorithm finds the
shortest connection between the given movies, visualized in a graph. Feedback is provided below the graph, informing
users whether their answers are correct or incorrect.

Categories Used (Graph and Graph Algorithms, Information Networks)
Firstly, we used JSoup to scrape data about popular actors and movies from Wikipedia.
Each actor and movie is a node in the graph, while the relationships between are the edges.
Secondly, we implemented a Breadth-First Search (BFS) algorithm that enabled the determination of the
shortest path between any two given movies. Finally, by integrating a Java GUI to display the connection graph,
we transformed the graph data into a visual representation.

Notes about running program:
- We added 4 options to test our program. The first 3 options finds the connection between movies that have a simple
connection. The last option find the connection between two random movies.
- The last option can be very slow, as each extra level in the path exponentially increases the time that it takes.
It takes close to a second to get the document for each actor/movie page, which adds up a lot over thousands of actors/movies
that it has to check. While it performs well for movies that are connected by 1 or two actors, any long than that can take an
exceptionally long time to run.
- If you look in the terminal while running the program, it prints the name of each actor/movie that it checks, so you can see that
it is working.
- Wikipedia does not format each page the same way, so there are instances where the information about an actor
or movie cannot be retrieved correctly. This sometimes leads to the program returning a path between two movies
that is not actually the shortest path if one of the actors/movies along the true shortest path has missing information.
- Also note that sometimes there are multiple valid shortest paths between two movies, and the program only returns the first
one that it finds