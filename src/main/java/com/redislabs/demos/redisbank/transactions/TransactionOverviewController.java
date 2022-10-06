package com.redislabs.demos.redisbank.transactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.search.Document;
import com.redis.lettucemod.search.SearchOptions;
import com.redis.lettucemod.search.SearchResults;
import com.redis.lettucemod.timeseries.Sample;
import com.redis.lettucemod.timeseries.TimeRange;
import com.redislabs.demos.redisbank.Config;
import com.redislabs.demos.redisbank.Config.StompConfig;

import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin
public class TransactionOverviewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionOverviewController.class);
    private static final String BALANCE_TS = "balance_ts";
    private static final String SORTED_SET_KEY = "bigspenders";

    private final Config config;
    private final StatefulRedisModulesConnection<String, String> srsc;
    private final StringRedisTemplate redis;

    public TransactionOverviewController(Config config, StatefulRedisModulesConnection<String, String> srsc,
            StringRedisTemplate redis) {
        this.config = config;
        this.srsc = srsc;
        this.redis = redis;
    }

    @GetMapping("/config/stomp")
    public StompConfig stompConfig() {
        return config.getStomp();
    }

    @GetMapping("/balance")
    public Balance[] balance() {
        return new Balance[0];
    }

    @GetMapping("/biggestspenders")
    public BiggestSpenders biggestSpenders() {
        return new BiggestSpenders(0);

    }

    @GetMapping("/search")
    @SuppressWarnings("all")
    public ResponseEntity<List<JsonNode>> searchTransactions(@RequestParam("term") String term) throws JsonProcessingException {
        LOGGER.info("RediSearch: "+term);
        SearchResults<String, String> results = new SearchResults<String, String>();
        LOGGER.info("RediSearch returned: "+results.size());

        // Redis returns a raw list of $:"JSON string" so we further simplify the output for the Frontend
        // at this stage Lettuce does not transform the content, so here is example code to turn this back in JSON
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> resultsJ = new ArrayList<JsonNode>();
        for (int i = 0; i < results.size(); i++) {
            resultsJ.add(mapper.readTree(results.get(i).get("$")));
        }
        return ResponseEntity.ok(resultsJ);
    }

    @GetMapping("/transactions")
    public SearchResults<String, String> listTransactions() {
        LOGGER.info("RediSearch: by Account: "+"lars");
        RediSearchCommands<String, String> commands = srsc.sync();
        SearchResults<String, String> results = commands.search(BankTransactionGenerator.ACCOUNT_INDEX, "lars");
        return results;
    }

}
