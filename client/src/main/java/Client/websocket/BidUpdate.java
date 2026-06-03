package Client.websocket;

/**
 * Immutable snapshot of one WebSocket {@code bid_update} message.
 *
 * @param auctionId  the auction that was just bid on
 * @param newPrice   the new current price after the bid
 * @param newEndTime ISO-8601 string of the reset end-time when anti-snipe fired, or {@code null}
 */
public record BidUpdate(int auctionId, double newPrice, String newEndTime) {}
