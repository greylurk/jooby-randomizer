package com.greylurk.randomizer.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RandomTable {
    private static final Logger logger = LoggerFactory.getLogger(RandomTable.class);
    private static final Pattern dicePattern = Pattern.compile("(\\d+)d(\\d+)");
    private static final Pattern moustachePattern = Pattern.compile("\\{([^\\}]+)\\}");
	private static final Pattern additionPattern = Pattern.compile("(\\d+)\\s*\\+\\s*(\\d+)");
    Map<String, Object> values;

    public RandomTable(Map<String, Object> values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public String expandValue(String name) {
        logger.debug(name);
        Object value = values.get(name);
        if( value == null ) {
            logger.debug("Got a null value");
        }
        if( value instanceof Map<?, ?>) {
            return eval((Map<String,String>) value);
        } else if( value instanceof List<?> ) {
            return eval((List<String>) value);
        } else if( value instanceof String ) {
            return eval((String) value);
        } else {
            return "None";
        }
    }

	private String eval(Map<String, String> value) {
        List<String>  weightedList = new LinkedList<String>();
        for( String key: value.keySet() ) {
            if( key.indexOf('-') != -1 ) {
                String[] numbers = StringUtils.split(key, '-');
                int start = Integer.parseInt(numbers[0]);
                int end = Integer.parseInt(numbers[1]);
                if( end == 0 ) {
                    end = 100;
                }
                logger.debug("numbers: " + start + " - " + end);
                for( int counter=start; counter <= end; counter++) {
                    weightedList.add(value.get(key));
                }
            } else {
                weightedList.add(value.get(key));
            }
        }
        return eval(weightedList);
	}

    private String eval(final String value) {
        return evalMoustache(evalAddition(evalDice(value)));
    }

    /**
     * Evaluate moustache expressions inside a string by checking the other keys in the
     * table and expanding them.
     */
	private String evalMoustache(final String value) {
        Matcher matcher = moustachePattern.matcher(value);
        if( matcher.groupCount() == 0 ) {
            return value;
        }
        matcher.reset();
        StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(buffer, expandValue(matcher.group(1)));
        }
        matcher.appendTail(buffer);
		return evalDice(buffer.toString());
    }

    /**
     * Evaluate dice expressions in a string by generating random numbers.
     */
    private String evalDice(final String value ) {
        Matcher matcher = dicePattern.matcher(value);
        if( matcher.groupCount() == 0 ) {
            return value;
        }
        matcher.reset();
        StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            int count = Integer.parseInt(matcher.group(1));
            int die = Integer.parseInt(matcher.group(2));
            int result = IntStream.range(0,count)
                .map((i) -> ThreadLocalRandom.current().nextInt(die))
                .reduce(0, Integer::sum);
            matcher.appendReplacement(buffer, Integer.toString(result));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String evalAddition(final String value ) {
        Matcher matcher = additionPattern.matcher(value);
        if( matcher.groupCount() == 0 ) {
            return value;
        }
        StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            int sum = Integer.sum(Integer.parseInt(matcher.group(1)),Integer.parseInt(matcher.group(2)));
            matcher.appendReplacement(buffer, Integer.toString(sum));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

	private String eval(final List<String> value) {
        logger.debug("Generating random entry between 0 and " + value.size());
        int entry = ThreadLocalRandom.current().nextInt(0,value.size());
        return eval(value.get(entry));
	}

}