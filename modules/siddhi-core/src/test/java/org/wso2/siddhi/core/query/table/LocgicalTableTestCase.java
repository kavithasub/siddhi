/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.table;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LocgicalTableTestCase {
    private static final Logger log = Logger.getLogger(LocgicalTableTestCase.class);
    private AtomicInteger inEventCount = new AtomicInteger(0);
    private int removeEventCount;
    private boolean eventArrived;
    private List<Object[]> inEventsList;

    @Before
    public void init() {
        inEventCount.set(0);
        removeEventCount = 0;
        eventArrived = false;
        inEventsList = new ArrayList<Object[]>();
    }

    //Join Test

    @Test
    public void logicalTableTest1() throws InterruptedException {
        log.info("logicalTableTest1");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and CheckStockStream.volume==200 " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"WSO2", 200l});
            checkStockStream.send(new Object[]{"GOOG", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300l},
                    new Object[]{"WSO2", 100l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest2() throws InterruptedException {
        log.info("logicalTableTest2");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume==300 " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 200l});
            checkStockStream.send(new Object[]{"WSO2", 200l});
            checkStockStream.send(new Object[]{"GOOG", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300l},
                    new Object[]{"GOOG", 300l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest3() throws InterruptedException {
        log.info("logicalTableTest3");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume==CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 300l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 300l});
            checkStockStream.send(new Object[]{"WSO2", 100l});
            checkStockStream.send(new Object[]{"GOOG", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 300l},
                    new Object[]{"WSO2", 100l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest4() throws InterruptedException {
        log.info("logicalTableTest4");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol and StockTable.volume<=CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 50l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 300l});
            checkStockStream.send(new Object[]{"WSO2", 100l});
            checkStockStream.send(new Object[]{"GOOG", 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 50l},
                    new Object[]{"WSO2", 100l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest5() throws InterruptedException {
        log.info("logicalTableTest5");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on 55.6f==StockTable.price and StockTable.volume<=CheckStockStream.volume " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 50l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 150l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 100l},
                    new Object[]{"IBM", 50l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isUnsortedEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    @Test
    public void logicalTableTest6() throws InterruptedException {
        log.info("logicalTableTest6");

        SiddhiManager siddhiManager = new SiddhiManager();
        String streams = "" +
                "define stream StockStream (symbol string, price float, volume long); " +
                "define stream CheckStockStream (symbol string, price float, volume long); " +
                "define stream UpdateStockStream (symbol string, price float, volume long);" +
                "@PrimaryKey('symbol') " +
                "@Index('volume') " +
                "define table StockTable (symbol string, price float, volume long); ";
        String query = "" +
                "@info(name = 'query1') " +
                "from StockStream " +
                "insert into StockTable ;" +
                "" +
                "@info(name = 'query2') " +
                "from CheckStockStream join StockTable " +
                " on CheckStockStream.symbol==StockTable.symbol " +
                "   and StockTable.volume==CheckStockStream.volume " +
                "   and StockTable.price<=CheckStockStream.price " +
                "select CheckStockStream.symbol, StockTable.volume " +
                "insert into OutStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        for (Event event : inEvents) {
                            inEventsList.add(event.getData());
                            inEventCount.incrementAndGet();
                        }
                        eventArrived = true;
                    }
                    if (removeEvents != null) {
                        removeEventCount = removeEventCount + removeEvents.length;
                    }
                    eventArrived = true;
                }
            });

            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler checkStockStream = executionPlanRuntime.getInputHandler("CheckStockStream");

            executionPlanRuntime.start();
            stockStream.send(new Object[]{"WSO2", 55.6f, 100l});
            stockStream.send(new Object[]{"IBM", 55.6f, 50l});
            stockStream.send(new Object[]{"GOOG", 55.6f, 300l});
            checkStockStream.send(new Object[]{"IBM", 55.6f, 50l});
            checkStockStream.send(new Object[]{"WSO2", 55.6f, 100l});

            List<Object[]> expected = Arrays.asList(
                    new Object[]{"IBM", 50l},
                    new Object[]{"WSO2", 100l}
            );
            SiddhiTestHelper.waitForEvents(100, 2, inEventCount, 60000);
            Assert.assertEquals("In events matched", true, SiddhiTestHelper.isEventsMatch(inEventsList, expected));
            Assert.assertEquals("Number of success events", 2, inEventCount.get());
            Assert.assertEquals("Number of remove events", 0, removeEventCount);
            Assert.assertEquals("Event arrived", true, eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }
}
