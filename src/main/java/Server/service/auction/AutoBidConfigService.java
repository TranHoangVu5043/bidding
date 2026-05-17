package Server.service.auction;

import Server.model.auction.AutoBidConfig;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidConfigService {
    private final Map<Integer, PriorityQueue<AutoBidConfig>> autoBidmaps = new ConcurrentHashMap<>();
}
