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
package com.datastax.oss.sink.kafka.record;

import static org.assertj.core.api.Assertions.*;

import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableMap;
import com.datastax.oss.sink.kafka.KafkaAPIAdapter;
import com.datastax.oss.sink.record.KeyOrValue;
import com.datastax.oss.sink.record.KeyValueRecord;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeyValueRecordTest {

  private KeyOrValue key;
  private KeyOrValue value;
  private Set<Header> headers =
      StreamSupport.stream(
              new ConnectHeaders().addString("h1", "hv1").addString("h2", "hv2").spliterator(),
              false)
          .collect(Collectors.toSet());

  private Map<String, String> keyFields =
      ImmutableMap.<String, String>builder().put("kf1", "kv1").put("kf2", "kv2").build();
  private Map<String, String> valueFields =
      ImmutableMap.<String, String>builder().put("vf1", "vv1").put("vf2", "vv2").build();

  private KafkaAPIAdapter adapter = new KafkaAPIAdapter();

  @BeforeEach
  void setUp() {
    key =
        new KeyOrValue() {
          @Override
          public Set<String> fields() {
            return keyFields.keySet();
          }

          @Override
          public Object getFieldValue(String field) {
            return keyFields.get(field);
          }
        };

    value =
        new KeyOrValue() {
          @Override
          public Set<String> fields() {
            return valueFields.keySet();
          }

          @Override
          public Object getFieldValue(String field) {
            return valueFields.get(field);
          }
        };
  }

  @Test
  void should_qualify_field_names() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(key, value, null, null, adapter);
    assertThat(record.fields()).containsOnly("key.kf1", "key.kf2", "value.vf1", "value.vf2");
  }

  @Test
  void should_qualify_field_names_and_headers() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(key, value, null, headers, adapter);
    assertThat(record.fields())
        .containsOnly("key.kf1", "key.kf2", "value.vf1", "value.vf2", "header.h1", "header.h2");
  }

  @Test
  void should_qualify_field_names_keys_only() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(key, null, null, null, adapter);
    assertThat(record.fields()).containsOnly("key.kf1", "key.kf2");
  }

  @Test
  void should_qualify_field_names_values_only() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(null, value, null, null, adapter);
    assertThat(record.fields()).containsOnly("value.vf1", "value.vf2");
  }

  @Test
  void should_qualify_field_names_headers_only() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(null, null, null, headers, adapter);
    assertThat(record.fields()).containsOnly("header.h1", "header.h2");
  }

  @Test
  void should_get_field_values() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(key, value, null, headers, adapter);
    assertThat(record.getFieldValue("key.kf1")).isEqualTo("kv1");
    assertThat(record.getFieldValue("value.vf2")).isEqualTo("vv2");
    assertThat(record.getFieldValue("value.not_exist")).isNull();
    assertThat(record.getFieldValue("header.h1")).isEqualTo("hv1");
    assertThat(record.getFieldValue("header.not_exists")).isNull();
  }

  @Test
  void should_throw_if_get_field_value_with_not_known_prefix() {
    KeyValueRecord<Header> record = new KeyValueRecord<>(key, value, null, headers, adapter);
    assertThatThrownBy(() -> record.getFieldValue("non_existing_prefix"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("field name must start with 'key.', 'value.' or 'header.'.");
  }
}
