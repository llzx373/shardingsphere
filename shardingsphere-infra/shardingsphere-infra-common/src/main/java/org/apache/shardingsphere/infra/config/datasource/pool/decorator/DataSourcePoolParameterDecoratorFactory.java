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

package org.apache.shardingsphere.infra.config.datasource.pool.decorator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Data source pool parameter decorator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePoolParameterDecoratorFactory {
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePoolParameterDecorator.class);
    }
    
    /**
     * Decorate data source.
     *
     * @param dataSource data source to be decorated
     * @return decorated data source
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static DataSource decorate(final DataSource dataSource) {
        Optional<DataSourcePoolParameterDecorator> decorator = TypedSPIRegistry.findRegisteredService(DataSourcePoolParameterDecorator.class, dataSource.getClass().getCanonicalName(), null);
        return decorator.isPresent() ? decorator.get().decorate(dataSource) : dataSource;
    }
}
