package com.sj.gfodapp.api.response.single_fault;

import com.sj.gfodapp.model.Fault;

public class SingleFaultResponse {
   private Fault fault;
   private ReaderOperator readerOperator;

    public SingleFaultResponse() {
    }

    public SingleFaultResponse(Fault fault, ReaderOperator readerOperator) {
        this.fault = fault;
        this.readerOperator = readerOperator;
    }

    public Fault getFault() {
        return fault;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public ReaderOperator getReaderOperator() {
        return readerOperator;
    }

    public void setReaderOperator(ReaderOperator readerOperator) {
        this.readerOperator = readerOperator;
    }
}
