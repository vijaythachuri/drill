/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.physical.impl.sort;

import java.util.List;

import org.apache.drill.exec.record.*;
import org.apache.drill.exec.record.BatchSchema.SelectionVectorMode;
import org.apache.drill.exec.record.selection.SelectionVector2;
import org.apache.drill.exec.vector.ValueVector;

import com.google.common.collect.Lists;

/**
 * Holds the data for a particular record batch for later manipulation.
 */
public class RecordBatchData {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RecordBatchData.class);
  
  final SelectionVector2 sv2;
  final int recordCount;
  VectorContainer container = new VectorContainer();
  
  public RecordBatchData(VectorAccessible batch){
    List<ValueVector> vectors = Lists.newArrayList();
    if (batch instanceof RecordBatch && batch.getSchema().getSelectionVectorMode() == SelectionVectorMode.TWO_BYTE) {
      this.sv2 = ((RecordBatch)batch).getSelectionVector2().clone();
    } else {
      this.sv2 = null;
    }
    
    for(VectorWrapper<?> v : batch){
      if(v.isHyper()) throw new UnsupportedOperationException("Record batch data can't be created based on a hyper batch.");
      TransferPair tp = v.getValueVector().getTransferPair();
      tp.transfer();
      vectors.add(tp.getTo());
    }

    container.addCollection(vectors);
    recordCount = batch.getRecordCount();
    container.setRecordCount(recordCount);
    container.buildSchema(batch.getSchema().getSelectionVectorMode());
  }
  
  public int getRecordCount(){
    return recordCount;
  }
  public List<ValueVector> getVectors() {
    List<ValueVector> vectors = Lists.newArrayList();
    for (VectorWrapper w : container) {
      vectors.add(w.getValueVector());
    }
    return vectors;
  }

  public SelectionVector2 getSv2() {
    return sv2;
  }

  public VectorContainer getContainer() {
    return container;
  }
}
