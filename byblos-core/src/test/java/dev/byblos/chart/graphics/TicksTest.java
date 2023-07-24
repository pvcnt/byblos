package dev.byblos.chart.graphics;

import com.google.common.collect.Iterables;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Ticks}.
 */
public class TicksTest {

    // There are some differences in floating point behavior that lead to some test failures
    // on ARM. For now we just disable those tests when running on ARM.
    private static final boolean isArm = "aarch64".equals(System.getProperty("os.arch"));

    @Test
    void value_0_100_5() {
        var ticks = Ticks.value(0.0, 100.0, 5);
        sanityCheck(ticks);
        assertThat(ticks).hasSize(21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
        assertEquals(ticks.get(0).offset(), 0.0);
        assertEquals(ticks.get(0).getLabel(), "0.0");
        assertLastTickEquals(ticks, "100.0");
    }

    @Test
    void value_1_2_7() {
        var ticks = Ticks.value(1.0, 2.0, 7);
        sanityCheck(ticks);
        assertThat(ticks).hasSize(21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
    }

    @Test
    void test_value_0_10_5() {
        var ticks = Ticks.value(0.0, 10.0, 5);
        sanityCheck(ticks);
        assertThat(ticks).hasSize(21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
        assertFirstTickEquals(ticks, 0.0, "0.0");
        assertLastTickEquals(ticks, "10.0");
    }

    @Test
    void test_values_0_8_5() {
        var ticks = Ticks.value(0.0, 8.0, 5);
        sanityCheck(ticks);
        assertThat(ticks).hasSize(17);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertFirstTickEquals(ticks, 0.0, "0.0");
        assertLastTickEquals(ticks, "8.0");
    }

    @Test
    void test_value_0_7_5() {
        var ticks = Ticks.value(0.0, 7.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 15);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(4);
        assertEquals(ticks.get(0).offset(), 0.0);
        assertEquals(getMajorTicks(ticks).stream().map(ValueTick::getLabel).collect(Collectors.joining(",")), "0.0,2.0,4.0,6.0");
    }

    @Test
    void test_value_096_1_5(){
        var ticks = Ticks.value(0.96, 1.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertFirstTickEquals(ticks, 0.0,"0.96");
        assertLastTickEquals(ticks, "1.00");
    }

    @Test
    void test_value_835_1068_5() {
        var ticks = Ticks.value(835.0, 1068, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 23);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertEquals(ticks.get(0).offset(), 0.0);
        // assertFirstMajorTickEquals(ticks, "0.85k");
        assertLastMajorTickEquals(ticks, "1.05k");
    }

    @Test
    void test_value_2026_2027_5() {
        var ticks = Ticks.value(2026.0, 2027.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
        assertFirstTickEquals(ticks, 2026.0, "0.0");
        assertLastTickEquals(ticks, "1.0");
    }

    @Test
    void test_value_200026_200027_5() {
        var ticks = Ticks.value(200026.0, 200027.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
        assertFirstTickEquals(ticks, 200026.0, "0.0");
        assertLastTickEquals(ticks, "1.0");
    }

    @Test
    void test_value_20002623_2000262371654_5() {
        var ticks = Ticks.value(200026.23, 200026.2371654, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 15);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(4);
        assertFirstTickEquals(ticks, 200026.23, "0.0");
        assertLastTickEquals(ticks, "7.0m");
    }

    @Test
    void test_value_2026_2047_5() {
        var ticks = Ticks.value(2026.0, 2047.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 22);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(4);
        assertEquals(ticks.get(0).offset(), 0.0);
        assertFirstMajorTickEquals(ticks, "2.030k");
        assertLastMajorTickEquals(ticks, "2.045k");
    }

    @Test
    void test_value_20_218_5() {
        var ticks = Ticks.value(20.0, 21.8, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 19);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertFirstTickEquals(ticks, 0.0, "20.0");
        assertLastTickEquals(ticks, "21.8");
    }

    @Test
    void test_value_n218_n20_5() {
        var ticks = Ticks.value(-21.8, -20.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 19);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertFirstTickEquals(ticks, 0.0, "-21.8");
        assertLastTickEquals(ticks, "-20.0");
    }

    @Test
    void test_value_n2027_n2026_5() {
        var ticks = Ticks.value(-2027.0, -2026.0, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 21);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(6);
        assertEquals(ticks.get(0).offset(), -2027.0);
        assertFirstMajorTickEquals(ticks, "0.0");
        assertLastMajorTickEquals(ticks, "1.0");
    }

    @Test
    void test_value_42_8123456e12_5() {
        var ticks = Ticks.value(42.0, 8.123456e12, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 16);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(4);
        assertFirstTickEquals(ticks, 0.0, "0.5T");
        assertLastMajorTickEquals(ticks, "8.0T");
    }

    @Test
    void test_value_21264044472658984_2128626188548245_9() {
        var ticks = Ticks.value(2126.4044472658984, 2128.626188548245, 9);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 22);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(7);
        assertFirstTickEquals(ticks, 2126.4, "100.0m");
        assertEquals(Iterables.getLast(ticks).getLabel(), "2.2");
    }

    @Test
    void test_value_0_1468en3() {
        var ticks = Ticks.value(0.0, 1.468e-3, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 15);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertEquals(ticks.get(0).offset(), 0.0);
        assertFirstMajorTickEquals(ticks, "0.0m");
        assertLastMajorTickEquals(ticks, "1.2m");
    }

    @Test
    void test_value_4560_45699_5() {
        var ticks = Ticks.value(4559.9, 4569.9, 5);
        sanityCheck(ticks);
        assertEquals(ticks.size(), 20);
        Assertions.assertThat(getMajorTicks(ticks)).hasSize(5);
        assertFirstTickEquals(ticks, 0.0, "4.560k");
        assertEquals(Iterables.getLast(ticks).getLabel(), "4.570k");
    }

    @Test
    void value_0_PositiveInfinity_5() {
        assertThatThrownBy(() -> Ticks.value(0, Double.POSITIVE_INFINITY, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("upper bound must be finite");
    }

    @Test
    void value_NegativeInfinity_0_5() {
        assertThatThrownBy(() -> Ticks.value(Double.NEGATIVE_INFINITY, 0.0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lower bound must be finite");
    }

    @Test
    void value_0_MaxValue_5() {
        Ticks.value(0.0, Double.MAX_VALUE, 5);
    }

    private static void sanityCheck(List<ValueTick> ticks) {
        checkForDuplicates(ticks);
    }

    private static void checkForDuplicates(List<ValueTick> ticks) {
        var duplicates = ticks.stream()
                .filter(ValueTick::major)
                .map(ValueTick::getLabel)
                .collect(Collectors.groupingBy(Function.identity()))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey)
                .collect(Collectors.toList());
        assertThat(duplicates).as("duplicate tick labels").isEmpty();
    }

    private static void assertFirstTickEquals(List<ValueTick> ticks, String label) {
        assertThat(Iterables.getFirst(ticks, null).getLabel()).isEqualTo(label);
    }

    private static void assertFirstTickEquals(List<ValueTick> ticks, double offset, String label) {
        assertThat(Iterables.getFirst(ticks, null).offset()).isEqualTo(offset);
        assertFirstTickEquals(ticks, label);
    }

    private static void assertLastTickEquals(List<ValueTick> ticks, String label) {
        assertThat(Iterables.getLast(ticks).getLabel()).isEqualTo(label);
    }

    private static void assertLastMajorTickEquals(List<ValueTick> ticks, String label) {
        assertThat(Iterables.getLast(getMajorTicks(ticks)).getLabel()).isEqualTo(label);
    }

    private static void assertFirstMajorTickEquals(List<ValueTick> ticks, String label) {
        assertThat(Iterables.getFirst(getMajorTicks(ticks), null).getLabel()).isEqualTo(label);
    }

    private static List<ValueTick> getMajorTicks(List<ValueTick> ticks) {
        return ticks.stream().filter(ValueTick::major).collect(Collectors.toList());
    }
}
