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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowTransactionRuleStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule.TransactionRuleResultSet;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TransactionRuleResultSetTest {
    
    @Before
    public void before() {
        TransactionRuleConfiguration configuration = new TransactionRuleConfiguration("XA", "Atomikos");
        ContextManager manager = mock(ContextManager.class);
        when(manager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class));
        when(manager.getMetaDataContexts().getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        when(manager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(configuration));
        ProxyContext.getInstance().init(manager);
    }
    
    @Test
    public void assertGetRowData() {
        DistSQLResultSet resultSet = new TransactionRuleResultSet();
        resultSet.init(mock(ShardingSphereMetaData.class), mock(ShowTransactionRuleStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("XA"));
        assertThat(rowData.next(), is("Atomikos"));
    }
}
