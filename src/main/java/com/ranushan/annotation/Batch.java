package com.ranushan.annotation;

import com.ranushan.domain.BatchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the annotated class as a Batch.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Batch {

    /**
     * The batch name.
     * <p>
     * If not specified, the batch name will be equal to the canonical class name.
     *
     * @return the batch name
     */
    String name() default "";

    /**
     * The batch type.
     * <p>
     * If not specified, {@link BatchType#TIMER} will be considered.
     *
     * @return the batch type
     */
    BatchType type() default BatchType.TIMER;

    /**
     * The interval between executions.
     * <p>
     * If not specified, a default interval will be considered for, which is defined by the
     * batch type.
     *
     * @return a string representing the configured interval between executions
     */
    String interval() default "";

    /**
     * Indicates whether the first execution should be delayed to cause the next ones
     * to occur on the interval boundary (default is {@code false}).
     * <p>
     * For example:
     * <ul>
     * <li><b>[modulate = true]:</b> if the current hour is 3h25 am and the batch interval is
     * {@code 4 hours} then the first execution will be delayed to 4h, and then the next ones
     * will occur at 8h, 12h, 16h, etc.</li>
     * <li><b>[modulate = false]:</b> if the current hour is 3h25 am and the batch interval is
     * {@code 4 hours} then the first execution will occur immediately (3h25), and then the
     * next ones will occur at 7h25, 11h25, 15h25, etc.</li>
     * </ul>
     * <p>
     * <b>NOTE:</b> This option is only applicable for batches of type {@link BatchType#TIMER}.
     *
     * @return a flag determining whether interval modulation is enabled for this batch
     */
    boolean modulate() default false;

    /**
     * Indicates whether the system will process and store statistical data (e.g.:
     * average execution duration) for this batch.
     *
     * @return a flag determining whether statistics are enabled for this batch
     */
    boolean enableStatistics() default false;
}
