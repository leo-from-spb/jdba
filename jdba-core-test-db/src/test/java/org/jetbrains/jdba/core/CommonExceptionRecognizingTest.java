package org.jetbrains.jdba.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jdba.CommonIntegrationCase;
import org.jetbrains.jdba.exceptions.NoTableOrViewException;
import org.junit.Before;
import org.junit.Test;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class CommonExceptionRecognizingTest extends CommonIntegrationCase {

  @Before
  public void setUp() throws Exception {
    DB.connect();
  }

  @Test(expected = NoTableOrViewException.class)
  public void recognize_NoTableOrView() {
    DB.inTransaction(new InTransactionNoResult() {
      @Override
      public void run(@NotNull final DBTransaction tran) {
        tran.query("select * from unexistent_table", Layouts.existence()).run();
      }
    });
  }

}
