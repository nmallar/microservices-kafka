package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.exception.TwitterToKafkaServiceException;
import com.microservices.demo.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.Status;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name="twitter-to-kafka-service.enable-mock-tweets",havingValue = "true",matchIfMissing = true)
public class MockKafkaStreamRunner implements StreamRunner {

    private static final Logger LOG= LoggerFactory.getLogger(TwitterKafkaStreamRunner.class);
    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private static final Random RANDOM=new Random();


    private static final String[] WORDS= new String[] {

            "Lorem",
            "ipsum",
            "dolor",
            "sit",
            "amet",
            "consectetur",
            "adipiscing",
            "elit",
            "Sed",
            "laoreet",
            "nisi",
            "at",
            "elit",
            "venenatis",
            "fringilla",
            "Cras",
            "ut",
            "semper",
            "quam",
            "sit"
    };

//    private static final String tweetRowAsJson=
//            "{\"created_at\":\"{0}\","+
//                    "\"id\":\"{1}\","+
//                    "\"text\":\"{2}\","+
//                    "\"user\":\"{3}\"}"+
//                    "}";

    private static final String tweetAsRawJson = "{" +
            "\"created_at\":\"{0}\"," +
            "\"id\":\"{1}\"," +
            "\"text\":\"{2}\"," +
            "\"user\":{\"id\":\"{3}\"}" +
            "}";
    private static final String TWITTER_STATUS_DATE_FORMAT="EEE MMM dd HH:mm:ss zzz yyyy";



    public MockKafkaStreamRunner(TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData, TwitterKafkaStatusListener twitterKafkaStatusListener) {
        this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
        this.twitterKafkaStatusListener = twitterKafkaStatusListener;
    }

    @Override
    public void start() throws TwitterException {

        String[] keywords=twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);

        int minTweetLength=twitterToKafkaServiceConfigData.getMockMinTweetLength();
        int maxTweetLength=twitterToKafkaServiceConfigData.getMockMaxTweetLength();
        long sleepTiemMs=twitterToKafkaServiceConfigData.getMockSleepMs();
        LOG.info("Strating mock flitering twitter streams for keywords {}", Arrays.toString(keywords));


        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTiemMs);

    }

    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTiemMs)
    {

        Executors.newSingleThreadExecutor().submit(()-> {

            try {
                while (true){
                    String formattedTweetAsRawJson= getFormattedTweet(keywords, minTweetLength, maxTweetLength);
                    Status status= TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    twitterKafkaStatusListener.onStatus(status);
                    sleep(sleepTiemMs);
                }
            } catch (TwitterException e) {
               LOG.error("Error creating twitter status",e);
            }
        });

    }


    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {

        StringBuilder tweet=new StringBuilder();
        int tweetLength=RANDOM.nextInt(maxTweetLength-minTweetLength+1)+minTweetLength;

        return constructRandomTweet(keywords, tweetLength, tweet);

    }

    private String constructRandomTweet(String[] keywords, int tweetLength, StringBuilder tweet) {
        for (int i = 0; i < tweetLength; i++) {
            tweet.append( WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if(i== tweetLength /2){
                tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweet.toString().trim();
    }

    private void sleep(long sleepTiemMs) {
        
        try{
            Thread.sleep(sleepTiemMs);
        }catch (InterruptedException e){
            throw new TwitterToKafkaServiceException("Error while sleeping for waiting new status to create");
      
        }
    }
    private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {

        String[] params=new String[] {
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                        getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                        String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };

        return formatTweetAsJsonWithParams(params);

    }

    private  String formatTweetAsJsonWithParams(String[] params) {
        String tweet=tweetAsRawJson;

        for (int i = 0; i < params.length; i++) tweet = tweet.replace("{" + i + "}", params[i]);
        return tweet;
    }
}

