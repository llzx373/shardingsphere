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

package org.apache.shardingsphere.dbdiscovery.rule.builder;

import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;

import java.util.Collection;

/**
 * Algorithm provided data base discovery rule builder.
 */
public final class AlgorithmProvidedDatabaseDiscoveryRuleBuilder implements SchemaRuleBuilder<AlgorithmProvidedDatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public DatabaseDiscoveryRule build(final SchemaRulesBuilderMaterials materials, final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config,
                                       final Collection<ShardingSphereRule> builtRules) {
        return new DatabaseDiscoveryRule(materials.getSchemaName(), materials.getDataSourceMap(), config);
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ALGORITHM_PROVIDER_ORDER;
    }
    
    @Override
    public Class<AlgorithmProvidedDatabaseDiscoveryRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class;
    }
}
