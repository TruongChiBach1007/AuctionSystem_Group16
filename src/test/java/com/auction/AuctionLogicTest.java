package com.auction;

import com.auction.model.core.Auction;
import com.auction.model.core.Bid;
import com.auction.model.items.Electronics;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.users.Bidder;
import com.auction.service.AutoBid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionLogicTest {

    @TempDir
    Path tempDir;

    @Test
    void acceptsOnlyHigherBidWithEnoughBalance() {
        Auction auction = new Auction(100, 60_000);
        Bidder bidder = new Bidder(1, "bach", "123", "Bach", "bach@example.com", 500);

        assertTrue(auction.placeBid(new Bid(bidder, 150)));
        assertEquals(150, auction.getCurrentPrice(), 0.001);
        assertEquals(bidder, auction.getHighestBidder());

        assertFalse(auction.placeBid(new Bid(bidder, 120)));
        assertFalse(auction.placeBid(new Bid(bidder, 600)));
        assertEquals(150, auction.getCurrentPrice(), 0.001);
    }

    @Test
    void rejectsBidAfterAuctionIsClosed() {
        Auction auction = new Auction(100, 60_000);
        Bidder bidder = new Bidder(1, "bach", "123", "Bach", "bach@example.com", 500);

        auction.closeAuction();

        assertThrows(IllegalStateException.class, () -> auction.placeBid(new Bid(bidder, 150)));
    }

    @Test
    void autoBidRaisesPriceWithinConfiguredLimit() {
        Auction auction = new Auction(100, 60_000);
        Bidder manualBidder = new Bidder(1, "manual", "123", "Manual Bidder", "manual@example.com", 500);
        Bidder autoBidder = new Bidder(2, "auto", "123", "Auto Bidder", "auto@example.com", 500);

        auction.registerAutoBid(new AutoBid(autoBidder, 200, 10));
        auction.placeBid(new Bid(manualBidder, 150));

        assertEquals(160, auction.getCurrentPrice(), 0.001);
        assertEquals(autoBidder, auction.getHighestBidder());
    }

    @Test
    void concurrentBidsKeepHighestPrice() throws InterruptedException {
        Auction auction = new Auction(100, 60_000);
        List<Thread> threads = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            double amount = 100 + i;
            Bidder bidder = new Bidder(i, "user" + i, "123", "User " + i, "u" + i + "@example.com", 1000);
            Thread thread = new Thread(() -> auction.placeBid(new Bid(bidder, amount)));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(120, auction.getCurrentPrice(), 0.001);
        assertEquals("User 20", auction.getHighestBidder().getFullName());
    }

    @Test
    void itemCanBeSavedAndLoadedWithJavaSerialization() throws Exception {
        Path file = tempDir.resolve("item.ser");
        Electronics original = new Electronics("item-test", "Camera", "Mirrorless camera", 1000, 1200);
        original.setStatus(ItemStatus.APPROVED);
        original.setSellerUsername("seller1");

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(original);
        }

        Object loaded;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            loaded = in.readObject();
        }

        Item item = assertInstanceOf(Item.class, loaded);
        assertEquals("item-test", item.getId());
        assertEquals(1200, item.getCurrentPrice(), 0.001);
        assertEquals(ItemStatus.APPROVED, item.getStatus());
        assertEquals("seller1", item.getSellerUsername());
    }
}
