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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Result set for show readwrite splitting rule.
 */
public final class ReadwriteSplittingRuleQueryResultSet implements DistSQLResultSet {
    
    private Iterator<ReadwriteSplittingDataSourceRuleConfiguration> data;
    
    private Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers;

    private Map<String, Map<String, String>> autoAwareDataSourceMap;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> optional.getDataSources().iterator()).orElse(Collections.emptyIterator());
        loadBalancers = ruleConfig.map(ReadwriteSplittingRuleConfiguration::getLoadBalancers).orElse(Collections.emptyMap());
        Optional<ExportableRule> exportableRule = metaData.getRuleMetaData().getRules().stream()
                .filter(each -> each instanceof ExportableRule && ((ExportableRule) each).export().containsKey(ExportableConstants.AUTO_AWARE_DATA_SOURCE_KEY))
                .map(each -> (ExportableRule) each).findAny();
        autoAwareDataSourceMap = (Map<String, Map<String, String>>) exportableRule.map(optional -> optional.export().get(ExportableConstants.AUTO_AWARE_DATA_SOURCE_KEY))
                .orElse(Collections.emptyMap());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "auto_aware_data_source_name", "write_data_source_name", "read_data_source_names", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = data.next();
        Optional<ShardingSphereAlgorithmConfiguration> configuration = Optional.ofNullable(loadBalancers.get(ruleConfig.getLoadBalancerName()));
        String writeDataSourceName = ruleConfig.getWriteDataSourceName();
        String readDataSourceNames = Joiner.on(",").join(ruleConfig.getReadDataSourceNames());
        if (null != ruleConfig.getAutoAwareDataSourceName()) {
            writeDataSourceName = autoAwareDataSourceMap.get(ruleConfig.getName()).get(ExportableConstants.PRIMARY_DATA_SOURCE_NAME);
            readDataSourceNames = autoAwareDataSourceMap.get(ruleConfig.getName()).get(ExportableConstants.REPLICA_DATA_SOURCE_NAMES);
        }
        return Arrays.asList(ruleConfig.getName(), ruleConfig.getAutoAwareDataSourceName(), writeDataSourceName, readDataSourceNames, 
                configuration.map(ShardingSphereAlgorithmConfiguration::getType).orElse(null), 
                PropertiesConverter.convert(configuration.map(ShardingSphereAlgorithmConfiguration::getProps).orElseGet(Properties::new)));
    }
    
    @Override
    public String getType() {
        return ShowReadwriteSplittingRulesStatement.class.getCanonicalName();
    }
}
