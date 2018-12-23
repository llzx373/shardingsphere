/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.transaction;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.loader.DataSourceMapConverterSPILoader;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SPIDataSourceMapConverterTest {
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Test
    public void assertCreateBackendDatasourceSuccess() {
        assertTrue(DataSourceMapConverterSPILoader.findDataSourceConverter(TransactionType.XA).isPresent());
        Map<String, DataSource> backendDatasourceMap = DataSourceMapConverterSPILoader.findDataSourceConverter(TransactionType.XA).get().convert(DatabaseType.MySQL, dataSourceMap);
        assertTrue(backendDatasourceMap.isEmpty());
    }
}
