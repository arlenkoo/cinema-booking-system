import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Showtime implements Serializable {
    private static final long serialVersionUID = 1L;
    public String showtimeId;
    public LocalDateTime dateTime;
    public int totalSeats;
    public List<Seat> seats;

    // Constructor
    public Showtime(String showtimeId, LocalDateTime dateTime, int totalSeats) {
        this.showtimeId = showtimeId;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.seats = new ArrayList<>();
    }

    // Simple method to get available seats - no streams
    public List<Seat> getAvailableSeats() {
        List<Seat> availableSeats = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            if (!seat.isBooked) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }

    // Check if a specific seat is available
    public boolean isSeatAvailable(String seatNumber) {
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            if (seat.seatNumber.equals(seatNumber)) {
                return !seat.isBooked;
            }
        }
        return false;
    }

    // Reserve a seat - simplified
    public Seat reserveSeat(String seatNumber) {
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            if (seat.seatNumber.equals(seatNumber)) {
                if (!seat.isBooked) {
                    seat.isBooked = true;
                    return seat;
                } else {
                    throw new IllegalStateException("Seat " + seatNumber + " is already reserved");
                }
            }
        }
        throw new IllegalArgumentException("Seat " + seatNumber + " not found");
    }

    // Cancel a seat reservation - simplified
    public void cancelSeat(String seatNumber) {
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            if (seat.seatNumber.equals(seatNumber)) {
                if (seat.isBooked) {
                    seat.isBooked = false;
                    return;
                } else {
                    throw new IllegalStateException("Seat " + seatNumber + " is not reserved");
                }
            }
        }
        throw new IllegalArgumentException("Seat " + seatNumber + " not found");
    }
    
    // Count booked seats
    public int getBookedSeatsCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isBooked) {
                count++;
            }
        }
        return count;
    }
}
