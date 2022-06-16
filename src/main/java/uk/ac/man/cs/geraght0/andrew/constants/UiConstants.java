package uk.ac.man.cs.geraght0.andrew.constants;

import uk.ac.man.cs.geraght0.andrew.ui.view.UiMode;

public class UiConstants {

  public static final UiMode VIEW_TO_SHOW_AFTER_PASSWORD = UiMode.CREATE_AND_ORG;

  private UiConstants() {}

  public static final int WIDTH_OVERALL = 1000;
  private static final int HEIGHT_LEFT_RIGHT = 600;
  public static final int HEIGHT_OVERALL = UiConstants.HEIGHT_LEFT_RIGHT + 100;
  public static final double MAX_TXT_HEIGHT = 40.0;
  public static final double MIN_TXT_WIDTH = UiConstants.WIDTH_OVERALL - 200.0;
}
