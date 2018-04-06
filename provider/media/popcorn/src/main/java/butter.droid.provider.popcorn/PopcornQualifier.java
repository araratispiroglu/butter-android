package butter.droid.provider.popcorn;

/**
 * Created by araratispiroglu on 5.04.2018.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PopcornQualifier {
}
