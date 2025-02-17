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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.decorator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.mock.MockedDriver;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class HikariParameterDecoratorTest {
    
    @Test
    public void assertDecorate() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(MockedDriver.class.getName());
        dataSource.setJdbcUrl("mock:jdbc");
        HikariDataSource actual = new HikariParameterDecorator().decorate(dataSource);
        Properties props = actual.getDataSourceProperties();
        assertThat(props.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("prepStmtCacheSize"), is("200000"));
        assertThat(props.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(props.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("netTimeoutForStreamingResults"), is("0"));
        assertThat(props.getProperty("tinyInt1isBit"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("useSSL"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("serverTimezone"), is("UTC"));
    }
    
    @Test
    public void assertDecorateWithExistedParam() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(MockedDriver.class.getName());
        dataSource.setJdbcUrl("mock:jdbc://127.0.0.1:3306/test0?tinyInt1isBit=true&useSSL=false");
        HikariDataSource actual = new HikariParameterDecorator().decorate(dataSource);
        Properties props = actual.getDataSourceProperties();
        assertThat(props.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("prepStmtCacheSize"), is("200000"));
        assertThat(props.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(props.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(props.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(props.getProperty("netTimeoutForStreamingResults"), is("0"));
        assertNull(props.getProperty("tinyInt1isBit"));
        assertNull(props.getProperty("useSSL"));
        assertThat(props.getProperty("serverTimezone"), is("UTC"));
    }
}
