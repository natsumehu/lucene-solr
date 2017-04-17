/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.client.solrj.io.stream.eval;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.map.HashedMap;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.eval.ConversionEvaluator;
import org.apache.solr.client.solrj.io.eval.RawValueEvaluator;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorDay;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorDayOfQuarter;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorDayOfYear;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorEpoch;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorHour;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorMinute;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorMonth;
import org.apache.solr.client.solrj.io.eval.StreamEvaluator;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorQuarter;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorSecond;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorWeek;
import org.apache.solr.client.solrj.io.eval.TemporalEvaluatorYear;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParser;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test ConversionEvaluators
 */
public class ConversionEvaluatorsTest {


  StreamFactory factory;
  Map<String, Object> values;

  public ConversionEvaluatorsTest() {
    super();

    factory = new StreamFactory();
    factory.withFunctionName("convert", ConversionEvaluator.class).withFunctionName("raw", RawValueEvaluator.class);

    values = new HashedMap();
  }

  @Test
  public void testInvalidExpression() throws Exception {

    StreamEvaluator evaluator;

    try {
      evaluator = factory.constructEvaluator("convert(inches)");
      StreamContext streamContext = new StreamContext();
      evaluator.setStreamContext(streamContext);
      assertTrue(false);
    } catch (IOException e) {
      assertTrue(e.getCause().getCause().getMessage().contains("Invalid expression convert(inches) - expecting 3 value but found 1"));
    }

    try {
      evaluator = factory.constructEvaluator("convert(inches, yards, 3)");
      StreamContext streamContext = new StreamContext();
      evaluator.setStreamContext(streamContext);
      Tuple tuple = new Tuple(new HashMap());
      evaluator.evaluate(tuple);
      assertTrue(false);
    } catch (IOException e) {
      assertTrue(e.getCause().getCause().getMessage().contains("No conversion available from INCHES to YARDS"));
    }
  }

  @Test
  public void testInches() throws Exception {
    testFunction("convert(inches, centimeters, 2)", (double)(2*2.54));
    testFunction("convert(inches, meters, 2)", (double)(2*0.0254));
    testFunction("convert(inches, millimeters, 2)", (double)(2*25.40));
  }

  @Test
  public void testYards() throws Exception {
    testFunction("convert(yards, meters, 2)", (double)(2*.91));
    testFunction("convert(yards, kilometers, 2)", (double)(2*.00091));
  }

  @Test
  public void testMiles() throws Exception {
    testFunction("convert(miles, kilometers, 2)", (double)(2*1.61));
  }

  @Test
  public void testMillimeters() throws Exception {
    testFunction("convert(millimeters, inches, 2)", (double)(2*.039));
  }

  @Test
  public void testCentimeters() throws Exception {
    testFunction("convert(centimeters, inches, 2)", (double)(2*.39));
  }

  @Test
  public void testMeters() throws Exception {
    testFunction("convert(meters, feet, 2)", (double)(2*3.28));
  }

  @Test
  public void testKiloMeters() throws Exception {
    testFunction("convert(kilometers, feet, 2)", (double)(2*3280.8));
    testFunction("convert(kilometers, miles, 2)", (double)(2*.62));
  }

  public void testFunction(String expression, Number expected) throws Exception {
    StreamEvaluator evaluator = factory.constructEvaluator(expression);
    StreamContext streamContext = new StreamContext();
    evaluator.setStreamContext(streamContext);
    Object result = evaluator.evaluate(new Tuple(values));
    assertTrue(result instanceof Number);
    assertEquals(expected, result);
  }


}