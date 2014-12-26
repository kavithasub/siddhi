/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.input.stream.join;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.processor.Processor;

import java.util.ArrayList;
import java.util.List;

public class JoinStreamRuntime implements StreamRuntime {

    List<SingleStreamRuntime> singleStreamRuntimeList = new ArrayList<SingleStreamRuntime>();
    private ExecutionPlanContext executionPlanContext;

    public JoinStreamRuntime(ExecutionPlanContext executionPlanContext) {

        this.executionPlanContext = executionPlanContext;
    }


    public void addRuntime(SingleStreamRuntime singleStreamRuntime) {
        singleStreamRuntimeList.add(singleStreamRuntime);
    }

    public List<SingleStreamRuntime> getSingleStreamRuntimes() {
        return singleStreamRuntimeList;
    }

    @Override
    public StreamRuntime clone(String key) {
        JoinStreamRuntime joinStreamRuntime = new JoinStreamRuntime(executionPlanContext);
        for (SingleStreamRuntime singleStreamRuntime : singleStreamRuntimeList) {
            joinStreamRuntime.addRuntime((SingleStreamRuntime) singleStreamRuntime.clone(key));
        }
        return joinStreamRuntime;
    }

    @Override
    public void setCommonProcessor(Processor commonProcessor) {
        for (SingleStreamRuntime singleStreamRuntime : singleStreamRuntimeList) {
            singleStreamRuntime.setCommonProcessor(commonProcessor);
        }

    }
}