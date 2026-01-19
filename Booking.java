import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    public String bookingId;
    public String customerName;
    public String movieTitle;
    public String showtimeId;
    public List<String> seatNumbers;
    public LocalDateTime bookingTime;

    // Constructor
    public Booking(String bookingId, String customerName, String movieTitle, String showtimeId, List<String> seatNumbers, LocalDateTime bookingTime) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.movieTitle = movieTitle;
        this.showtimeId = showtimeId;
        this.seatNumbers = new ArrayList<>(seatNumbers);
        this.bookingTime = bookingTime;
    }
    
    @Override
    public String toString() {
        return "Booking ID: " + bookingId + 
               " | Customer: " + customerName + 
               " | Movie: " + movieTitle + 
               " | Showtime: " + showtimeId + 
               " | Seats: " + String.join(", ", seatNumbers) + 
               " | Time: " + bookingTime;
    }
}
