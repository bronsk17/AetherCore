package me.bronsk.aethercore.ui.components;

import java.util.Comparator;
import java.util.function.Function;

/**
 * One {@link AETable} column: header text, how to read a cell's display value, and how to compare
 * two rows for sorting - only used when the table is constructed sortable. The 2-arg constructor
 * defaults to natural string order on the display value; pass an explicit {@code comparator} when
 * that would sort wrong (numbers, dates, anything not already displayed in sortable string form).
 * Column order = list order.
 */
public record AEUIColDef<T>(String header, Function<T, String> accessor, Comparator<T> comparator) {

    public AEUIColDef(String header, Function<T, String> accessor) {
        this(header, accessor, Comparator.comparing(accessor));
    }
}
