import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Reliability Testing Suite for Cinema Booking System
 * Measures: Failure Rate, Transaction Success Rate, Error Recovery Rate
 */
public class ReliabilityTest {
    
    // Metrics counters
    private static AtomicInteger totalTransactions = new AtomicInteger(0);
    private static AtomicInteger successfulTransactions = new AtomicInteger(0);
    private static AtomicInteger failedTransactions = new AtomicInteger(0);
    private static AtomicInteger totalErrors = new AtomicInteger(0);
    private static AtomicInteger recoveredErrors = new AtomicInteger(0);
    private static AtomicInteger dataCorruptions = new AtomicInteger(0);
    private static AtomicInteger doubleBookings = new AtomicInteger(0);
    
    private static long testStartTime;
    
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("    CINEMA BOOKING SYSTEM - RELIABILITY TEST SUITE");
        System.out.println("================================================================\n");
        
        testStartTime = System.currentTimeMillis();
        
        // Run all tests
        test1_SequentialBookings();
        test2_ConcurrentBookings();
        test3_ErrorRecovery();
        test4_DataIntegrity();
        
        // Generate final report
        generateReport();
    }
    
    // ==================== TEST 1: Sequential Bookings ====================
    private static void test1_SequentialBookings() {
        System.out.println("\n[TEST 1] Sequential Booking Transactions");
        System.out.println("--------------------------------------------------");
        
        int numBookings = 50;
        int localSuccess = 0;
        int localFailed = 0;
        
        for (int i = 0; i < numBookings; i++) {
            totalTransactions.incrementAndGet();
            
            try {
                // Create fresh system for each test
                CinemaBookingSystem system = new CinemaBookingSystem();
                system.initialize();
                
                // Get first available movie and showtime
                if (!system.getMovies().isEmpty() && !system.getMovies().get(0).showtimes.isEmpty()) {
                    Movie movie = system.getMovies().get(0);
                    Showtime showtime = movie.showtimes.get(0);
                    
                    // Try to book first available seat
                    List<Seat> availableSeats = showtime.getAvailableSeats();
                    if (!availableSeats.isEmpty()) {
                        String seatNumber = availableSeats.get(0).seatNumber;
                        
                        // Create test user
                        User testUser = new User("TEST" + i, "TestUser" + i, "pass", "CUSTOMER");
                        
                        // Simulate booking
                        boolean bookingSuccess = simulateBooking(system, testUser, movie, showtime, seatNumber);
                        
                        if (bookingSuccess) {
                            successfulTransactions.incrementAndGet();
                            localSuccess++;
                        } else {
                            failedTransactions.incrementAndGet();
                            localFailed++;
                        }
                    } else {
                        failedTransactions.incrementAndGet();
                        localFailed++;
                    }
                } else {
                    failedTransactions.incrementAndGet();
                    localFailed++;
                }
                
            } catch (Exception e) {
                failedTransactions.incrementAndGet();
                localFailed++;
                System.out.println("  Transaction " + i + " FAILED: " + e.getMessage());
            }
        }
        
        System.out.println("Result: " + localSuccess + " successful, " + localFailed + " failed");
        System.out.println("Success Rate: " + String.format("%.2f%%", (localSuccess * 100.0 / numBookings)));
    }
    
    // ==================== TEST 2: Concurrent Bookings ====================
    private static void test2_ConcurrentBookings() {
        System.out.println("\n[TEST 2] Concurrent Booking Stress Test");
        System.out.println("--------------------------------------------------");
        
        int numThreads = 10;
        int bookingsPerThread = 5;
        
        CinemaBookingSystem sharedSystem = new CinemaBookingSystem();
        sharedSystem.initialize();
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(numThreads);
        
        AtomicInteger concurrentSuccess = new AtomicInteger(0);
        AtomicInteger concurrentFailed = new AtomicInteger(0);
        
        // Launch all threads
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    // Each thread attempts bookings
                    for (int b = 0; b < bookingsPerThread; b++) {
                        totalTransactions.incrementAndGet();
                        
                        try {
                            // All threads compete for same seats
                            if (!sharedSystem.getMovies().isEmpty()) {
                                Movie movie = sharedSystem.getMovies().get(0);
                                if (!movie.showtimes.isEmpty()) {
                                    Showtime showtime = movie.showtimes.get(0);
                                    
                                    // Try to book first available seat (race condition!)
                                    List<Seat> availableSeats = showtime.getAvailableSeats();
                                    if (!availableSeats.isEmpty()) {
                                        String seatNumber = availableSeats.get(0).seatNumber;
                                        
                                        User testUser = new User("THREAD" + threadId + "_" + b, 
                                                               "User" + threadId, "pass", "CUSTOMER");
                                        
                                        boolean success = simulateBooking(sharedSystem, testUser, 
                                                                        movie, showtime, seatNumber);
                                        
                                        if (success) {
                                            concurrentSuccess.incrementAndGet();
                                            successfulTransactions.incrementAndGet();
                                        } else {
                                            concurrentFailed.incrementAndGet();
                                            failedTransactions.incrementAndGet();
                                        }
                                    } else {
                                        concurrentFailed.incrementAndGet();
                                        failedTransactions.incrementAndGet();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            concurrentFailed.incrementAndGet();
                            failedTransactions.incrementAndGet();
                            System.out.println("  Thread " + threadId + " booking " + b + 
                                             " FAILED: " + e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        try {
            // Wait for completion (max 30 seconds)
            boolean completed = completeLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            if (!completed) {
                System.out.println("WARNING: Test timed out!");
            }
            
            // Check for double bookings
            int detectedDoubleBookings = detectDoubleBookings(sharedSystem);
            doubleBookings.addAndGet(detectedDoubleBookings);
            
            System.out.println("Threads: " + numThreads + ", Bookings per thread: " + bookingsPerThread);
            System.out.println("Result: " + concurrentSuccess.get() + " successful, " + 
                             concurrentFailed.get() + " failed");
            System.out.println("Double Bookings Detected: " + detectedDoubleBookings);
            System.out.println("Success Rate: " + String.format("%.2f%%", 
                (concurrentSuccess.get() * 100.0 / (numThreads * bookingsPerThread))));
            
        } catch (InterruptedException e) {
            System.out.println("Test interrupted");
        }
    }
    
    // ==================== TEST 3: Error Recovery ====================
    private static void test3_ErrorRecovery() {
        System.out.println("\n[TEST 3] Error Recovery & Handling");
        System.out.println("--------------------------------------------------");
        
        int localErrors = 0;
        int localRecovered = 0;
        int localCorruptions = 0;
        
        // Test 3.1: Invalid seat number
        System.out.println("  3.1: Invalid Seat Numbers...");
        String[] invalidSeats = {"", "ZZZ99", "A-1", "999", "INVALID"};
        for (String seat : invalidSeats) {
            localErrors++;
            totalErrors.incrementAndGet();
            
            try {
                CinemaBookingSystem system = new CinemaBookingSystem();
                system.initialize();
                
                if (!system.getMovies().isEmpty() && !system.getMovies().get(0).showtimes.isEmpty()) {
                    Movie movie = system.getMovies().get(0);
                    Showtime showtime = movie.showtimes.get(0);
                    User testUser = new User("TEST_ERR", "ErrorTest", "pass", "CUSTOMER");
                    
                    // Try invalid booking
                    boolean result = simulateBooking(system, testUser, movie, showtime, seat);
                    
                    // Should fail gracefully
                    if (!result) {
                        localRecovered++;
                        recoveredErrors.incrementAndGet();
                    } else {
                        localCorruptions++;
                        dataCorruptions.incrementAndGet();
                        System.out.println("    [CORRUPT] Invalid seat '" + seat + "' was accepted!");
                    }
                }
            } catch (Exception e) {
                // Exception is acceptable if system remains consistent
                localRecovered++;
                recoveredErrors.incrementAndGet();
            }
        }
        
        // Test 3.2: Partial transaction failure
        System.out.println("  3.2: Partial Transaction Rollback...");
        localErrors++;
        totalErrors.incrementAndGet();
        
        try {
            CinemaBookingSystem system = new CinemaBookingSystem();
            system.initialize();
            
            if (!system.getMovies().isEmpty() && !system.getMovies().get(0).showtimes.isEmpty()) {
                Showtime showtime = system.getMovies().get(0).showtimes.get(0);
                int initialAvailable = showtime.getAvailableSeats().size();
                
                // Book first seat successfully
                if (!showtime.getAvailableSeats().isEmpty()) {
                    String seat1 = showtime.getAvailableSeats().get(0).seatNumber;
                    showtime.reserveSeat(seat1);
                    
                    // Try to book same seat again (should fail)
                    try {
                        showtime.reserveSeat(seat1);
                        localCorruptions++;
                        dataCorruptions.incrementAndGet();
                        System.out.println("    [CORRUPT] Same seat booked twice!");
                    } catch (IllegalStateException e) {
                        // Expected - check if first booking still valid
                        int afterFailure = showtime.getAvailableSeats().size();
                        if (afterFailure == initialAvailable - 1) {
                            localRecovered++;
                            recoveredErrors.incrementAndGet();
                        } else {
                            localCorruptions++;
                            dataCorruptions.incrementAndGet();
                        }
                    }
                }
            }
        } catch (Exception e) {
            localCorruptions++;
            dataCorruptions.incrementAndGet();
        }
        
        // Test 3.3: File corruption handling
        System.out.println("  3.3: File Corruption Handling...");
        localErrors++;
        totalErrors.incrementAndGet();
        
        try {
            // Corrupt the bookings file
            File bookingsFile = new File("bookings.txt");
            boolean fileExisted = bookingsFile.exists();
            
            try (FileWriter fw = new FileWriter(bookingsFile)) {
                fw.write("CORRUPTED|DATA|INCOMPLETE\n");
                fw.write("INVALID|FORMAT\n");
            }
            
            // Try to load
            CinemaBookingSystem system = new CinemaBookingSystem();
            system.initialize();
            
            // If it doesn't crash, it recovered
            localRecovered++;
            recoveredErrors.incrementAndGet();
            
            // Clean up
            if (!fileExisted) {
                bookingsFile.delete();
            }
            
        } catch (Exception e) {
            System.out.println("    [FAIL] Crashed on corrupted file");
            localCorruptions++;
            dataCorruptions.incrementAndGet();
        }
        
        System.out.println("Errors Injected: " + localErrors);
        System.out.println("Recovered: " + localRecovered);
        System.out.println("Data Corruptions: " + localCorruptions);
        System.out.println("Recovery Rate: " + String.format("%.2f%%", 
            (localRecovered * 100.0 / localErrors)));
    }
    
    // ==================== TEST 4: Data Integrity ====================
    private static void test4_DataIntegrity() {
        System.out.println("\n[TEST 4] Data Integrity Verification");
        System.out.println("--------------------------------------------------");
        
        try {
            CinemaBookingSystem system = new CinemaBookingSystem();
            system.initialize();
            
            // Check 1: No duplicate IDs
            Set<String> movieIds = new HashSet<>();
            Set<String> showtimeIds = new HashSet<>();
            boolean duplicateFound = false;
            
            for (Movie movie : system.getMovies()) {
                if (!movieIds.add(movie.movieId)) {
                    System.out.println("  [FAIL] Duplicate movie ID: " + movie.movieId);
                    duplicateFound = true;
                }
                for (Showtime showtime : movie.showtimes) {
                    if (!showtimeIds.add(showtime.showtimeId)) {
                        System.out.println("  [FAIL] Duplicate showtime ID: " + showtime.showtimeId);
                        duplicateFound = true;
                    }
                }
            }
            
            if (!duplicateFound) {
                System.out.println("  [PASS] No duplicate IDs found");
            }
            
            // Check 2: Booking references valid
            boolean orphanedBookings = false;
            for (Booking booking : system.getBookings()) {
                boolean found = false;
                for (Movie movie : system.getMovies()) {
                    if (movie.title.equals(booking.movieTitle)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println("  [FAIL] Orphaned booking: " + booking.bookingId);
                    orphanedBookings = true;
                }
            }
            
            if (!orphanedBookings) {
                System.out.println("  [PASS] All bookings reference valid movies");
            }
            
            System.out.println("  Data Integrity: " + 
                (duplicateFound || orphanedBookings ? "COMPROMISED" : "INTACT"));
            
        } catch (Exception e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }
    
    // ==================== Helper Methods ====================
    
    private static boolean simulateBooking(CinemaBookingSystem system, User user, 
                                          Movie movie, Showtime showtime, String seatNumber) {
        try {
            // Check if seat is available
            if (!showtime.isSeatAvailable(seatNumber)) {
                return false;
            }
            
            // Reserve the seat
            showtime.reserveSeat(seatNumber);
            
            // Create booking
            List<String> seats = new ArrayList<>();
            seats.add(seatNumber);
            Booking booking = new Booking(
                "B" + System.currentTimeMillis(),
                user.name,
                movie.title,
                showtime.showtimeId,
                seats,
                LocalDateTime.now()
            );
            
            system.getBookings().add(booking);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private static int detectDoubleBookings(CinemaBookingSystem system) {
        int doubleBookingCount = 0;
        
        // Check each showtime for seats booked by multiple bookings
        for (Movie movie : system.getMovies()) {
            for (Showtime showtime : movie.showtimes) {
                for (Seat seat : showtime.seats) {
                    if (seat.isBooked) {
                        // Count how many bookings claim this seat
                        int bookingCount = 0;
                        for (Booking booking : system.getBookings()) {
                            if (booking.showtimeId.equals(showtime.showtimeId) && 
                                booking.seatNumbers.contains(seat.seatNumber)) {
                                bookingCount++;
                            }
                        }
                        
                        if (bookingCount > 1) {
                            System.out.println("  [CRITICAL] Seat " + seat.seatNumber + 
                                             " in showtime " + showtime.showtimeId + 
                                             " booked " + bookingCount + " times!");
                            doubleBookingCount++;
                        }
                    }
                }
            }
        }
        
        return doubleBookingCount;
    }
    
    // ==================== Report Generation ====================
    
    private static void generateReport() {
        long testEndTime = System.currentTimeMillis();
        double runTimeMinutes = (testEndTime - testStartTime) / (1000.0 * 60.0);
        double runTimeHours = runTimeMinutes / 60.0;
        
        System.out.println("\n================================================================");
        System.out.println("                    TEST RESULTS SUMMARY");
        System.out.println("================================================================\n");
        
        // Transaction Metrics
        System.out.println("TRANSACTION METRICS:");
        System.out.println("  Total Transactions: " + totalTransactions.get());
        System.out.println("  Successful: " + successfulTransactions.get());
        System.out.println("  Failed: " + failedTransactions.get());
        
        double successRate = (successfulTransactions.get() * 100.0) / 
                            Math.max(totalTransactions.get(), 1);
        System.out.println("  Transaction Success Rate: " + String.format("%.2f%%", successRate));
        
        // Failure Rate
        double failureRate = failedTransactions.get() / Math.max(runTimeHours, 0.01);
        System.out.println("\nFAILURE RATE:");
        System.out.println("  Test Duration: " + String.format("%.2f", runTimeMinutes) + " minutes");
        System.out.println("  Failure Rate: " + String.format("%.2f", failureRate) + " failures/hour");
        
        // Error Recovery
        System.out.println("\nERROR RECOVERY:");
        System.out.println("  Total Errors Injected: " + totalErrors.get());
        System.out.println("  Errors Recovered: " + recoveredErrors.get());
        System.out.println("  Data Corruptions: " + dataCorruptions.get());
        
        double recoveryRate = (recoveredErrors.get() * 100.0) / Math.max(totalErrors.get(), 1);
        System.out.println("  Error Recovery Rate: " + String.format("%.2f%%", recoveryRate));
        
        // Reliability Issues
        System.out.println("\nRELIABILITY ISSUES DETECTED:");
        System.out.println("  Double Bookings: " + doubleBookings.get());
        System.out.println("  Data Corruptions: " + dataCorruptions.get());
        
        // Overall Assessment
        System.out.println("\n================================================================");
        System.out.println("RELIABILITY SCORE: ");
        
        double reliabilityScore = (successRate * 0.4) + (recoveryRate * 0.3) + 
                                 ((doubleBookings.get() == 0 ? 100 : 0) * 0.3);
        
        System.out.println("  " + String.format("%.2f", reliabilityScore) + "/100");
        
        if (reliabilityScore >= 90) {
            System.out.println("  Assessment: EXCELLENT - Production Ready");
        } else if (reliabilityScore >= 75) {
            System.out.println("  Assessment: GOOD - Minor improvements needed");
        } else if (reliabilityScore >= 60) {
            System.out.println("  Assessment: FAIR - Significant improvements needed");
        } else {
            System.out.println("  Assessment: POOR - Major reliability issues");
        }
        
        System.out.println("================================================================\n");
    }
}
