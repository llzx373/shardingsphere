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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract shadow DML statement route engine.
 */
public abstract class AbstractShadowDMLStatementRouteEngine implements ShadowRouteEngine {
    
    @Getter
    private final Map<String, String> tableAliasNameMappings = new LinkedHashMap<>();
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        findShadowDataSourceMappings(shadowRule).ifPresent(shadowDataSourceMappings -> shadowRouteDecorate(routeContext, shadowDataSourceMappings));
    }
    
    private Optional<Map<String, String>> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Collection<String> relatedShadowTables = getRelatedShadowTables(getAllTables(), shadowRule);
        if (relatedShadowTables.isEmpty() && isMatchDefaultShadowAlgorithm(shadowRule)) {
            return Optional.of(shadowRule.getAllShadowDataSourceMappings());
        }
        ShadowOperationType shadowOperationType = getShadowOperationType();
        Map<String, String> result = findBySQLComments(relatedShadowTables, shadowRule, shadowOperationType);
        if (!result.isEmpty()) {
            return Optional.of(result);
        }
        result = findByShadowColumn(relatedShadowTables, shadowRule, shadowOperationType);
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private Collection<String> getRelatedShadowTables(final Collection<SimpleTableSegment> simpleTableSegments, final ShadowRule shadowRule) {
        Collection<String> tableNames = new LinkedHashSet<>();
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAlias().isPresent() ? each.getAlias().get() : tableName;
            tableNames.add(tableName);
            tableAliasNameMappings.put(alias, tableName);
        }
        return shadowRule.getRelatedShadowTables(tableNames);
    }
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultShadowAlgorithm(final ShadowRule shadowRule) {
        Optional<Collection<String>> sqlComments = parseSQLComments();
        if (!sqlComments.isPresent()) {
            return false;
        }
        Optional<ShadowAlgorithm> defaultShadowAlgorithm = shadowRule.getDefaultShadowAlgorithm();
        if (defaultShadowAlgorithm.isPresent()) {
            ShadowAlgorithm shadowAlgorithm = defaultShadowAlgorithm.get();
            if (shadowAlgorithm instanceof HintShadowAlgorithm<?>) {
                ShadowDetermineCondition shadowDetermineCondition = new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH);
                return isMatchHintShadowAlgorithm((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithm, shadowDetermineCondition.initSQLComments(sqlComments.get()), shadowRule);
            }
        }
        return false;
    }
    
    private Map<String, String> findBySQLComments(final Collection<String> relatedShadowTables, final ShadowRule shadowRule, final ShadowOperationType shadowOperationType) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            if (isContainsShadowInSQLComments(each, shadowRule, new ShadowDetermineCondition(each, shadowOperationType))) {
                result.putAll(shadowRule.getRelatedShadowDataSourceMappings(each));
                return result;
            }
        }
        return result;
    }
    
    private boolean isContainsShadowInSQLComments(final String tableName, final ShadowRule shadowRule, final ShadowDetermineCondition shadowCondition) {
        return parseSQLComments().filter(SQLComments -> shadowRule.getRelatedHintShadowAlgorithms(tableName)
                .filter(shadowAlgorithms -> isMatchAnyHintShadowAlgorithms(shadowAlgorithms, shadowCondition.initSQLComments(SQLComments), shadowRule)).isPresent()).isPresent();
    }
    
    private boolean isMatchAnyHintShadowAlgorithms(final Collection<HintShadowAlgorithm<Comparable<?>>> shadowAlgorithms, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        for (HintShadowAlgorithm<Comparable<?>> each : shadowAlgorithms) {
            if (isMatchHintShadowAlgorithm(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchHintShadowAlgorithm(final HintShadowAlgorithm<Comparable<?>> hintShadowAlgorithm, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        return ShadowDeterminerFactory.newInstance(hintShadowAlgorithm).isShadow(shadowCondition, shadowRule);
    }
    
    private Map<String, String> findByShadowColumn(final Collection<String> relatedShadowTables, final ShadowRule shadowRule, final ShadowOperationType shadowOperationType) {
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<Optional<ShadowColumnCondition>> iterator = getShadowColumnConditionIterator();
        while (iterator.hasNext()) {
            Optional<ShadowColumnCondition> next = iterator.next();
            if (!next.isPresent()) {
                continue;
            }
            Optional<String> shadowTable = findShadowTableByShadowColumn(relatedShadowTables, shadowRule, next.get(), shadowOperationType);
            if (!shadowTable.isPresent()) {
                continue;
            }
            result.putAll(shadowRule.getRelatedShadowDataSourceMappings(shadowTable.get()));
            return result;
        }
        return result;
    }
    
    private Optional<String> findShadowTableByShadowColumn(final Collection<String> relatedShadowTables, final ShadowRule shadowRule, final ShadowColumnCondition shadowColumnCondition,
                                                           final ShadowOperationType shadowOperationType) {
        ShadowDetermineCondition shadowDetermineCondition;
        for (String each : relatedShadowTables) {
            shadowDetermineCondition = new ShadowDetermineCondition(each, shadowOperationType);
            if (isContainsShadowInColumn(each, shadowRule, shadowDetermineCondition.initShadowColumnCondition(shadowColumnCondition))) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isContainsShadowInColumn(final String tableName, final ShadowRule shadowRule, final ShadowDetermineCondition shadowCondition) {
        Optional<Collection<ColumnShadowAlgorithm<Comparable<?>>>> relatedColumnShadowAlgorithms = shadowRule.getRelatedColumnShadowAlgorithms(tableName, shadowCondition.getShadowOperationType());
        return relatedColumnShadowAlgorithms.isPresent() && isMatchAnyColumnShadowAlgorithms(relatedColumnShadowAlgorithms.get(), shadowCondition, shadowRule);
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final Collection<ColumnShadowAlgorithm<Comparable<?>>> shadowAlgorithms, final ShadowDetermineCondition shadowCondition,
                                                     final ShadowRule shadowRule) {
        for (ColumnShadowAlgorithm<Comparable<?>> each : shadowAlgorithms) {
            if (isMatchColumnShadowAlgorithm(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchColumnShadowAlgorithm(final ColumnShadowAlgorithm<Comparable<?>> columnShadowAlgorithm, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        return ShadowDeterminerFactory.newInstance(columnShadowAlgorithm).isShadow(shadowCondition, shadowRule);
    }
    
    /**
     * Get all tables.
     *
     * @return all tables
     */
    protected abstract Collection<SimpleTableSegment> getAllTables();
    
    /**
     * get shadow operation type.
     *
     * @return shadow operation type
     */
    protected abstract ShadowOperationType getShadowOperationType();
    
    /**
     * Parse SQL Comments.
     *
     * @return SQL comments
     */
    protected abstract Optional<Collection<String>> parseSQLComments();
    
    /**
     * Get shadow column condition iterator.
     *
     * @return shadow column condition iterator
     */
    protected abstract Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator();
    
    /**
     * Get single table tame.
     *
     * @return table tame
     */
    protected String getSingleTableName() {
        return tableAliasNameMappings.entrySet().iterator().next().getValue();
    }
}
