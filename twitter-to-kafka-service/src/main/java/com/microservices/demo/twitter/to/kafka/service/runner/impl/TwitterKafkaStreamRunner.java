package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.TwitterException;


import java.util.Arrays;

@Component

@ConditionalOnProperty(name="twitter-to-kafka-service.enable-mock-tweets",havingValue = "false")
public class TwitterKafkaStreamRunner implements StreamRunner {

    private static final Logger LOG= LoggerFactory.getLogger(TwitterKafkaStreamRunner.class);
    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;

    private twitter4j.TwitterStream twitterStream;

    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    public TwitterKafkaStreamRunner(TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData,
                                    TwitterKafkaStatusListener twitterKafkaStatusListener) {
        this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
    }


    @Override
    public void start() throws TwitterException {
//        twitterStream =new TwitterStreamFactory().getInstance();
//        twitterStream.addListener( twitterKafkaStatusListener);
    LOG.info("starting the run method");
        addFilter();



    }

    @PreDestroy
    public void shutDown(){
        LOG.info("Closing stream");

    }

    private void addFilter(){
        String[] keywords=twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
//        twitter4j.FilterQuery filterQuery=new FilterQuery(keywords);
        LOG.info("Started filtering twitter stream for keywords {}", Arrays.toString(keywords));
    }
}
