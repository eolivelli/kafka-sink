/*
 * Copyright DataStax, Inc.
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
package com.datastax.oss.sink.pulsar.record;

import static org.assertj.core.api.Assertions.*;

import com.datastax.oss.sink.pulsar.GRecordBuilder;
import com.datastax.oss.sink.pulsar.PulsarAPIAdapter;
import com.datastax.oss.sink.record.RawData;
import com.datastax.oss.sink.record.StructData;
import java.nio.ByteBuffer;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroRecord;
import org.junit.jupiter.api.Test;

class StructDataTest {
  private final PulsarAPIAdapter adapter = new PulsarAPIAdapter();
  private final Schema schema =
      SchemaBuilder.record("Pulsar")
          .fields()
          .optionalLong("bigint")
          .optionalBoolean("boolean")
          .optionalBytes("bytes")
          .endRecord();
  private final byte[] bytesArray = {3, 2, 1};
  private final GenericAvroRecord struct =
      new GRecordBuilder(schema)
          .put("bigint", 1234L)
          .put("boolean", false)
          .put("bytes", bytesArray)
          .build();
  private final StructData structData = new StructData(struct, adapter);

  @Test
  void should_parse_field_names_from_struct() {
    assertThat(structData.fields())
        .containsExactlyInAnyOrder(RawData.FIELD_NAME, "bigint", "boolean", "bytes");
  }

  @Test
  void should_get_field_value() {
    assertThat(structData.getFieldValue("bigint")).isEqualTo(1234L);
    assertThat(structData.getFieldValue("boolean")).isEqualTo(false);

    // Even though the record has a byte[], we must get a ByteBuffer when we
    // retrieve it because the driver requires the input to be a ByteBuffer
    // when encoding for a blob column.
    assertThat(structData.getFieldValue("bytes")).isEqualTo(ByteBuffer.wrap(bytesArray));
  }

  @Test
  void should_handle_null_struct() {
    StructData empty = new StructData<>(null, adapter);
    assertThat(empty.fields()).containsOnly(RawData.FIELD_NAME);
    assertThat(empty.getFieldValue("junk")).isNull();
  }
}