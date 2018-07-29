package com.dkorobtsov.logging;

import org.junit.Test;

public class GeneralTests {

  @Test(expected = UnsupportedOperationException.class)
  public void printerCanNotBeInstantiated(){
    new Printer();
  }

}
