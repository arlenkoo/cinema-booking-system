import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    public String movieId;
    public String title;
    public int duration; // in minutes
    public List<Showtime> showtimes;

    // Constructor
    public Movie(String movieId, String title, int duration) {
        this.movieId = movieId;
        this.title = title;
        this.duration = duration;
        this.showtimes = new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return "Movie[" + movieId + ": " + title + " (" + duration + "min)]";
    }
}
