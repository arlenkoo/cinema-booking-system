import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CinemaBookingSystem {
    private List<Movie> movies;
    private List<Booking> bookings;
    private List<User> users;
    private User currentUser;
    private Scanner scanner;
    
    // Simple ID counters
    private int movieCounter = 0;
    private int showtimeCounter = 0;
    private int bookingCounter = 0;
    private int userCounter = 0;
    
    // File paths
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String MOVIES_FILE = "movies.txt";
    private static final String USERS_FILE = "users.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor
    public CinemaBookingSystem() {
        this.movies = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.users = new ArrayList<>();
        this.currentUser = null;
        this.scanner = new Scanner(System.in);
    }

    // Initialize the system
    public void initialize() {
        System.out.println("========================================");
        System.out.println("   Cinema Ticket Booking System");
        System.out.println("========================================\n");

        // Load existing data
        loadMovies();
        loadBookings();
        loadUsers();

        // If no movies exist, create sample data
        if (movies.isEmpty()) {
            System.out.println("Creating sample data...\n");
            createSampleData();
        } else {
            // Initialize counters from existing data
            initializeCountersFromData();
        }
    }
    
    // Initialize counters based on existing data
    private void initializeCountersFromData() {
        int maxMovieCount = 0;
        int maxShowtimeCount = 0;
        int maxBookingCount = 0;
        
        // Parse movie IDs to find the maximum counter value
        for (Movie movie : movies) {
            String movieId = movie.movieId;
            if (movieId.startsWith("M")) {
                try {
                    int count = Integer.parseInt(movieId.substring(1));
                    maxMovieCount = Math.max(maxMovieCount, count);
                } catch (NumberFormatException e) {
                    // Skip if parsing fails
                }
            }
            
            // Parse showtime IDs
            for (Showtime showtime : movie.showtimes) {
                String showtimeId = showtime.showtimeId;
                if (showtimeId.startsWith("S")) {
                    try {
                        int count = Integer.parseInt(showtimeId.substring(1));
                        maxShowtimeCount = Math.max(maxShowtimeCount, count);
                    } catch (NumberFormatException e) {
                        // Skip if parsing fails
                    }
                }
            }
        }
        
        // Parse booking IDs
        for (Booking booking : bookings) {
            String bookingId = booking.bookingId;
            if (bookingId.startsWith("B")) {
                try {
                    int count = Integer.parseInt(bookingId.substring(1));
                    maxBookingCount = Math.max(maxBookingCount, count);
                } catch (NumberFormatException e) {
                    // Skip if parsing fails
                }
            }
        }
        
        // Parse user IDs
        int maxUserCount = 0;
        for (User user : users) {
            String userId = user.userId;
            if (userId.startsWith("U")) {
                try {
                    int count = Integer.parseInt(userId.substring(1));
                    maxUserCount = Math.max(maxUserCount, count);
                } catch (NumberFormatException e) {
                    // Skip if parsing fails
                }
            }
        }
        
        // Set counters to the maximum values found
        movieCounter = maxMovieCount;
        showtimeCounter = maxShowtimeCount;
        bookingCounter = maxBookingCount;
        userCounter = maxUserCount;
    }

    // Create sample data for testing
    private void createSampleData() {
        // Create movies
        Movie movie1 = new Movie(generateMovieId(), "Inception", 148);
        Movie movie2 = new Movie(generateMovieId(), "The Dark Knight", 152);
        Movie movie3 = new Movie(generateMovieId(), "Interstellar", 169);

        // Create showtimes for movie1
        Showtime showtime1 = new Showtime(
            generateShowtimeId(),
            LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
            20
        );
        
        Showtime showtime2 = new Showtime(
            generateShowtimeId(),
            LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
            20
        );

        // Add seats to showtimes
        for (int i = 1; i <= 20; i++) {
            showtime1.seats.add(new Seat("A" + i));
            showtime2.seats.add(new Seat("A" + i));
        }

        movie1.showtimes.add(showtime1);
        movie1.showtimes.add(showtime2);

        // Create showtimes for movie2
        Showtime showtime3 = new Showtime(
            generateShowtimeId(),
            LocalDateTime.now().plusDays(1).withHour(17).withMinute(30),
            20
        );
        
        Showtime showtime3b = new Showtime(
            generateShowtimeId(),
            LocalDateTime.now().plusDays(2).withHour(15).withMinute(0),
            20
        );

        for (int i = 1; i <= 20; i++) {
            showtime3.seats.add(new Seat("B" + i));
            showtime3b.seats.add(new Seat("B" + i));
        }

        movie2.showtimes.add(showtime3);
        movie2.showtimes.add(showtime3b);

        // Create showtimes for movie3
        Showtime showtime4 = new Showtime(
            generateShowtimeId(),
            LocalDateTime.now().plusDays(3).withHour(19).withMinute(0),
            20
        );

        for (int i = 1; i <= 20; i++) {
            showtime4.seats.add(new Seat("C" + i));
        }

        movie3.showtimes.add(showtime4);

        // Add movies to list
        movies.add(movie1);
        movies.add(movie2);
        movies.add(movie3);

        // Save to storage
        saveMovies();
    }
    
    // Simple ID generation methods
    private String generateMovieId() {
        movieCounter++;
        return "M" + movieCounter;
    }
    
    private String generateShowtimeId() {
        showtimeCounter++;
        return "S" + showtimeCounter;
    }
    
    private String generateBookingId() {
        bookingCounter++;
        return "B" + bookingCounter;
    }
    
    private String generateUserId() {
        userCounter++;
        return "U" + userCounter;
    }

    // Main menu
    public void showMainMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("           Main Menu");
            System.out.println("========================================");
            
            // Check if current user is admin
            boolean isAdmin = (currentUser != null && currentUser.role.equals("ADMIN"));
            
            if (isAdmin) {
                // Admin menu with all admin options displayed
                System.out.println("1. View Movies");
                System.out.println("2. View All Bookings");
                System.out.println("3. View Statistics");
                System.out.println("4. Add New Movie");
                System.out.println("5. Add New Showtime");
                System.out.println("6. Remove Movie");
                System.out.println("7. Remove Showtime");
                System.out.println("8. Clear All Data");
                System.out.println("9. Exit");
            } else {
                // Customer menu
                System.out.println("1. View Movies");
                System.out.println("2. Book Tickets");
                System.out.println("3. View My Bookings");
                System.out.println("4. Cancel Booking");
                System.out.println("5. Exit");
            }
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                if (isAdmin) {
                    // Admin menu choices
                    switch (choice) {
                        case 1:
                            viewMovies();
                            break;
                        case 2:
                            viewAllBookings();
                            break;
                        case 3:
                            viewStatistics();
                            break;
                        case 4:
                            addNewMovie();
                            break;
                        case 5:
                            addNewShowtime();
                            break;
                        case 6:
                            removeMovie();
                            break;
                        case 7:
                            removeShowtime();
                            break;
                        case 8:
                            clearAllData();
                            break;
                        case 9:
                            exit();
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } else {
                    // Customer menu choices
                    switch (choice) {
                        case 1:
                            viewMovies();
                            break;
                        case 2:
                            bookTickets();
                            break;
                        case 3:
                            viewBookings();
                            break;
                        case 4:
                            cancelBooking();
                            break;
                        case 5:
                            exit();
                            return;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // View all movies and showtimes
    private void viewMovies() {
        System.out.println("\n========================================");
        System.out.println("         Available Movies");
        System.out.println("========================================");

        if (movies.isEmpty()) {
            System.out.println("No movies available.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            System.out.println("\n" + (i + 1) + ". " + movie.title);
            System.out.println("   Duration: " + movie.duration + " minutes");
            System.out.println("   Movie ID: " + movie.movieId);
            System.out.println("   Showtimes:");

            List<Showtime> showtimes = movie.showtimes;
            for (int j = 0; j < showtimes.size(); j++) {
                Showtime showtime = showtimes.get(j);
                System.out.println("   " + (j + 1) + ". " + showtime.dateTime.format(formatter) +
                    " (Available: " + showtime.getAvailableSeats().size() + "/" + showtime.totalSeats + ")");
            }
        }
    }

    // Book tickets
    private void bookTickets() {
        viewMovies();

        if (movies.isEmpty()) {
            return;
        }

        System.out.print("\nEnter movie number to book: ");
        try {
            int movieIndex = Integer.parseInt(scanner.nextLine()) - 1;

            if (movieIndex < 0 || movieIndex >= movies.size()) {
                System.out.println("Invalid movie selection.");
                return;
            }

            Movie selectedMovie = movies.get(movieIndex);
            List<Showtime> showtimes = selectedMovie.showtimes;

            if (showtimes.isEmpty()) {
                System.out.println("No showtimes available for this movie.");
                return;
            }

            System.out.print("Enter showtime number: ");
            int showtimeIndex = Integer.parseInt(scanner.nextLine()) - 1;

            if (showtimeIndex < 0 || showtimeIndex >= showtimes.size()) {
                System.out.println("Invalid showtime selection.");
                return;
            }

            Showtime selectedShowtime = showtimes.get(showtimeIndex);

            // Display available seats
            System.out.println("\nAvailable Seats:");
            List<Seat> availableSeats = selectedShowtime.getAvailableSeats();

            if (availableSeats.isEmpty()) {
                System.out.println("No seats available for this showtime.");
                return;
            }

            for (int i = 0; i < availableSeats.size(); i++) {
                System.out.print(availableSeats.get(i).seatNumber + " ");
                if ((i + 1) % 10 == 0) {
                    System.out.println();
                }
            }

            System.out.print("\n\nEnter number of seats to book: ");
            int numSeats = Integer.parseInt(scanner.nextLine());

            if (numSeats <= 0 || numSeats > availableSeats.size()) {
                System.out.println("Invalid number of seats.");
                return;
            }

            List<String> bookedSeats = new ArrayList<>();

            for (int i = 0; i < numSeats; i++) {
                System.out.print("Enter seat number " + (i + 1) + ": ");
                String seatNumber = scanner.nextLine().toUpperCase();

                if (!selectedShowtime.isSeatAvailable(seatNumber)) {
                    System.out.println("Seat " + seatNumber + " is not available.");
                    return;
                }

                try {
                    selectedShowtime.reserveSeat(seatNumber);
                    bookedSeats.add(seatNumber);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }
            }

            // Create booking
            Booking booking = new Booking(
                generateBookingId(),
                currentUser.name,
                selectedMovie.title,
                selectedShowtime.showtimeId,
                bookedSeats,
                LocalDateTime.now()
            );

            bookings.add(booking);

            // Print ticket
            printTicket(booking);

            // Save data
            saveMovies();
            saveBookings();

            System.out.println("Booking successful!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // View bookings
    private void viewBookings() {
        System.out.println("\n========================================");
        System.out.println("         Your Bookings");
        System.out.println("========================================");

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        List<Booking> userBookings = getUserBookings();

        if (userBookings.isEmpty()) {
            System.out.println("You have no bookings.");
            return;
        }

        for (int i = 0; i < userBookings.size(); i++) {
            Booking booking = userBookings.get(i);
            System.out.println("\n");
            printTicket(booking, i + 1);
        }
    }
    
    // Print ticket for display - consolidated method
    private void printTicket(Booking booking) {
        printTicket(booking, null);
    }
    
    private void printTicket(Booking booking, Integer number) {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("========================================");
        if (number != null) {
            System.out.println("         CINEMA TICKET " + number);
        } else {
            System.out.println("         CINEMA TICKET");
        }
        System.out.println("========================================");
        System.out.println("Booking ID: " + booking.bookingId);
        System.out.println("Customer: " + booking.customerName);
        System.out.println("Movie: " + booking.movieTitle);
        System.out.println("Showtime ID: " + booking.showtimeId);
        System.out.println("Seat(s): " + String.join(", ", booking.seatNumbers));
        System.out.println("Booking Time: " + booking.bookingTime.format(displayFormatter));
        System.out.println("Total Seats: " + booking.seatNumbers.size());
        System.out.println("========================================");
        System.out.println("    Thank you for your booking!");
        System.out.println("========================================");
    }

    // Cancel booking
    private void cancelBooking() {
        viewBookings();

        if (bookings.isEmpty()) {
            return;
        }

        // Get user bookings
        List<Booking> userBookings = getUserBookings();
        if (userBookings.isEmpty()) {
            return;
        }

        System.out.print("\nEnter booking number to cancel: ");
        try {
            int bookingIndex = Integer.parseInt(scanner.nextLine()) - 1;

            if (bookingIndex < 0 || bookingIndex >= userBookings.size()) {
                System.out.println("Invalid booking selection.");
                return;
            }

            Booking booking = userBookings.get(bookingIndex);

            // Release seats back to showtime
            releaseSeatBooking(booking);

            // Remove booking
            bookings.remove(booking);

            // Save data
            saveMovies();
            saveBookings();

            System.out.println("Booking cancelled successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    // Helper method to get user bookings
    private List<Booking> getUserBookings() {
        List<Booking> userBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.customerName.equals(currentUser.name)) {
                userBookings.add(booking);
            }
        }
        return userBookings;
    }
    
    // Helper method to release seat booking
    private void releaseSeatBooking(Booking booking) {
        for (Movie movie : movies) {
            if (movie.title.equals(booking.movieTitle)) {
                for (Showtime showtime : movie.showtimes) {
                    if (showtime.showtimeId.equals(booking.showtimeId)) {
                        for (String seatNumber : booking.seatNumbers) {
                            try {
                                showtime.cancelSeat(seatNumber);
                            } catch (Exception e) {
                                System.out.println("Warning: " + e.getMessage());
                            }
                        }
                        return;
                    }
                }
            }
        }
    }
    
    // Helper method to validate user input integer in range
    private Integer getValidIntegerInput(String prompt, int min, int max) {
        System.out.print(prompt);
        try {
            int value = Integer.parseInt(scanner.nextLine());
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
        return null;
    }
    
    // Helper method to confirm action
    private boolean confirmAction(String message) {
        System.out.print(message + " (yes/no): ");
        String confirm = scanner.nextLine().trim();
        return confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y");
    }
    
    // Helper method to validate non-empty string input
    private String getNonEmptyInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return null;
        }
        return input;
    }
    
    // Helper method to find showtime by ID
    private Showtime findShowtime(String showtimeId) {
        for (Movie movie : movies) {
            for (Showtime showtime : movie.showtimes) {
                if (showtime.showtimeId.equals(showtimeId)) {
                    return showtime;
                }
            }
        }
        return null;
    }

    private void viewAllBookings() {
        System.out.println("\n========================================");
        System.out.println("         All Bookings");
        System.out.println("========================================");

        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Booking booking : bookings) {
            System.out.println(booking);
        }
    }

    private void viewStatistics() {
        System.out.println("\n========================================");
        System.out.println("          Statistics");
        System.out.println("========================================");
        System.out.println("Total Movies: " + movies.size());
        System.out.println("Total Bookings: " + bookings.size());

        int totalSeatsBooked = 0;
        for (Booking booking : bookings) {
            totalSeatsBooked += booking.seatNumbers.size();
        }
        System.out.println("Total Seats Booked: " + totalSeatsBooked);

        int totalShowtimes = 0;
        for (Movie movie : movies) {
            totalShowtimes += movie.showtimes.size();
        }
        System.out.println("Total Showtimes: " + totalShowtimes);
    }

    private void addNewMovie() {
        System.out.println("\n========================================");
        System.out.println("         Add New Movie");
        System.out.println("========================================");
        
        String title = getNonEmptyInput("Enter movie title: ");
        if (title == null) {
            return;
        }
        
        System.out.print("Enter movie duration (in minutes): ");
        try {
            int duration = Integer.parseInt(scanner.nextLine());
            
            if (duration <= 0) {
                System.out.println("Duration must be a positive number.");
                return;
            }
            
            // Create new movie
            Movie newMovie = new Movie(generateMovieId(), title, duration);
            movies.add(newMovie);
            
            // Save to file
            saveMovies();
            
            System.out.println("\nMovie added successfully!");
            System.out.println("Movie ID: " + newMovie.movieId);
            System.out.println("Title: " + newMovie.title);
            System.out.println("Duration: " + newMovie.duration + " minutes");
            
            // Ask if user wants to add showtimes immediately
            if (confirmAction("Would you like to add showtimes for this movie now?")) {
                addShowtimesToMovie(newMovie);
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration format. Please enter a valid number.");
        }
    }
    
    private void addNewShowtime() {
        System.out.println("\n========================================");
        System.out.println("         Add New Showtime");
        System.out.println("========================================");
        
        if (movies.isEmpty()) {
            System.out.println("No movies available. Please add a movie first.");
            return;
        }
        
        // Display available movies
        System.out.println("\nAvailable Movies:");
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            System.out.println((i + 1) + ". " + movie.title + " (ID: " + movie.movieId + ")");
        }
        
        System.out.print("\nEnter movie number to add showtime: ");
        try {
            int movieIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (movieIndex < 0 || movieIndex >= movies.size()) {
                System.out.println("Invalid movie selection.");
                return;
            }
            
            Movie selectedMovie = movies.get(movieIndex);
            addShowtimesToMovie(selectedMovie);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
    
    private void addShowtimesToMovie(Movie movie) {
        System.out.println("\nAdding showtime for: " + movie.title);
        
        String dateTimeInput = getNonEmptyInput("Enter date and time (yyyy-MM-dd HH:mm): ");
        if (dateTimeInput == null) {
            return;
        }
        
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeInput, inputFormatter);
            
            // Check if the showtime is in the future
            if (dateTime.isBefore(LocalDateTime.now())) {
                System.out.println("Showtime must be in the future.");
                return;
            }
            
            System.out.print("Enter total number of seats: ");
            int totalSeats = Integer.parseInt(scanner.nextLine());
            
            if (totalSeats <= 0 || totalSeats > 100) {
                System.out.println("Number of seats must be between 1 and 100.");
                return;
            }
            
            // Create new showtime
            Showtime newShowtime = new Showtime(generateShowtimeId(), dateTime, totalSeats);
            
            // Add seats to the showtime
            System.out.print("Enter seat prefix (e.g., A, B, C) [default: S]: ");
            String seatPrefix = scanner.nextLine().toUpperCase().trim();
            
            if (seatPrefix.isEmpty()) {
                seatPrefix = "S";
            }
            
            for (int i = 1; i <= totalSeats; i++) {
                newShowtime.seats.add(new Seat(seatPrefix + i));
            }
            
            // Add showtime to movie
            movie.showtimes.add(newShowtime);
            
            // Save to file
            saveMovies();
            
            System.out.println("\nShowtime added successfully!");
            System.out.println("Showtime ID: " + newShowtime.showtimeId);
            System.out.println("Date & Time: " + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            System.out.println("Total Seats: " + totalSeats);
            System.out.println("Seats: " + seatPrefix + "1 to " + seatPrefix + totalSeats);
            
        } catch (Exception e) {
            System.out.println("Invalid input. Please use yyyy-MM-dd HH:mm format for date and time.");
        }
    }
    
    private void removeMovie() {
        System.out.println("\n========================================");
        System.out.println("         Remove Movie");
        System.out.println("========================================");
        
        if (movies.isEmpty()) {
            System.out.println("No movies available to remove.");
            return;
        }
        
        // Display available movies
        System.out.println("\nAvailable Movies:");
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            System.out.println((i + 1) + ". " + movie.title + " (ID: " + movie.movieId + ")");
            System.out.println("   Duration: " + movie.duration + " minutes");
            System.out.println("   Showtimes: " + movie.showtimes.size());
        }
        
        System.out.print("\nEnter movie number to remove (0 to cancel): ");
        try {
            int movieIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (movieIndex == -1) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            if (movieIndex < 0 || movieIndex >= movies.size()) {
                System.out.println("Invalid movie selection.");
                return;
            }
            
            Movie selectedMovie = movies.get(movieIndex);
            
            // Confirm deletion
            if (!confirmAction("Are you sure you want to remove '" + selectedMovie.title + "'?")) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            // Remove all bookings related to this movie
            List<Booking> bookingsToRemove = new ArrayList<>();
            for (Booking booking : bookings) {
                if (booking.movieTitle.equals(selectedMovie.title)) {
                    bookingsToRemove.add(booking);
                }
            }
            
            for (Booking booking : bookingsToRemove) {
                bookings.remove(booking);
            }
            
            // Remove the movie
            movies.remove(movieIndex);
            
            // Save data
            saveMovies();
            saveBookings();
            
            System.out.println("\nMovie '" + selectedMovie.title + "' removed successfully!");
            if (!bookingsToRemove.isEmpty()) {
                System.out.println(bookingsToRemove.size() + " related booking(s) were also removed.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
    
    private void removeShowtime() {
        System.out.println("\n========================================");
        System.out.println("         Remove Showtime");
        System.out.println("========================================");
        
        if (movies.isEmpty()) {
            System.out.println("No movies available.");
            return;
        }
        
        // Display available movies
        System.out.println("\nAvailable Movies:");
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            System.out.println((i + 1) + ". " + movie.title + " (ID: " + movie.movieId + ")");
        }
        
        System.out.print("\nEnter movie number (0 to cancel): ");
        try {
            int movieIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (movieIndex == -1) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            if (movieIndex < 0 || movieIndex >= movies.size()) {
                System.out.println("Invalid movie selection.");
                return;
            }
            
            Movie selectedMovie = movies.get(movieIndex);
            
            if (selectedMovie.showtimes.isEmpty()) {
                System.out.println("No showtimes available for this movie.");
                return;
            }
            
            // Display available showtimes
            System.out.println("\nShowtimes for " + selectedMovie.title + ":");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (int i = 0; i < selectedMovie.showtimes.size(); i++) {
                Showtime showtime = selectedMovie.showtimes.get(i);
                int bookedSeats = 0;
                for (Seat seat : showtime.seats) {
                    if (seat.isBooked) {
                        bookedSeats++;
                    }
                }
                System.out.println((i + 1) + ". " + showtime.dateTime.format(formatter) + 
                    " (ID: " + showtime.showtimeId + ")");
                System.out.println("   Seats: " + bookedSeats + "/" + showtime.totalSeats + " booked");
            }
            
            System.out.print("\nEnter showtime number to remove (0 to cancel): ");
            int showtimeIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (showtimeIndex == -1) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            if (showtimeIndex < 0 || showtimeIndex >= selectedMovie.showtimes.size()) {
                System.out.println("Invalid showtime selection.");
                return;
            }
            
            Showtime selectedShowtime = selectedMovie.showtimes.get(showtimeIndex);
            
            // Confirm deletion
            if (!confirmAction("Are you sure you want to remove this showtime?")) {
                System.out.println("Operation cancelled.");
                return;
            }
            
            // Remove all bookings related to this showtime
            List<Booking> bookingsToRemove = new ArrayList<>();
            for (Booking booking : bookings) {
                if (booking.showtimeId.equals(selectedShowtime.showtimeId)) {
                    bookingsToRemove.add(booking);
                }
            }
            
            for (Booking booking : bookingsToRemove) {
                bookings.remove(booking);
            }
            
            // Remove the showtime
            selectedMovie.showtimes.remove(showtimeIndex);
            
            // Save data
            saveMovies();
            saveBookings();
            
            System.out.println("\nShowtime removed successfully!");
            if (!bookingsToRemove.isEmpty()) {
                System.out.println(bookingsToRemove.size() + " related booking(s) were also removed.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
    
    private void clearAllData() {
        if (!confirmAction("Are you sure you want to clear all data?")) {
            System.out.println("Operation cancelled.");
            return;
        }

        // Clear all data files
        File bookingsFile = new File(BOOKINGS_FILE);
        File moviesFile = new File(MOVIES_FILE);

        if (bookingsFile.exists()) {
            bookingsFile.delete();
            System.out.println("Bookings data cleared.");
        }

        if (moviesFile.exists()) {
            moviesFile.delete();
            System.out.println("Movies data cleared.");
        }
        
        movies.clear();
        bookings.clear();
        System.out.println("All data cleared. Creating new sample data...");
        createSampleData();
    }
    
    // Storage methods - save and load data
    private void saveBookings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking booking : bookings) {
                writer.write(booking.bookingId + "|");
                writer.write(booking.customerName + "|");
                writer.write(booking.movieTitle + "|");
                writer.write(booking.showtimeId + "|");
                writer.write(String.join(",", booking.seatNumbers) + "|");
                writer.write(booking.bookingTime.format(formatter));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    private void loadBookings() {
        File file = new File(BOOKINGS_FILE);
        
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String bookingId = parts[0];
                    String customerName = parts[1];
                    String movieTitle = parts[2];
                    String showtimeId = parts[3];
                    
                    List<String> seatNumbers = new ArrayList<>();
                    if (!parts[4].isEmpty()) {
                        String[] seats = parts[4].split(",");
                        for (String seat : seats) {
                            seatNumbers.add(seat);
                        }
                    }
                    
                    LocalDateTime bookingTime = LocalDateTime.parse(parts[5], formatter);
                    bookings.add(new Booking(bookingId, customerName, movieTitle, showtimeId, seatNumbers, bookingTime));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }

    private void saveMovies() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MOVIES_FILE))) {
            for (Movie movie : movies) {
                writer.write("MOVIE|" + movie.movieId + "|" + movie.title + "|" + movie.duration);
                writer.newLine();
                
                // Save showtimes for this movie
                for (Showtime showtime : movie.showtimes) {
                    writer.write("SHOWTIME|" + showtime.showtimeId + "|" + 
                                showtime.dateTime.format(formatter) + "|" + 
                                showtime.totalSeats);
                    writer.newLine();
                    
                    // Save seats for this showtime
                    for (Seat seat : showtime.seats) {
                        writer.write("SEAT|" + seat.seatNumber + "|" + seat.isBooked);
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving movies: " + e.getMessage());
        }
    }

    private void loadMovies() {
        File file = new File(MOVIES_FILE);
        
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(MOVIES_FILE))) {
            String line;
            Movie currentMovie = null;
            Showtime currentShowtime = null;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                
                if (parts[0].equals("MOVIE") && parts.length == 4) {
                    // Save previous movie and showtime if they exist
                    if (currentShowtime != null && currentMovie != null) {
                        currentMovie.showtimes.add(currentShowtime);
                    }
                    if (currentMovie != null) {
                        movies.add(currentMovie);
                    }
                    
                    currentMovie = new Movie(parts[1], parts[2], Integer.parseInt(parts[3]));
                    currentShowtime = null;
                    
                } else if (parts[0].equals("SHOWTIME") && parts.length == 4 && currentMovie != null) {
                    // Save previous showtime if it exists
                    if (currentShowtime != null) {
                        currentMovie.showtimes.add(currentShowtime);
                    }
                    
                    LocalDateTime dateTime = LocalDateTime.parse(parts[2], formatter);
                    currentShowtime = new Showtime(parts[1], dateTime, Integer.parseInt(parts[3]));
                    
                } else if (parts[0].equals("SEAT") && parts.length == 3 && currentShowtime != null) {
                    currentShowtime.seats.add(new Seat(parts[1], Boolean.parseBoolean(parts[2])));
                }
            }
            
            // Add final showtime and movie
            if (currentShowtime != null && currentMovie != null) {
                currentMovie.showtimes.add(currentShowtime);
            }
            if (currentMovie != null) {
                movies.add(currentMovie);
            }
            
        } catch (IOException e) {
            System.err.println("Error loading movies: " + e.getMessage());
        }
    }

    // Exit the system
    private void exit() {
        System.out.println("\n========================================");
        System.out.println("Thank you for using Cinema Booking System!");
        System.out.println("========================================");
        scanner.close();
    }

    // Authentication menu
    public void showAuthMenu() {
        while (currentUser == null) {
            System.out.println("\n========================================");
            System.out.println("        Welcome to Cinema Booking");
            System.out.println("========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        login();
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        System.out.println("\nThank you for visiting. Goodbye!");
                        scanner.close();
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Login method
    private void login() {
        System.out.println("\n========================================");
        System.out.println("              Login");
        System.out.println("========================================");
        System.out.println("Login as:");
        System.out.println("1. Customer");
        System.out.println("2. Admin");
        System.out.print("Select user type (1-2): ");
        String choice = scanner.nextLine().trim();
        
        String userType = "";
        if (choice.equals("1")) {
            userType = "CUSTOMER";
        } else if (choice.equals("2")) {
            userType = "ADMIN";
        } else {
            System.out.println("\nInvalid choice. Login cancelled.");
            return;
        }
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.name.equals(username) && user.password.equals(password) && user.role.equals(userType)) {
                currentUser = user;
                System.out.println("\nLogin successful! Welcome back, " + username + "!");
                return;
            }
        }

        System.out.println("\nLogin failed. Invalid username, password, or user type.");
    }

    // Register method
    private void register() {
        System.out.println("\n========================================");
        System.out.println("            Registration");
        System.out.println("========================================");
        System.out.println("Register as:");
        System.out.println("1. Customer");
        System.out.println("2. Admin");
        System.out.print("Select user type (1-2): ");
        String choice = scanner.nextLine().trim();
        
        String userType = "";
        if (choice.equals("1")) {
            userType = "CUSTOMER";
        } else if (choice.equals("2")) {
            userType = "ADMIN";
        } else {
            System.out.println("\nInvalid choice. Registration cancelled.");
            return;
        }
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        // Check if username already exists
        for (User user : users) {
            if (user.name.equals(username)) {
                System.out.println("\nUsername already exists. Please choose a different username.");
                return;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("\nPasswords do not match. Registration failed.");
            return;
        }

        if (password.isEmpty()) {
            System.out.println("\nPassword cannot be empty. Registration failed.");
            return;
        }

        User newUser = new User(generateUserId(), username, password, userType, "", "");
        users.add(newUser);
        saveUsers();
        System.out.println("\nRegistration successful! You can now login.");
    }

    // Load users from file
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    User user = new User(parts[0], parts[1], parts[2], parts[3], "", "");
                    users.add(user);
                }
            }
        } catch (FileNotFoundException e) {
            // No users file yet - starting fresh
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    // Save users to file
    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.println(user.userId + "|" + user.name + "|" + user.password + "|" + user.role);
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // Getter methods for testing purposes
    public List<Movie> getMovies() {
        return movies;
    }
    
    public List<Booking> getBookings() {
        return bookings;
    }
    
    public List<User> getUsers() {
        return users;
    }

    // Main method to start the system
    public static void main(String[] args) {
        CinemaBookingSystem system = new CinemaBookingSystem();
        system.initialize();
        system.showAuthMenu();
        system.showMainMenu();
    }
}
