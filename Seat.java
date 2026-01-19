import java.io.Serializable;

public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;
    public String seatNumber;
    public boolean isBooked;

    // Constructor
    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
        this.isBooked = false;
    }

    // Constructor with booking status
    public Seat(String seatNumber, boolean isBooked) {
        this.seatNumber = seatNumber;
        this.isBooked = isBooked;
    }
    
    @Override
    public String toString() {
        return seatNumber + (isBooked ? "[X]" : "[ ]");
    }
}
