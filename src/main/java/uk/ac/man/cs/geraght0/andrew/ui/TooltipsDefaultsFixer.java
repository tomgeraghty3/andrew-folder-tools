package uk.ac.man.cs.geraght0.andrew.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.slf4j.LoggerFactory;

/**
 * {@link TooltipsDefaultsFixer}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
class TooltipsDefaultsFixer {

  private TooltipsDefaultsFixer() {}

  /**
   * Returns true if successful.
   * Current defaults are 1000, 5000, 200;
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  static void setTooltipTimers(final long openDelay, final long visibleDuration, final long closeDelay) {
    try {
      final Field f = Tooltip.class.getDeclaredField("BEHAVIOR");
      f.setAccessible(true);    //NOSONAR - no way to do this  without reflection until Java 9 but client using Java 8

      final Class[] classes = Tooltip.class.getDeclaredClasses();
      for (final Class clazz : classes) {
        if (clazz.getName()                                                   //NOSONAR no way to use instanceof because Tooltip Behaviour has private access
                 .equals("javafx.scene.control.Tooltip$TooltipBehavior")) {   //NOSONAR no way to use instanceof because Tooltip Behaviour has private access
          final Constructor ctor = clazz.getDeclaredConstructor(Duration.class, Duration.class, Duration.class, boolean.class);
          ctor.setAccessible(true);          //NOSONAR - no way to do this without reflection until Java 9 but client using Java 8
          final Object tooltipBehavior = ctor.newInstance(new Duration(openDelay), new Duration(visibleDuration), new Duration(closeDelay), false);
          f.set(null, tooltipBehavior);      //NOSONAR - no way to do this  without reflection until Java 9 but client using Java 8
          break;
        }
      }
    } catch (final Exception e) {
      LoggerFactory.getLogger(TooltipsDefaultsFixer.class)
                   .error("Unexpected", e);
    }
  }
}