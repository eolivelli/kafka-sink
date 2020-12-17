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
package com.datastax.oss.common.sink.metadata;

import com.datastax.oss.common.sink.AbstractSinkRecord;
import com.datastax.oss.common.sink.record.KeyOrValue;
import com.datastax.oss.common.sink.record.RecordMetadata;

/**
 * Simple container class to tie together a {@link AbstractSinkRecord} key/value and its metadata.
 */
public class InnerDataAndMetadata {
  private final KeyOrValue innerData;
  private final RecordMetadata innerMetadata;

  InnerDataAndMetadata(KeyOrValue innerData, RecordMetadata innerMetadata) {
    this.innerMetadata = innerMetadata;
    this.innerData = innerData;
  }

  public KeyOrValue getInnerData() {
    return innerData;
  }

  public RecordMetadata getInnerMetadata() {
    return innerMetadata;
  }
}
