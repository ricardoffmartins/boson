package io.boson.json.extractors;


import javax.json.stream.JsonParser;

import java.math.BigDecimal;

import static javax.json.stream.JsonParser.Event.KEY_NAME;


public class NumberExtractor  {

    public final String keyName;

    public NumberExtractor(String keyName) {
        this.keyName = keyName;
    }

//    public Validation<BigDecimal> apply(JsonParser jsonStream) {
//
//        JsonParser.Event event = jsonStream.next();
//        Validation<BigDecimal> validation;
//        if ( event == KEY_NAME ) {
//            String key = jsonStream.getString();
//            event = jsonStream.next();
//            if (key.equals(keyName)) {
//                validation = new Result<BigDecimal>(jsonStream.getBigDecimal());
//            } else {
//                validation = apply(jsonStream);
//            }
//        } else {
//            validation = new Trace<BigDecimal>("Unexpected event " + event.name() + " expecting " + KEY_NAME.name());
//        }
//
//        return validation;
//    }



}
