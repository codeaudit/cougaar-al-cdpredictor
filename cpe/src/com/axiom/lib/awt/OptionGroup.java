package com.axiom.lib.awt ;

/**
 *  Contains various combinations of options settings for panels such
 *  as the OKGroupPanel.
 */

public interface OptionGroup {
  public static int OK_BUTTON = 0x0001;
  public static int CANCEL_BUTTON = 0x0002;
  public static int UNDO_BUTTON = 0x0004;
  public static int APPLY_BUTTON = 0x0008;
  public static int YES_BUTTON = 0x0010;
  public static int NO_BUTTON = 0x0020;

  public static int OK_GROUP = OK_BUTTON ;
  public static int CANCEL_GROUP = CANCEL_BUTTON ;
  public static int OK_CANCEL_GROUP = OK_BUTTON | CANCEL_BUTTON ;
  public static int OK_CANCEL_UNDO_GROUP = OK_BUTTON | CANCEL_BUTTON | UNDO_BUTTON ;
  public static int OK_CANCEL_APPLY_GROUP = OK_BUTTON | CANCEL_BUTTON | APPLY_BUTTON ;
  public static int OK_CANCEL_APPLY_UNDO_GROUP = OK_BUTTON | CANCEL_BUTTON | APPLY_BUTTON | UNDO_BUTTON ;
  public static int YES_NO_GROUP = YES_BUTTON | NO_BUTTON ;

  public static final String OK = "OK";
  public static final String CANCEL = "Cancel";
  public static final String APPLY = "Apply";
  public static final String UNDO = "Undo";
  public static final String YES = "Yes";
  public static final String NO = "No";
    
}