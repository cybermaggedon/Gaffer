/*
 * Copyright 2017. Crown Copyright
 *
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
 */

package uk.gov.gchq.gaffer.parquetstore.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConversions$;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.parquetstore.testutils.DataGen;
import uk.gov.gchq.gaffer.parquetstore.testutils.TestUtils;
import uk.gov.gchq.gaffer.store.SerialisationFactory;
import uk.gov.gchq.gaffer.store.StoreException;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaElementDefinition;
import uk.gov.gchq.gaffer.store.schema.SchemaOptimiser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class AggregateGafferRowsFunctionTest {
    private SchemaUtils utils;

    @Before
    public void setUp() throws StoreException {
        Logger.getRootLogger().setLevel(Level.WARN);
        final Schema schema = Schema.fromJson(
                getClass().getResourceAsStream("/schemaUsingStringVertexType/dataSchema.json"),
                getClass().getResourceAsStream("/schemaUsingStringVertexType/dataTypes.json"),
                getClass().getResourceAsStream("/schemaUsingStringVertexType/storeSchema.json"),
                getClass().getResourceAsStream("/schemaUsingStringVertexType/storeTypes.json"));
        final SchemaOptimiser optimiser = new SchemaOptimiser(new SerialisationFactory(ParquetStoreConstants.SERIALISERS));
        utils = new SchemaUtils(optimiser.optimise(schema, true));
    }

    @After
    public void cleanUp() {
        utils = null;
    }

    private HashMap<String, String> buildcolumnToAggregatorMap(final SchemaElementDefinition gafferSchema) {
        HashMap<String, String> columnToAggregatorMap = new HashMap<>();
        for (final String column : gafferSchema.getProperties()) {
            columnToAggregatorMap.put(column, gafferSchema.getPropertyTypeDef(column).getAggregateFunction().getClass().getCanonicalName());
        }
        return columnToAggregatorMap;
    }

    @Test
    public void mergeEntityRowsTest() throws OperationException, IOException {
        final String group = "BasicEntity";
        final SchemaElementDefinition elementSchema = utils.getGafferSchema().getElement(group);
        final HashMap<String, String> columnToAggregator = buildcolumnToAggregatorMap(elementSchema);
        final GafferGroupObjectConverter converter = utils.getConverter(group);
        final String[] gafferProperties = new String[elementSchema.getProperties().size()];
        elementSchema.getProperties().toArray(gafferProperties);
        final AggregateGafferRowsFunction aggregator = new AggregateGafferRowsFunction(gafferProperties,
                true, elementSchema.getGroupBy(), utils.getColumnToPaths(group), columnToAggregator, converter);
        final GenericRowWithSchema row1 = DataGen.generateEntityRow(utils, group, "vertex", (byte) 'a', 0.2, 3f, TestUtils.TREESET1, 5L, (short) 6, TestUtils.DATE, TestUtils.FREQMAP1);
        final GenericRowWithSchema row2 = DataGen.generateEntityRow(utils, group, "vertex", (byte) 'c', 0.7, 4f, TestUtils.TREESET2, 7L, (short) 4, TestUtils.DATE, TestUtils.FREQMAP2);
        final GenericRowWithSchema merged = aggregator.call(row1, row2);
        final List<Object> actual = new ArrayList<>(11);
        for (int i = 0; i < merged.length(); i++) {
            actual.add(merged.apply(i));
        }
        final List<Object> expected = new ArrayList<>(11);
        expected.add("vertex");
        expected.add(new byte[]{(byte) 'c'});
        expected.add(0.8999999999999999);
        expected.add(7f);
        expected.add(new String[]{"A", "B", "C"});
        expected.add(12L);
        expected.add(10);
        expected.add(TestUtils.DATE.getTime());
        expected.add(JavaConversions$.MODULE$.mapAsScalaMap(TestUtils.MERGED_FREQMAP));
        expected.add(2);
        assertThat(expected, contains(actual.toArray()));
    }

    @Test
    public void mergeEdgeRowsTest() throws OperationException, SerialisationException {
        final String group = "BasicEdge";
        final SchemaElementDefinition elementSchema = utils.getGafferSchema().getElement(group);
        final HashMap<String, String> columnToAggregator = buildcolumnToAggregatorMap(elementSchema);
        final GafferGroupObjectConverter converter = utils.getConverter(group);
        final String[] gafferProperties = new String[elementSchema.getProperties().size()];
        elementSchema.getProperties().toArray(gafferProperties);
        final AggregateGafferRowsFunction aggregator = new AggregateGafferRowsFunction(gafferProperties,
                false, elementSchema.getGroupBy(), utils.getColumnToPaths(group), columnToAggregator, converter);
        final GenericRowWithSchema row1 = DataGen.generateEdgeRow(utils, group, "src", "dst", true, (byte) 'a', 0.2, 3f, TestUtils.TREESET1, 5L, (short) 6, TestUtils.DATE, TestUtils.FREQMAP1);
        final GenericRowWithSchema row2 = DataGen.generateEdgeRow(utils, group, "src", "dst", true, (byte) 'c', 0.7, 4f, TestUtils.TREESET2, 7L, (short) 4, TestUtils.DATE, TestUtils.FREQMAP2);
        final GenericRowWithSchema merged = aggregator.call(row1, row2);
        final List<Object> actual = new ArrayList<>(13);
        for (int i = 0; i < merged.length(); i++) {
            actual.add(merged.apply(i));
        }
        final List<Object> expected = new ArrayList<>(13);
        expected.add("src");
        expected.add("dst");
        expected.add(true);
        expected.add(new byte[]{(byte) 'c'});
        expected.add(0.8999999999999999);
        expected.add(7f);
        expected.add(new String[]{"A", "B", "C"});
        expected.add(12L);
        expected.add(10);
        expected.add(TestUtils.DATE.getTime());
        expected.add(JavaConversions$.MODULE$.mapAsScalaMap(TestUtils.MERGED_FREQMAP));
        expected.add(2);
        assertThat(expected, contains(actual.toArray()));
    }
}