/*
 * Copyright (C) 2014 The Calrissian Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.calrissian.flowbox.example;

import com.google.common.collect.Iterables;
import org.calrissian.flowbox.example.support.ExampleRunner;
import org.calrissian.flowbox.example.support.FlowProvider;
import org.calrissian.flowbox.model.Event;
import org.calrissian.flowbox.model.Flow;
import org.calrissian.flowbox.model.Policy;
import org.calrissian.flowbox.model.Tuple;
import org.calrissian.flowbox.model.builder.FlowBuilder;
import org.calrissian.flowbox.support.Criteria;
import org.calrissian.flowbox.support.Function;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
/**
 * An example showing how a stream can output directly to another stream without sending its output events to the
 * standard output component (stream bridging). This essentially leads to streams feeding directly into other
 * streams, allowing for things like joins.
 */
public class JoinExample implements FlowProvider {

  @Override
  public List<Flow> getFlows() {
    Flow flow = new FlowBuilder()
      .id("flow")
      .flowDefs()
        .stream("stream1")
            .each().function(new Function() {
              @Override
              public List<Event> execute(Event event) {
                Event newEvent = new Event(event.getId(), event.getTimestamp());
                newEvent.putAll(Iterables.concat(event.getTuples().values()));
                newEvent.put(new Tuple("stream", "stream1"));
                return singletonList(newEvent);
              }
            }).end()
        .endStream(false, "stream3")   // send ALL results to stream2 and not to standard output
        .stream("stream2")      // don't read any events from standard input
          .each().function(new Function() {
            @Override
            public List<Event> execute(Event event) {
              Event newEvent = new Event(event.getId(), event.getTimestamp());
              newEvent.putAll(Iterables.concat(event.getTuples().values()));
              newEvent.put(new Tuple("stream", "stream2"));
              return singletonList(newEvent);
            }
          }).end()
        .endStream(false, "stream3")
        .stream("stream3", false)
            .join("stream1", "stream2").evict(Policy.TIME, 5).end()
        .endStream()
      .endDefs()
    .createFlow();

    return asList(new Flow[]{flow});
  }

  public static void main(String args[]) {
    new ExampleRunner(new JoinExample()).run();
  }
}
