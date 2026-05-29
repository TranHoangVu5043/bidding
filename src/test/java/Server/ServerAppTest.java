package Server;


import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.networking.DataSourceFactory;
import Server.service.auction.AutoBidConfigService;
import Server.service.auction.BiddingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.desktop.SystemSleepEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class ServerAppTest
{
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}
